package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import com.example.R
import com.example.ui.theme.*

/**
 * Reusable visual confirmation dialog integrating Lottie animations.
 * Provides high-impact feedback when users complete critical matrimonial actions
 * such as updating privacy settings, blocking a user, reporting a profile, or verifying OTP.
 */
@Composable
fun ActionSuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actionButtonText: String = "Done",
    detailsBadgeText: String? = null,
    secondaryIcon: ImageVector = Icons.Default.CheckCircle,
    accentColor: Color = TrustGreen
) {
    // Load Lottie animation from raw resources
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success_animation))
    val composition = compositionResult.value
    val isLottieLoading = compositionResult.isLoading

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true,
        speed = 1.0f
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfacePure,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.5.dp, GoldLight),
            modifier = modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight()
                .testTag("action_success_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Lottie Animation Container with graceful fallback
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (composition != null) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("action_success_lottie_animation")
                        )
                    } else {
                        // Fallback while loading or if composition is null
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = secondaryIcon,
                                contentDescription = "Action Successful",
                                tint = accentColor,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BurgundyDark,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.testTag("action_success_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = NeutralDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .testTag("action_success_message")
                )

                // Optional detail badge / pill
                if (!detailsBadgeText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = TrustGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = detailsBadgeText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BurgundyDark
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Confirmation Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("action_success_done_btn")
                ) {
                    Text(
                        text = actionButtonText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
