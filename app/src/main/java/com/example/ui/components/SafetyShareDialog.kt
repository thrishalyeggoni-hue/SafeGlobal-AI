package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URLEncoder

object SafetyShareHelper {

    fun shareToWhatsApp(context: Context, message: String, phone: String? = null) {
        try {
            val cleanPhone = phone?.filter { it.isDigit() }?.let {
                if (it.length == 10) "91$it" else it
            }
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = if (!cleanPhone.isNullOrBlank()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp package direct launch failed, fallback to web intent or generic share
            try {
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val webUri = Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                shareGeneric(context, message, "Share via WhatsApp")
            }
        }
    }

    fun shareToSms(context: Context, message: String, phone: String? = null) {
        try {
            val cleanPhone = phone?.filter { it.isDigit() || it == '+' }
            val uri = if (!cleanPhone.isNullOrBlank()) {
                Uri.parse("smsto:$cleanPhone")
            } else {
                Uri.parse("smsto:")
            }
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(viewIntent)
            } catch (e2: Exception) {
                shareGeneric(context, message, "Send Normal SMS Message")
            }
        }
    }

    fun shareGeneric(context: Context, message: String, title: String = "Share") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "SafeSphere") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun SafetyShareDialog(
    title: String,
    subtitle: String,
    message: String,
    recipientName: String? = null,
    recipientPhone: String? = null,
    isEmergency: Boolean = false,
    isImOk: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isEmergency -> Color(0xFFFFDAD6)
                                    isImOk -> Color(0xFFDCFCE7)
                                    else -> Color(0xFFEEF2FF)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isEmergency -> Icons.Default.Warning
                                isImOk -> Icons.Default.CheckCircle
                                else -> Icons.Default.LocationOn
                            },
                            contentDescription = null,
                            tint = when {
                                isEmergency -> Color(0xFFBA1A1A)
                                isImOk -> Color(0xFF16A34A)
                                else -> Color(0xFF4F46E5)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Recipient info chip if available
                if (!recipientName.isNullOrBlank() || !recipientPhone.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(14.dp))
                            Text(
                                text = "Recipient: ${recipientName ?: "Mom"} ${recipientPhone?.let { "($it)" } ?: ""}".trim(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                // Message preview
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Message to send:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Primary Action 1: WHATSAPP
                Button(
                    onClick = {
                        SafetyShareHelper.shareToWhatsApp(context, message, recipientPhone)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!recipientName.isNullOrBlank()) "Forward to $recipientName on WhatsApp" else "Send via WhatsApp",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Primary Action 2: NORMAL MESSAGES (SMS)
                Button(
                    onClick = {
                        SafetyShareHelper.shareToSms(context, message, recipientPhone)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "SMS", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!recipientName.isNullOrBlank()) "Send SMS to $recipientName" else "Send via Normal Messages (SMS)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Copy to Clipboard
                OutlinedButton(
                    onClick = {
                        SafetyShareHelper.copyToClipboard(context, message)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Copy Location & Message", fontSize = 12.sp, color = Color(0xFF475569))
                }
            }
        },
        confirmButton = {}
    )
}
