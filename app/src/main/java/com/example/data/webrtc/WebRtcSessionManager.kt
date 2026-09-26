package com.example.data.webrtc

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.webrtc.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Production-ready WebRTC Session Manager for SafeSphere.
 * Manages real-time peer-to-peer video streaming between Parent and Child devices.
 * Uses Google STUN servers and Firebase Firestore for SDP and ICE candidate signaling.
 */
object WebRtcSessionManager {

    private const val TAG = "SafeSphereWebRtc"
    private const val COL_SESSIONS = "cameraSessions"
    private const val SUBCOL_CHILD_CANDIDATES = "childCandidates"
    private const val SUBCOL_PARENT_CANDIDATES = "parentCandidates"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    var eglBase: EglBase? = null
        private set

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var sessionListener: ListenerRegistration? = null
    private var candidateListener: ListenerRegistration? = null

    private val _connectionState = MutableStateFlow(WebRtcState.DISCONNECTED)
    val connectionState: StateFlow<WebRtcState> = _connectionState.asStateFlow()

    private var currentSessionId: String? = null
    private var currentRole: SessionRole = SessionRole.VIEWER

    // Memory fallback bus for candidates when Firestore is offline
    private val memoryCandidates = ConcurrentHashMap<String, MutableList<Map<String, Any>>>()

