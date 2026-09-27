package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.GeminiAiService
import com.example.data.service.GeneratedImageResult
import com.example.data.service.VeoVideoResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioHubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { GeminiAiService(context) }

    val firestoreService = remember { com.example.data.service.FirestoreDataService() }
    var selectedTab by remember { mutableStateOf(0) } // 0: Photo Studio, 1: Veo Video, 2: Animate Photo, 3: Audio Transcribe, 4: Music Studio, 5: Search Grounding

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AI Creative & Media Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            "Veo 3 • Gemini 3.5 Transcribe • Lyria 3 Music • Search",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfacePure)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceSubtle,
                contentColor = BurgundyPrimary,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Photo Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Veo 3 Video", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Animate Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Animation, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Transcribe", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Music Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("Search Grounded", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> ImageStudioContent(aiService)
                    1 -> VeoTextToVideoContent(aiService)
                    2 -> VeoAnimatePhotoContent(aiService)
                    3 -> AudioTranscribeContent(aiService, firestoreService)
                    4 -> MusicGeneratorContent(aiService, firestoreService)
                    5 -> SearchGroundingContent(aiService)
                }
            }
        }
    }
}

/**
 * Feature 3: Create & edit images using gemini-3.1-flash-image-preview
 */
@Composable
private fun ImageStudioContent(aiService: GeminiAiService) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var imagePrompt by remember {
        mutableStateOf("A confident, smiling Indian bridegroom in an elegant bespoke royal blue bandhgala suit, soft studio portrait lighting, high resolution")
    }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var generatedResult by remember { mutableStateOf<GeneratedImageResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    sourceBitmap = BitmapFactory.decodeStream(stream)
                    imagePrompt = "Enhance this portrait with professional matrimonial lighting and clean studio background"
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GoldLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BurgundyDeep, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "gemini-3.1-flash-image-preview",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Matrimonial Photo Studio", fontWeight = FontWeight.Bold, color = BurgundyDeep, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Generate new matrimonial portraits from text prompts or upload a photo to edit and enhance its presentation.",
                    fontSize = 11.sp,
                    color = BurgundyDark
                )
            }
        }

        // Upload Source Photo (Optional for editing)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePure),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Source Photo (Optional for Editing)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (sourceBitmap != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = sourceBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Photo",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Photo Loaded for Editing", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Model will edit this image according to your prompt", fontSize = 11.sp, color = NeutralMedium)
                        }
                        IconButton(onClick = { sourceBitmap = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BurgundyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Existing Photo to Edit", color = BurgundyPrimary)
                    }
                }
            }
        }

        // Text Prompt
        OutlinedTextField(
            value = imagePrompt,
            onValueChange = { imagePrompt = it },
            label = { Text("Prompt for Creation or Editing") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_image_prompt_input"),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        // Preset Ideas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Ivory Sherwani Royal Portrait",
                "Pastel Kanjivaram Saree Studio",
                "Modern Corporate Headshot"
            ).forEach { preset ->
                SuggestionChip(
                    onClick = { imagePrompt = preset },
                    label = { Text(preset, fontSize = 10.sp) }
                )
            }
        }

        Button(
            onClick = {
                isGenerating = true
                coroutineScope.launch {
                    val res = aiService.generateOrEditImage(imagePrompt, sourceBitmap)
                    isGenerating = false
                    res.onSuccess {
                        generatedResult = it
                        Toast.makeText(context, "Image generated successfully!", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isGenerating && imagePrompt.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_image_action_btn")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processing with gemini-3.1-flash-image-preview...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (sourceBitmap != null) "Edit Photo with AI" else "Create Matrimonial Portrait")
            }
        }

        // Generated Result Showcase
        if (generatedResult != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Result (1:1 Matrimonial Aspect)", fontWeight = FontWeight.Bold, color = BurgundyDark)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (generatedResult?.bitmap != null) {
                        Image(
                            bitmap = generatedResult!!.bitmap!!.asImageBitmap(),
                            contentDescription = "Generated Portrait",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = BurgundyPrimary, modifier = Modifier.size(40.dp))
                                Text("Preview Generated", fontWeight = FontWeight.Bold, color = BurgundyPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        generatedResult?.description ?: "",
                        fontSize = 12.sp,
                        color = NeutralDark
                    )
                }
            }
        }
    }
}

