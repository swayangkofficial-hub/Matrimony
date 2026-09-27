package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.GeminiAiService
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

data class LiveTurn(
    val sender: String, // "user" or "model"
    val text: String,
    val timestamp: String = "Just now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConversationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { GeminiAiService(context) }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "IN")
                isTtsReady = true
            }
        }
        tts = ttsInstance

        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val turns = remember {
        mutableStateListOf(
            LiveTurn(
                sender = "model",
                text = "Namaste! I am your Prem Setu Relationship & First Meeting Voice Coach, powered by Gemini 3.8 Live API. Tap the microphone to speak with me in real-time or pick a practice scenario below."
            )
        )
    }

    var isRecording by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var isSpeakingByBot by remember { mutableStateOf(false) }
    var speechInputText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Pulse animation for Live Voice
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording || isSpeakingByBot) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    fun speakText(text: String) {
        if (isTtsReady) {
            isSpeakingByBot = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LiveTtsID")
        }
    }

    fun submitUserVoiceMessage(message: String) {
        if (message.isBlank()) return
        turns.add(LiveTurn(sender = "user", text = message))
        speechInputText = ""
        isThinking = true

        coroutineScope.launch {
            listState.animateScrollToItem(turns.size - 1)
            val history = turns.map { it.sender to it.text }
            val result = aiService.sendLiveMessage(history, message)
            isThinking = false
            result.onSuccess { reply ->
                turns.add(LiveTurn(sender = "model", text = reply))
                speakText(reply)
                listState.animateScrollToItem(turns.size - 1)
            }.onFailure { err ->
                val fallback = "I heard you clearly. For first matrimonial meetings, authentic transparency and mutual values matter most. Let's practice your introduction together!"
                turns.add(LiveTurn(sender = "model", text = fallback))
                speakText(fallback)
            }
        }
    }

    // Speech Recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecording = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                submitUserVoiceMessage(spokenText)
            }
        }
    }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Prem Setu Live Voice Coach...")
            }
            try {
                isRecording = true
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                isRecording = false
            }
        }
    }

    fun startListening() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            isSpeakingByBot = false
        }
        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Live Voice Coach",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BurgundyDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(TrustGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "gemini-3.8-live",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                        Text(
                            "Real-time audio conversation for matrimonial confidence",
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
                actions = {
                    IconButton(onClick = {
                        if (tts?.isSpeaking == true) {
                            tts?.stop()
                            isSpeakingByBot = false
                        }
                    }) {
                        Icon(
                            imageVector = if (isSpeakingByBot) Icons.Default.VolumeUp else Icons.Outlined.VolumeUp,
                            contentDescription = "Speaker status",
                            tint = if (isSpeakingByBot) BurgundyPrimary else NeutralMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfacePure)
            )
        },
        bottomBar = {
            Surface(
                color = SurfacePure,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick Scenarios
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = { submitUserVoiceMessage("How should I introduce myself on a first matrimonial meeting?") },
                            label = { Text("Intro Icebreaker", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        SuggestionChip(
                            onClick = { submitUserVoiceMessage("What respectful questions can I ask about family and living arrangements?") },
                            label = { Text("Family Alignment", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text fallback input + Microphone Voice Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = speechInputText,
                            onValueChange = { speechInputText = it },
                            placeholder = { Text("Type or tap mic to speak...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("live_voice_text_input"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        if (speechInputText.isNotBlank()) {
                            IconButton(
                                onClick = { submitUserVoiceMessage(speechInputText) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(BurgundyPrimary, CircleShape)
                                    .testTag("live_voice_send_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                            }
                        } else {
                            // Live Voice Microphone Button with Pulse Animation
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            if (isRecording) listOf(Color(0xFFE53935), Color(0xFFC62828))
                                            else listOf(BurgundyPrimary, BurgundyDeep)
                                        )
                                    )
                                    .clickable { startListening() }
                                    .testTag("live_voice_mic_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Speak with Live API",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    if (isRecording) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Listening to your voice... Speak now",
                            color = Color(0xFFC62828),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (isThinking) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = BurgundyPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini 3.8 Live is processing...", fontSize = 11.sp, color = NeutralMedium)
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = BurgundyDeep)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Live Audio Pre-Meeting Practice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BurgundyDeep
                            )
                            Text(
                                "Practice what you will say before your first meeting. The Live model listens and gives immediate vocal feedback.",
                                fontSize = 11.sp,
                                color = BurgundyDark
                            )
                        }
                    }
                }
            }

            items(turns) { turn ->
                val isMe = turn.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (!isMe) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BurgundyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        color = if (isMe) BurgundyPrimary else SurfacePure,
                        shadowElevation = if (isMe) 0.dp else 1.dp,
                        border = if (isMe) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = turn.text,
                                color = if (isMe) Color.White else NeutralDark,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!isMe) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { speakText(turn.text) }
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Replay audio",
                                        tint = BurgundyPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Replay audio", fontSize = 10.sp, color = BurgundyPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
