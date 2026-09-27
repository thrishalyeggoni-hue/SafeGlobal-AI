package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R

object SafeSphereAvatarHelper {
    val AVATAR_RESOURCES = listOf(
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6,
        R.drawable.avatar_7,
        R.drawable.avatar_8,
        R.drawable.avatar_9,
        R.drawable.avatar_10,
        R.drawable.avatar_11,
        R.drawable.avatar_12,
        R.drawable.avatar_13,
        R.drawable.avatar_14,
        R.drawable.avatar_15,
        R.drawable.avatar_16
    )

    fun getAvatarDrawable(index: Int): Int {
        val safeIdx = (index - 1).coerceIn(0, AVATAR_RESOURCES.size - 1)
        return AVATAR_RESOURCES[safeIdx]
    }
}

/**
 * Inline horizontal scrollable row to pick an avatar (used in Create Account screens).
 */
@Composable
fun AvatarPickerRow(
    selectedAvatarIndex: Int,
    onAvatarSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SafeSphereAvatarHelper.AVATAR_RESOURCES.forEachIndexed { i, drawableRes ->
            val avatarNum = i + 1
            val isSelected = selectedAvatarIndex == avatarNum

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarSelected(avatarNum) }
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, Color(0xFF22C55E), CircleShape)
                        } else {
                            Modifier.border(1.dp, Color(0xFF334155), CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = "Avatar $avatarNum",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4x4 Grid Dialog to view and choose from all 16 avatars anytime inside the app.
 */
@Composable
fun AvatarSelectionDialog(
    currentAvatarIndex: Int,
    onAvatarSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var tempSelected by remember(currentAvatarIndex) { mutableIntStateOf(currentAvatarIndex.coerceIn(1, 16)) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Your Avatar",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Select your portrait from the 16 SafeSphere avatars",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // 4x4 Grid of 16 circular avatars
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(SafeSphereAvatarHelper.AVATAR_RESOURCES) { index, resId ->
                            val avatarNum = index + 1
                            val isSelected = tempSelected == avatarNum

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .clickable { tempSelected = avatarNum }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, Color(0xFF2563EB), CircleShape)
                                        } else {
                                            Modifier.border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = "Avatar $avatarNum",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onAvatarSelected(tempSelected)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Save Avatar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
