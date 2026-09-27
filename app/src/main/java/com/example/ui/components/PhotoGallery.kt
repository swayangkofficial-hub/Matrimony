package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.repository.ProfilePhotoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * A comprehensive Matrimonial PhotoGallery component allowing users to upload
 * and view multiple profile images stored in Firebase Storage.
 */
@Composable
fun PhotoGallery(
    userId: String,
    photoUrls: List<String>,
    primaryAvatarUrl: String = "",
    onPhotosChanged: (updatedUrls: List<String>, newPrimaryUrl: String) -> Unit,
    modifier: Modifier = Modifier,
    photoRepository: ProfilePhotoRepository = remember { ProfilePhotoRepository() },
    isWomenPrivacyEnabled: Boolean = false,
    maxPhotos: Int = 6
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadStatusMessage by remember { mutableStateOf("") }
    var selectedPhotoForPreview by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var photoToDelete by remember { mutableStateOf<String?>(null) }

    // Multi-photo picker using modern Android Photo Picker (zero broad storage permissions)
    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxPhotos)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val remainingSlots = maxPhotos - photoUrls.size
            if (remainingSlots <= 0) {
                Toast.makeText(context, "Maximum of $maxPhotos photos already reached", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            val toUpload = uris.take(remainingSlots)
            isUploading = true
            uploadProgress = 0.05f
            uploadStatusMessage = "Connecting to Firebase Storage..."

            coroutineScope.launch {
                val uploadedList = photoUrls.toMutableList()
                var currentPrimary = primaryAvatarUrl.ifBlank { uploadedList.firstOrNull() ?: "" }

                for ((index, uri) in toUpload.withIndex()) {
                    uploadStatusMessage = "Uploading photo ${index + 1} of ${toUpload.size} to Firebase Storage..."
                    val result = photoRepository.uploadProfilePhoto(
                        userId = userId,
                        imageUri = uri,
                        context = context,
                        onProgress = { p ->
                            uploadProgress = ((index + p) / toUpload.size).coerceIn(0f, 1f)
                        }
                    )
                    if (result.isSuccess) {
                        val downloadUrl = result.getOrThrow()
                        uploadedList.add(downloadUrl)
                        if (currentPrimary.isBlank()) {
                            currentPrimary = downloadUrl
                        }
                    } else {
                        Toast.makeText(context, "Failed to upload one image", Toast.LENGTH_SHORT).show()
                    }
                }

                isUploading = false
                uploadProgress = 0f
                uploadStatusMessage = ""
                onPhotosChanged(uploadedList, currentPrimary)
                Toast.makeText(context, "Photos successfully added to profile!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePure),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("photo_gallery_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Photo Gallery",
                        tint = BurgundyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Matrimonial Photo Album",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BurgundyDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TrustGreenLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Storage",
                            tint = TrustGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${photoUrls.size}/$maxPhotos Stored",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Authentic photos with family and traditional attire increase genuine matrimonial interest by 300%.",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium
            )

            // Women's extra privacy alert indicator
            if (isWomenPrivacyEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarningAmberLight,
                    border = BorderStroke(1.dp, WarningAmber)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Privacy Shield",
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Women's Privacy Shield active: Photos are watermarked and visible only to verified matches you accept.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }

            // Upload Progress Bar
            if (isUploading) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BackgroundIvory,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uploadStatusMessage.ifBlank { "Uploading to Firebase Storage..." },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BurgundyDark
                            )
                            Text(
                                text = "${(uploadProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BurgundyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BurgundyPrimary,
                            trackColor = Color(0xFFE5E7EB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Photos + Add Photo Card
            val totalCells = photoUrls.size + (if (photoUrls.size < maxPhotos) 1 else 0)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Chunk into rows of 3
                val rows = (0 until totalCells).chunked(3)
                for (rowIndices in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (idx in rowIndices) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (idx < photoUrls.size) {
                                    val photoUrl = photoUrls[idx]
                                    val isPrimary = (photoUrl == primaryAvatarUrl) || (primaryAvatarUrl.isBlank() && idx == 0)

                                    PhotoThumbnailCard(
                                        photoUrl = photoUrl,
                                        isPrimary = isPrimary,
                                        index = idx,
                                        onClick = { selectedPhotoForPreview = idx to photoUrl },
                                        onDelete = { photoToDelete = photoUrl },
                                        onSetPrimary = {
                                            val reordered = photoUrls.toMutableList()
                                            reordered.remove(photoUrl)
                                            reordered.add(0, photoUrl)
                                            onPhotosChanged(reordered, photoUrl)
                                            Toast.makeText(context, "Set as primary profile photo", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    // Add Photo Slot Card
                                    AddPhotoCard(
                                        enabled = !isUploading,
                                        onClick = {
                                            multiPhotoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        // Fill empty trailing spaces in row
                        if (rowIndices.size < 3) {
                            for (k in 0 until (3 - rowIndices.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Upload Action Footer Button
            if (photoUrls.size < maxPhotos) {
                OutlinedButton(
                    onClick = {
                        multiPhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isUploading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BurgundyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_photo_gallery_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Upload Photos",
                        tint = BurgundyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (photoUrls.isEmpty()) "Upload Profile Photos to Firebase" else "Add More Photos (${maxPhotos - photoUrls.size} left)",
                        fontWeight = FontWeight.Bold,
                        color = BurgundyPrimary
                    )
                }
            }
        }
    }

    // Modal: Full-screen Photo Viewer Dialog
    selectedPhotoForPreview?.let { (index, url) ->
        val isPrimary = (url == primaryAvatarUrl) || (primaryAvatarUrl.isBlank() && index == 0)

        Dialog(
            onDismissRequest = { selectedPhotoForPreview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .testTag("photo_viewer_dialog"),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Dialog Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Photo ${index + 1} of ${photoUrls.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isPrimary) {
                                Text(
                                    text = "★ Primary Profile Photo",
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedPhotoForPreview = null },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close preview",
                                tint = Color.White
                            )
                        }
                    }

                    // Enlarge Image
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Full Size Profile Photo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    // Dialog Actions Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isPrimary) {
                            Button(
                                onClick = {
                                    val reordered = photoUrls.toMutableList()
                                    reordered.remove(url)
                                    reordered.add(0, url)
                                    onPhotosChanged(reordered, url)
                                    selectedPhotoForPreview = 0 to url
                                    Toast.makeText(context, "Set as Primary Photo", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("set_primary_photo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Set Primary",
                                    tint = BurgundyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Make Primary",
                                    color = BurgundyDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                selectedPhotoForPreview = null
                                photoToDelete = url
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier
                                .weight(if (!isPrimary) 1f else 2f)
                                .testTag("delete_photo_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Delete",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Confirmation Dialog for Deleting Photo
    photoToDelete?.let { targetUrl ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = {
                Text("Delete Profile Photo?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to remove this photo from your matrimonial album and Firebase Storage?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = photoUrls.filterNot { it == targetUrl }
                        val newPrimary = if (primaryAvatarUrl == targetUrl) {
                            updated.firstOrNull() ?: ""
                        } else {
                            primaryAvatarUrl
                        }
                        photoToDelete = null
                        coroutineScope.launch {
                            photoRepository.deleteProfilePhoto(targetUrl)
                            onPhotosChanged(updated, newPrimary)
                            Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PhotoThumbnailCard(
    photoUrl: String,
    isPrimary: Boolean,
    index: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isPrimary) 2.dp else 1.dp,
            color = if (isPrimary) GoldAccent else CardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("photo_item_card_$index")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = if (isPrimary) "Primary profile photo" else "Profile photo $index",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Primary badge overlay
            if (isPrimary) {
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = GoldAccent,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Primary",
                            tint = BurgundyDark,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Main",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark
                        )
                    }
                }
            }

            // Quick delete icon overlay at top end
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("delete_thumb_btn_$index")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPhotoCard(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, BurgundyLight),
        colors = CardDefaults.cardColors(containerColor = BackgroundIvory),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .testTag("add_photo_empty_slot")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BurgundyPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add Photo",
                    tint = BurgundyPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add Photo",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BurgundyPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Firebase Cloud",
                fontSize = 9.sp,
                color = NeutralMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