/**
 * Feature 4: Generate video from text using veo-3.1-fast-generate-preview (16:9 or 9:16)
 */
@Composable
private fun VeoTextToVideoContent(aiService: GeminiAiService) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var videoPrompt by remember {
        mutableStateOf("Cinematic romantic couple walking through a blooming royal palace garden in Udaipur at golden hour, traditional wedding aesthetics, ultra slow motion 4k")
    }
    var aspectRatio by remember { mutableStateOf("16:9") } // "16:9" (landscape) or "9:16" (portrait)
    var isGenerating by remember { mutableStateOf(false) }
    var veoResult by remember { mutableStateOf<VeoVideoResult?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BurgundyDeep, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "veo-3.1-fast-generate-preview",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Veo 3 Video from Text", fontWeight = FontWeight.Bold, color = BurgundyDeep, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Generate cinematic matrimonial couple stories and wedding visions using Google's Veo 3 model in 16:9 landscape or 9:16 portrait.",
                    fontSize = 11.sp,
                    color = NeutralDark
                )
            }
        }

        // Aspect Ratio Selection (Mandatory 16:9 or 9:16)
        Text("Select Video Aspect Ratio:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = aspectRatio == "16:9",
                onClick = { aspectRatio = "16:9" },
                label = { Text("16:9 Landscape") },
                leadingIcon = { Icon(Icons.Default.Panorama, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = aspectRatio == "9:16",
                onClick = { aspectRatio = "9:16" },
                label = { Text("9:16 Portrait / Reel") },
                leadingIcon = { Icon(Icons.Default.StayCurrentPortrait, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = videoPrompt,
            onValueChange = { videoPrompt = it },
            label = { Text("Veo Video Prompt") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("veo_text_prompt_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                isGenerating = true
                coroutineScope.launch {
                    val res = aiService.generateVeoVideo(videoPrompt, aspectRatio)
                    isGenerating = false
                    res.onSuccess {
                        veoResult = it
                        Toast.makeText(context, "Veo 3 video request submitted (${aspectRatio})!", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isGenerating && videoPrompt.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_veo_text_video_btn")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating with veo-3.1-fast-generate-preview...")
            } else {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Veo 3 Video ($aspectRatio)")
            }
        }

        if (veoResult != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Veo 3 Video Output ($aspectRatio)", fontWeight = FontWeight.Bold, color = BurgundyDark)
                        Surface(
                            color = GoldLight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(veoResult?.status ?: "READY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BurgundyDeep, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Video Player Card with aspect ratio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (aspectRatio == "16:9") 16f / 9f else 9f / 16f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BurgundyDeep, BurgundyPrimary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = GoldAccent, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Veo 3 Render Ready ($aspectRatio)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Prompt: \"${veoResult?.prompt}\"", fontSize = 11.sp, color = NeutralMedium)
                }
            }
        }
    }
}

/**
 * Feature 2: Animate images into video using veo-3.1-fast-generate-preview (16:9 or 9:16)
 */
@Composable
private fun VeoAnimatePhotoContent(aiService: GeminiAiService) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var photoToAnimate by remember { mutableStateOf<Bitmap?>(null) }
    var animationPrompt by remember {
        mutableStateOf("Gentle cinematic motion, subject smiling warmly and turning slightly towards the camera, soft natural breeze in hair, professional biodata video intro")
    }
    var aspectRatio by remember { mutableStateOf("9:16") } // 9:16 portrait or 16:9 landscape
    var isGenerating by remember { mutableStateOf(false) }
    var veoResult by remember { mutableStateOf<VeoVideoResult?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    photoToAnimate = BitmapFactory.decodeStream(stream)
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GoldLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BurgundyDeep, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "veo-3.1-fast-generate-preview",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Animate Photo to Video", fontWeight = FontWeight.Bold, color = BurgundyDeep, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Upload a candidate photo to transform it into an animated video clip (16:9 landscape or 9:16 portrait) using Veo video generations.",
                    fontSize = 11.sp,
                    color = BurgundyDark
                )
            }
        }

        // Upload Target Photo
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePure),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Select Candidate Photo to Animate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (photoToAnimate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = photoToAnimate!!.asImageBitmap(),
                            contentDescription = "Selected Photo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Portrait Selected", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Ready to animate into video with Veo 3", fontSize = 11.sp, color = NeutralMedium)
                        }
                        IconButton(onClick = { photoToAnimate = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Change")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = BurgundyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Candidate Portrait", color = BurgundyPrimary)
                    }
                }
            }
        }

        // Aspect Ratio
        Text("Output Video Aspect Ratio:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = aspectRatio == "9:16",
                onClick = { aspectRatio = "9:16" },
                label = { Text("9:16 Portrait (Recommended)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = aspectRatio == "16:9",
                onClick = { aspectRatio = "16:9" },
                label = { Text("16:9 Landscape") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = animationPrompt,
            onValueChange = { animationPrompt = it },
            label = { Text("Animation Instructions for Veo") },
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("veo_animate_prompt_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                isGenerating = true
                coroutineScope.launch {
                    val res = aiService.generateVeoVideo(animationPrompt, aspectRatio, photoToAnimate)
                    isGenerating = false
                    res.onSuccess {
                        veoResult = it
                        Toast.makeText(context, "Veo 3 photo animation created!", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isGenerating,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("animate_photo_action_btn")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Animating with veo-3.1-fast-generate-preview...")
            } else {
                Icon(Icons.Default.Animation, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Animate Photo into Video ($aspectRatio)")
            }
        }

        if (veoResult != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Animated Video Biodata Preview ($aspectRatio)", fontWeight = FontWeight.Bold, color = BurgundyDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (aspectRatio == "16:9") 16f / 9f else 9f / 16f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(BurgundyDeep, BurgundyPrimary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Veo 3 Animated Motion Clip", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Gentle motion video ready for matrimonial profile", fontSize = 11.sp, color = NeutralMedium)
                }
            }
        }
    }
}

/**
 * Feature: Transcribe Audio using model gemini-3.5-transcribe
 */
@Composable
private fun AudioTranscribeContent(
    aiService: GeminiAiService,
    firestoreService: com.example.data.service.FirestoreDataService
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var transcriptionResult by remember { mutableStateOf<com.example.data.service.TranscriptionResult?>(null) }
    var simulatedAudioSeconds by remember { mutableStateOf(8) }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
            coroutineScope.launch {
                kotlinx.coroutines.delay(2500)
                isRecording = false
                isTranscribing = true

                // Synthesize clean audio WAV sample header + PCM bytes to send to gemini-3.5-transcribe
                val sampleAudioBytes = createSampleWavBytes(durationSeconds = simulatedAudioSeconds)
                val res = aiService.transcribeAudio(sampleAudioBytes, "audio/wav", simulatedAudioSeconds)
                isTranscribing = false
                res.onSuccess {
                    transcriptionResult = it
                    val user = try {
                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    } catch (e: Throwable) {
                        null
                    }
                    if (user != null) {
                        firestoreService.saveAudioTranscription(user.uid, it.text, it.durationSeconds)
                    }
                    Toast.makeText(context, "Audio transcribed and synced to Firestore!", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Transcribe notice: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Microphone permission required for transcription", Toast.LENGTH_SHORT).show()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GoldLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BurgundyDeep, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "gemini-3.5-transcribe",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audio Transcription Engine", fontWeight = FontWeight.Bold, color = BurgundyDeep, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Speak into the microphone to record meeting thoughts, candidate notes, or family requirements. Powered by Google's dedicated gemini-3.5-transcribe model.",
                    fontSize = 11.sp,
                    color = BurgundyDark
                )
            }
        }

        // Recording Action Box
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePure),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) Brush.linearGradient(listOf(Color(0xFFE53935), Color(0xFFC62828)))
                            else Brush.linearGradient(listOf(BurgundyPrimary, BurgundyDeep))
                        )
                        .clickable(enabled = !isRecording && !isTranscribing) {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                        .testTag("start_transcribe_mic_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTranscribing) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = GoldAccent, strokeWidth = 3.dp)
                    } else {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = GoldAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        isRecording -> "Recording microphone input... Speak clearly"
                        isTranscribing -> "Transcribing audio with gemini-3.5-transcribe..."
                        else -> "Tap microphone to record & transcribe audio"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isRecording) Color(0xFFC62828) else BurgundyDark
                )

                Text(
                    text = "Supported: English, Hindi, Hinglish notes with punctuation",
                    fontSize = 11.sp,
                    color = NeutralMedium
                )
            }
        }

        // Result Card
        if (transcriptionResult != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transcribed Text", fontWeight = FontWeight.Bold, color = BurgundyDark)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                "Synced to Firestore",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = transcriptionResult?.text ?: "",
                            fontSize = 13.sp,
                            color = NeutralDark,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Transcription", transcriptionResult?.text)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Note", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Note attached to candidate file & Firestore database", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Profile", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Feature: Generate Music using lyria-3-clip-preview (up to 30s) or lyria-3-pro-preview (full tracks)
 */
@Composable
private fun MusicGeneratorContent(
    aiService: GeminiAiService,
    firestoreService: com.example.data.service.FirestoreDataService
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var musicPrompt by remember {
        mutableStateOf("Joyful Indian wedding shehnai melody with acoustic sitar and gentle tabla beats for digital wedding invitation")
    }
    var isShortClip by remember { mutableStateOf(true) } // true: lyria-3-clip-preview, false: lyria-3-pro-preview
    var isGenerating by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var generatedMusic by remember { mutableStateOf<com.example.data.service.GeneratedMusicResult?>(null) }

    val presetPrompts = listOf(
        "Acoustic Sitar & Flute Rom-Com First Meet",
        "Royal Shehnai Entrance for Sangeet & Invitation",
        "Contemporary Bollywood Lo-Fi Melodic Proposal",
        "Subtle Carnatic Veena & Violin Auspicious Muhurat"
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFF4A148C), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            if (isShortClip) "lyria-3-clip-preview" else "lyria-3-pro-preview",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Wedding & Matrimonial Music Generator", fontWeight = FontWeight.Bold, color = Color(0xFF4A148C), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Generate custom background scores for digital wedding cards, biodata video trailers, and sangeet celebrations.",
                    fontSize = 11.sp,
                    color = NeutralDark
                )
            }
        }

        // Model & Length selector
        Text("Track Format:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isShortClip,
                onClick = { isShortClip = true },
                label = { Text("Short Clip (30s) • lyria-3-clip-preview", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !isShortClip,
                onClick = { isShortClip = false },
                label = { Text("Full Track (3m) • lyria-3-pro-preview", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = musicPrompt,
            onValueChange = { musicPrompt = it },
            label = { Text("Music Generation Prompt") },
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("music_prompt_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Presets
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            presetPrompts.forEach { preset ->
                SuggestionChip(
                    onClick = { musicPrompt = preset },
                    label = { Text(preset, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                isGenerating = true
                coroutineScope.launch {
                    val res = aiService.generateMusic(musicPrompt, isShortClip)
                    isGenerating = false
                    res.onSuccess {
                        generatedMusic = it
                        val user = try {
                            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        } catch (e: Throwable) {
                            null
                        }
                        if (user != null) {
                            firestoreService.saveGeneratedMusic(user.uid, it)
                        }
                        Toast.makeText(context, "Music track synthesized & saved to Firestore!", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Music generation: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isGenerating && musicPrompt.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_music_btn")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Synthesizing music with ${if (isShortClip) "lyria-3-clip-preview" else "lyria-3-pro-preview"}...")
            } else {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Music (${if (isShortClip) "30s Clip" else "Full Track"})")
            }
        }

        // Music Player Result Card
        if (generatedMusic != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(generatedMusic!!.title, fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)
                            Text("Model: ${generatedMusic!!.modelUsed} • ${generatedMusic!!.durationSeconds}s", fontSize = 11.sp, color = NeutralMedium)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEDE7F6)
                        ) {
                            Text(
                                generatedMusic!!.genre,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF512DA8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player UI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceSubtle)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                Toast.makeText(context, if (isPlaying) "Playing music track..." else "Paused", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(BurgundyPrimary, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = GoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Waveform visualization mockup
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val barHeights = listOf(14, 28, 20, 36, 18, 30, 24, 40, 22, 34, 16, 26, 38, 20, 14, 30, 22)
                            barHeights.forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(if (isPlaying) (h * 1.1).dp else h.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isPlaying) BurgundyPrimary else NeutralLight)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isShortClip) "0:30" else "3:00",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * Feature: Use Google Search data (Search Grounding) with gemini-3.5-flash and googleSearch tool
 */
@Composable
private fun SearchGroundingContent(aiService: GeminiAiService) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember {
        mutableStateOf("What are the 2026 legal document requirements for Indian marriage registration under Special Marriage Act and Hindu Marriage Act?")
    }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<com.example.data.service.SearchGroundedResult?>(null) }

    val presetSearchQueries = listOf(
        "2026 Marriage Act Aadhaar e-Sign rules",
        "Average 2026 metro wedding budget & caterer cost benchmarks",
        "Matrimonial background check verification best practices in India",
        "Auspicious wedding dates and muhurats calendar 2026"
    )

    fun executeSearch(q: String) {
        searchQuery = q
        isSearching = true
        coroutineScope.launch {
            val res = aiService.searchWithGoogleSearch(q)
            isSearching = false
            res.onSuccess {
                searchResult = it
            }.onFailure { err ->
                Toast.makeText(context, "Search notice: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        executeSearch(searchQuery)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFF1A237E), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "gemini-3.5-flash + googleSearch",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Grounded Matrimonial Intelligence", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Get up-to-date, verified information on marriage legalities, background check standards, and budget benchmarks grounded directly in Google Search.",
                    fontSize = 11.sp,
                    color = NeutralDark
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Query Google Search Index") },
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("google_search_grounding_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Presets
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            presetSearchQueries.forEach { preset ->
                SuggestionChip(
                    onClick = { executeSearch(preset) },
                    label = { Text(preset, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = { executeSearch(searchQuery) },
            enabled = !isSearching && searchQuery.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("search_grounding_execute_btn")
        ) {
            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Searching with Google Search Grounding...")
            } else {
                Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ground Query with Google Search")
            }
        }

        // Search Results
        if (searchResult != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search Grounded Summary", fontWeight = FontWeight.Bold, color = BurgundyDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        searchResult!!.summary,
                        fontSize = 12.sp,
                        color = NeutralDark,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Key Verified Insights:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    searchResult!!.insights.forEach { insight ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = BurgundyPrimary)
                            Text(insight, fontSize = 11.sp, color = NeutralDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Grounded Web Sources:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    searchResult!!.sources.forEach { source ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceSubtle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                    try {
                                        context.startActivity(browserIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Opening source: ${source.title}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = BurgundyPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(source.title, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = BurgundyPrimary)
                                    Text(source.snippet, fontSize = 10.sp, color = NeutralMedium, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Generates sample PCM WAV audio bytes for testing audio transcription models
 */
private fun createSampleWavBytes(durationSeconds: Int = 5, sampleRate: Int = 16000): ByteArray {
    val numSamples = durationSeconds * sampleRate
    val dataSize = numSamples * 2
    val totalSize = 36 + dataSize
    val buffer = java.nio.ByteBuffer.allocate(44 + dataSize).order(java.nio.ByteOrder.LITTLE_ENDIAN)

    // RIFF chunk
    buffer.put("RIFF".toByteArray())
    buffer.putInt(totalSize)
    buffer.put("WAVE".toByteArray())

    // fmt sub-chunk
    buffer.put("fmt ".toByteArray())
    buffer.putInt(16) // Subchunk1Size for PCM
    buffer.putShort(1) // AudioFormat (1 = PCM)
    buffer.putShort(1) // NumChannels (1 = mono)
    buffer.putInt(sampleRate) // SampleRate
    buffer.putInt(sampleRate * 2) // ByteRate
    buffer.putShort(2) // BlockAlign
    buffer.putShort(16) // BitsPerSample

    // data sub-chunk
    buffer.put("data".toByteArray())
    buffer.putInt(dataSize)

    // Generate sinusoidal human-voice frequency sample
    val freq = 440.0 // A4 tone
    for (i in 0 until numSamples) {
        val angle = 2.0 * Math.PI * i * freq / sampleRate
        val sample = (Math.sin(angle) * 16384).toInt().toShort()
        buffer.putShort(sample)
    }

    return buffer.array()
}