    enum class WebRtcState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED,
        CLOSED
    }

    enum class SessionRole {
        STREAMER, // Child phone streaming camera
        VIEWER    // Parent phone receiving live video
    }

    data class SessionSignalingData(
        val sessionId: String = "",
        val parentUid: String = "",
        val parentName: String = "",
        val childUid: String = "",
        val studentName: String = "",
        val cameraFacing: String = "BACK", // "FRONT" or "BACK"
        val status: String = "REQUESTED",  // REQUESTED, ACCEPTED, CONNECTING, LIVE, DECLINED, ENDED, EXPIRED
        val offer: Map<String, String>? = null,
        val answer: Map<String, String>? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val expiresAt: Long = System.currentTimeMillis() + 60 * 1000L
    )

    fun initialize(context: Context) {
        if (eglBase != null && peerConnectionFactory != null) return

        try {
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)

            eglBase = EglBase.create()

            val encoderFactory = DefaultVideoEncoderFactory(eglBase?.eglBaseContext, true, true)
            val decoderFactory = DefaultVideoDecoderFactory(eglBase?.eglBaseContext)

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .setOptions(PeerConnectionFactory.Options())
                .createPeerConnectionFactory()

            Log.i(TAG, "WebRTC successfully initialized with hardware video pipeline.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WebRTC engine: ${e.message}", e)
        }
    }

    private fun getIceServers(): List<PeerConnection.IceServer> {
        return listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer()
        )
    }

    /**
     * CHILD (Streamer): Begins capturing video from camera and offers WebRTC stream.
     */
    fun startStreaming(
        context: Context,
        sessionId: String,
        initialFacingFront: Boolean,
        localView: SurfaceViewRenderer? = null,
        onSessionEnded: () -> Unit
    ) {
        initialize(context)
        closeActiveSession()

        currentSessionId = sessionId
        currentRole = SessionRole.STREAMER
        _connectionState.value = WebRtcState.CONNECTING

        try {
            val factory = peerConnectionFactory ?: run {
                _connectionState.value = WebRtcState.FAILED
                return
            }

            // 1. Setup Camera Video Source & Capturer
            val enumerator = Camera2Enumerator(context)
            val deviceNames = enumerator.deviceNames
            val selectedCamera = deviceNames.firstOrNull { name ->
                if (initialFacingFront) enumerator.isFrontFacing(name) else enumerator.isBackFacing(name)
            } ?: deviceNames.firstOrNull()

            if (selectedCamera == null) {
                Log.e(TAG, "No suitable camera found on device")
                _connectionState.value = WebRtcState.FAILED
                return
            }

            videoCapturer = enumerator.createCapturer(selectedCamera, object : CameraVideoCapturer.CameraEventsHandler {
                override fun onCameraError(p0: String?) {
                    Log.e(TAG, "Camera error: $p0")
                }
                override fun onCameraDisconnected() {
                    Log.w(TAG, "Camera disconnected")
                }
                override fun onCameraFreezed(p0: String?) {
                    Log.w(TAG, "Camera freezed: $p0")
                }
                override fun onCameraOpening(p0: String?) {}
                override fun onFirstFrameAvailable() {}
                override fun onCameraClosed() {}
            })

            surfaceTextureHelper = SurfaceTextureHelper.create("WebRtcCaptureThread", eglBase?.eglBaseContext)
            videoSource = factory.createVideoSource(videoCapturer!!.isScreencast)
            videoCapturer!!.initialize(surfaceTextureHelper, context, videoSource!!.capturerObserver)
            videoCapturer!!.startCapture(720, 480, 30)

            localVideoTrack = factory.createVideoTrack("SafeSphereVideoTrack", videoSource)
            localView?.let { localVideoTrack?.addSink(it) }

            // 2. Setup PeerConnection
            val rtcConfig = PeerConnection.RTCConfiguration(getIceServers()).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            }

            val observer = object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate?) {
                    candidate?.let { sendIceCandidate(sessionId, it, isChild = true) }
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    Log.d(TAG, "Streamer ICE State: $newState")
                    when (newState) {
                        PeerConnection.IceConnectionState.CONNECTED,
                        PeerConnection.IceConnectionState.COMPLETED -> {
                            _connectionState.value = WebRtcState.CONNECTED
                            updateSessionStatus(sessionId, "LIVE")
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED -> {
                            _connectionState.value = WebRtcState.DISCONNECTED
                        }
                        PeerConnection.IceConnectionState.FAILED -> {
                            _connectionState.value = WebRtcState.FAILED
                        }
                        PeerConnection.IceConnectionState.CLOSED -> {
                            _connectionState.value = WebRtcState.CLOSED
                            onSessionEnded()
                        }
                        else -> {}
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
                override fun onAddStream(stream: MediaStream?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onDataChannel(dc: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            }

            peerConnection = factory.createPeerConnection(rtcConfig, observer)
            peerConnection?.addTrack(localVideoTrack)

            // 3. Create Offer
            val sdpConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
            }

            peerConnection?.createOffer(object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription?) {
                    sdp ?: return
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            // Send offer to Firestore
                            sendOffer(sessionId, sdp.description)
                        }
                        override fun onCreateFailure(error: String?) {
                            Log.e(TAG, "Failed to set local description: $error")
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Failed to set local description: $error")
                        }
                    }, sdp)
                }

                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String?) {
                    Log.e(TAG, "Failed to create offer: $error")
                }
                override fun onSetFailure(error: String?) {}
            }, sdpConstraints)

            // 4. Listen for Parent's Answer & ICE Candidates
            listenForAnswerAndCandidates(sessionId, isChild = true, onSessionEnded = onSessionEnded)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting WebRTC stream: ${e.message}", e)
            _connectionState.value = WebRtcState.FAILED
        }
    }

    /**
     * PARENT (Viewer): Receives WebRTC video stream and renders on remoteView.
     */
    fun startViewing(
        context: Context,
        sessionId: String,
        remoteView: SurfaceViewRenderer,
        onSessionEnded: () -> Unit
    ) {
        initialize(context)
        closeActiveSession()

        currentSessionId = sessionId
        currentRole = SessionRole.VIEWER
        _connectionState.value = WebRtcState.CONNECTING

        try {
            val factory = peerConnectionFactory ?: run {
                _connectionState.value = WebRtcState.FAILED
                return
            }

            val rtcConfig = PeerConnection.RTCConfiguration(getIceServers()).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            }

            val observer = object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate?) {
                    candidate?.let { sendIceCandidate(sessionId, it, isChild = false) }
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    Log.d(TAG, "Viewer ICE State: $newState")
                    when (newState) {
                        PeerConnection.IceConnectionState.CONNECTED,
                        PeerConnection.IceConnectionState.COMPLETED -> {
                            _connectionState.value = WebRtcState.CONNECTED
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED -> {
                            _connectionState.value = WebRtcState.DISCONNECTED
                        }
                        PeerConnection.IceConnectionState.FAILED -> {
                            _connectionState.value = WebRtcState.FAILED
                        }
                        PeerConnection.IceConnectionState.CLOSED -> {
                            _connectionState.value = WebRtcState.CLOSED
                            onSessionEnded()
                        }
                        else -> {}
                    }
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    val track = transceiver?.receiver?.track()
                    if (track is VideoTrack) {
                        Log.i(TAG, "Remote video track received from Child. Attaching to SurfaceViewRenderer.")
                        track.addSink(remoteView)
                    }
                }

                override fun onAddStream(stream: MediaStream?) {
                    val track = stream?.videoTracks?.firstOrNull()
                    if (track != null) {
                        Log.i(TAG, "Remote stream video track received. Attaching sink.")
                        track.addSink(remoteView)
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onDataChannel(dc: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            }

            peerConnection = factory.createPeerConnection(rtcConfig, observer)

            // Listen for Child's Offer, generate Answer, and exchange ICE candidates
            listenForOfferAndCandidates(sessionId, remoteView, onSessionEnded)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting WebRTC viewer: ${e.message}", e)
            _connectionState.value = WebRtcState.FAILED
        }
    }

    fun switchCamera(onSuccess: ((Boolean) -> Unit)? = null) {
        val capturer = videoCapturer ?: return
        capturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                Log.d(TAG, "Camera switched successfully. Front: $isFrontCamera")
                onSuccess?.invoke(isFrontCamera)
            }

            override fun onCameraSwitchError(errorDescription: String?) {
                Log.e(TAG, "Camera switch error: $errorDescription")
            }
        })
    }

    private fun sendOffer(sessionId: String, sdp: String) {
        scope.launch {
            try {
                db?.collection(COL_SESSIONS)?.document(sessionId)?.update(
                    mapOf(
                        "offer" to mapOf("type" to "offer", "sdp" to sdp),
                        "status" to "CONNECTING"
                    )
                )?.await()
            } catch (e: Exception) {
                Log.w(TAG, "Error sending offer: ${e.message}")
            }
        }
    }

    private fun sendAnswer(sessionId: String, sdp: String) {
        scope.launch {
            try {
                db?.collection(COL_SESSIONS)?.document(sessionId)?.update(
                    mapOf(
                        "answer" to mapOf("type" to "answer", "sdp" to sdp),
                        "status" to "LIVE"
                    )
                )?.await()
            } catch (e: Exception) {
                Log.w(TAG, "Error sending answer: ${e.message}")
            }
        }
    }

    private fun sendIceCandidate(sessionId: String, candidate: IceCandidate, isChild: Boolean) {
        val subcol = if (isChild) SUBCOL_CHILD_CANDIDATES else SUBCOL_PARENT_CANDIDATES
        val data = mapOf(
            "sdpMid" to (candidate.sdpMid ?: ""),
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdp" to candidate.sdp,
            "timestamp" to System.currentTimeMillis()
        )

        // Memory fallback
        val key = "$sessionId-$subcol"
        memoryCandidates.getOrPut(key) { mutableListOf() }.add(data)

        scope.launch {
            try {
                db?.collection(COL_SESSIONS)?.document(sessionId)
                    ?.collection(subcol)
                    ?.add(data)
                    ?.await()
            } catch (e: Exception) {
                Log.w(TAG, "Error sending ICE candidate: ${e.message}")
            }
        }
    }

    private fun listenForAnswerAndCandidates(sessionId: String, isChild: Boolean, onSessionEnded: () -> Unit) {
        sessionListener?.remove()
        sessionListener = db?.collection(COL_SESSIONS)?.document(sessionId)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val status = snapshot.getString("status")
                if (status == "ENDED" || status == "DECLINED" || status == "EXPIRED") {
                    _connectionState.value = WebRtcState.CLOSED
                    onSessionEnded()
                    return@addSnapshotListener
                }

                @Suppress("UNCHECKED_CAST")
                val answerMap = snapshot.get("answer") as? Map<String, String>
                if (answerMap != null && peerConnection?.remoteDescription == null) {
                    val sdp = answerMap["sdp"]
                    if (!sdp.isNullOrEmpty()) {
                        val sessionDesc = SessionDescription(SessionDescription.Type.ANSWER, sdp)
                        peerConnection?.setRemoteDescription(object : SdpObserver {
                            override fun onSetSuccess() {
                                Log.i(TAG, "Remote answer set successfully on streamer.")
                            }
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onCreateFailure(p0: String?) {}
                            override fun onSetFailure(p0: String?) {}
                        }, sessionDesc)
                    }
                }
            }

        // Listen for candidates from opposite peer
        val oppositeSubcol = if (isChild) SUBCOL_PARENT_CANDIDATES else SUBCOL_CHILD_CANDIDATES
        candidateListener?.remove()
        candidateListener = db?.collection(COL_SESSIONS)?.document(sessionId)
            ?.collection(oppositeSubcol)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                for (doc in snapshot.documentChanges) {
                    if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val sdp = doc.document.getString("sdp") ?: continue
                        val sdpMid = doc.document.getString("sdpMid") ?: ""
                        val sdpMLineIndex = doc.document.getLong("sdpMLineIndex")?.toInt() ?: 0
                        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                        peerConnection?.addIceCandidate(candidate)
                    }
                }
            }
    }

    private fun listenForOfferAndCandidates(
        sessionId: String,
        remoteView: SurfaceViewRenderer,
        onSessionEnded: () -> Unit
    ) {
        sessionListener?.remove()
        sessionListener = db?.collection(COL_SESSIONS)?.document(sessionId)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val status = snapshot.getString("status")
                if (status == "ENDED" || status == "DECLINED" || status == "EXPIRED") {
                    _connectionState.value = WebRtcState.CLOSED
                    onSessionEnded()
                    return@addSnapshotListener
                }

                @Suppress("UNCHECKED_CAST")
                val offerMap = snapshot.get("offer") as? Map<String, String>
                if (offerMap != null && peerConnection?.remoteDescription == null) {
                    val sdp = offerMap["sdp"]
                    if (!sdp.isNullOrEmpty()) {
                        val sessionDesc = SessionDescription(SessionDescription.Type.OFFER, sdp)
                        peerConnection?.setRemoteDescription(object : SdpObserver {
                            override fun onSetSuccess() {
                                Log.i(TAG, "Remote offer set successfully on viewer. Creating answer...")
                                val constraints = MediaConstraints().apply {
                                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
                                }
                                peerConnection?.createAnswer(object : SdpObserver {
                                    override fun onCreateSuccess(answerSdp: SessionDescription?) {
                                        answerSdp ?: return
                                        peerConnection?.setLocalDescription(object : SdpObserver {
                                            override fun onSetSuccess() {
                                                sendAnswer(sessionId, answerSdp.description)
                                            }
                                            override fun onCreateSuccess(p0: SessionDescription?) {}
                                            override fun onCreateFailure(p0: String?) {}
                                            override fun onSetFailure(p0: String?) {}
                                        }, answerSdp)
                                    }
                                    override fun onSetSuccess() {}
                                    override fun onCreateFailure(p0: String?) {}
                                    override fun onSetFailure(p0: String?) {}
                                }, constraints)
                            }
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onCreateFailure(p0: String?) {}
                            override fun onSetFailure(p0: String?) {}
                        }, sessionDesc)
                    }
                }
            }

        // Listen for child's candidates
        candidateListener?.remove()
        candidateListener = db?.collection(COL_SESSIONS)?.document(sessionId)
            ?.collection(SUBCOL_CHILD_CANDIDATES)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                for (doc in snapshot.documentChanges) {
                    if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val sdp = doc.document.getString("sdp") ?: continue
                        val sdpMid = doc.document.getString("sdpMid") ?: ""
                        val sdpMLineIndex = doc.document.getLong("sdpMLineIndex")?.toInt() ?: 0
                        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                        peerConnection?.addIceCandidate(candidate)
                    }
                }
            }
    }

    fun endSession(sessionId: String) {
        updateSessionStatus(sessionId, "ENDED")
        closeActiveSession()
    }

    private fun updateSessionStatus(sessionId: String, status: String) {
        scope.launch {
            try {
                db?.collection(COL_SESSIONS)?.document(sessionId)?.update(
                    mapOf("status" to status, "updatedAt" to System.currentTimeMillis())
                )?.await()
            } catch (e: Exception) {
                Log.w(TAG, "Error updating session status: ${e.message}")
            }
        }
    }

    fun closeActiveSession() {
        sessionListener?.remove()
        sessionListener = null
        candidateListener?.remove()
        candidateListener = null

        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null

            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null

            localVideoTrack?.dispose()
            localVideoTrack = null

            videoSource?.dispose()
            videoSource = null

            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up WebRTC session: ${e.message}")
        }

        _connectionState.value = WebRtcState.DISCONNECTED
        currentSessionId = null
    }
}
