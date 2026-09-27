package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PremSetuTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.PremSetuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            PremSetuApplication.initFirebaseSafely(application)
        } catch (e: Exception) {
            // Handled safely
        }
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PremSetuApp()
            }
        }
    }
}

@Composable
fun PremSetuApp(
    viewModel: PremSetuViewModel = viewModel()
) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val selectedMatchDetail by viewModel.selectedMatchForDetail.collectAsState()
    val trustScore by viewModel.trustScore.collectAsState()
    val showLoginScreen by viewModel.showLoginScreen.collectAsState()
    val showUserProfileScreen by viewModel.showUserProfileScreen.collectAsState()
    val showSearchScreen by viewModel.showSearchScreen.collectAsState()
    val showEmailCenterScreen by viewModel.showEmailCenterScreen.collectAsState()
    val showPrivacySettingsScreen by viewModel.showPrivacySettingsScreen.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Handle Back Press
    BackHandler(enabled = showPrivacySettingsScreen || showEmailCenterScreen || showSearchScreen || showUserProfileScreen || showLoginScreen || selectedMatchDetail != null || currentDestination != AppDestination.HOME) {
        if (showPrivacySettingsScreen) {
            viewModel.closePrivacySettingsScreen()
        } else if (showEmailCenterScreen) {
            viewModel.closeEmailCenterScreen()
        } else if (showSearchScreen) {
            viewModel.closeSearchScreen()
        } else if (showUserProfileScreen) {
            viewModel.closeUserProfileScreen()
        } else if (showLoginScreen) {
            viewModel.closeLoginScreen()
        } else if (selectedMatchDetail != null) {
            viewModel.closeMatchDetail()
        } else if (currentDestination != AppDestination.HOME) {
            viewModel.navigateTo(AppDestination.HOME)
        }
    }

    if (showPrivacySettingsScreen) {
        PrivacySettingsScreen(
            currentUserProfile = currentUserProfile,
            onBack = {
                viewModel.closePrivacySettingsScreen()
            },
            onSaveProfile = { updated ->
                viewModel.saveUserProfile(updated)
            },
            userProfileRepository = viewModel.userProfileRepository
        )
    } else if (showEmailCenterScreen) {
        com.example.ui.screens.EmailNotificationCenterScreen(
            viewModel = viewModel,
            onBack = {
                viewModel.closeEmailCenterScreen()
            }
        )
    } else if (showSearchScreen) {
        SearchScreen(
            onBack = {
                viewModel.closeSearchScreen()
            },
            userProfileRepository = viewModel.userProfileRepository,
            currentUserProfile = currentUserProfile,
            currentUserId = currentUser?.uid ?: "user_default_001",
            onUpdateUserProfile = { updated ->
                viewModel.saveUserProfile(updated)
            }
        )
    } else if (showUserProfileScreen) {
        UserProfileScreen(
            currentProfile = currentUserProfile,
            onSaveProfile = { updated ->
                viewModel.saveUserProfile(updated)
            },
            onBack = {
                viewModel.closeUserProfileScreen()
            },
            userProfileRepository = viewModel.userProfileRepository,
            onVerifyEmailClick = {
                viewModel.openEmailCenterScreen()
            },
            onOpenPrivacySettings = {
                viewModel.openPrivacySettingsScreen()
            },
            zohoEmailService = viewModel.zohoEmailService
        )
    } else if (showLoginScreen) {
        LoginScreen(
            onLoginSuccess = { user ->
                viewModel.onUserSignedIn(user)
            },
            onDismiss = {
                viewModel.closeLoginScreen()
            },
            viewModel = viewModel
        )
    } else if (selectedMatchDetail != null) {
        ProfileDetailScreen(
            match = selectedMatchDetail!!,
            viewModel = viewModel,
            onBack = { viewModel.closeMatchDetail() }
        )
    } else {
        Scaffold(
            topBar = {
                PremSetuTopAppBar(
                    title = currentDestination.title,
                    subtitle = when (currentDestination) {
                        AppDestination.HOME -> "Find Someone Who Complements Your Life"
                        AppDestination.DISCOVERY -> "Transparent Verified Recommendations"
                        AppDestination.JOURNEY -> "Digital Matrimonial Relationship OS"
                        AppDestination.FAMILY -> "Controlled Family Participation Desk"
                        AppDestination.CHAT -> "End-to-End Safe Connect"
                        AppDestination.MORE -> "Safety, Verification & CRM Engine"
                    },
                    trustScore = trustScore,
                    onTrustBadgeClick = {
                        viewModel.navigateTo(AppDestination.MORE)
                        viewModel.setMoreActiveSection("VERIFICATION")
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.openPrivacySettingsScreen() },
                            modifier = Modifier.testTag("top_bar_privacy_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Privacy Settings (Search & Contact Visibility)",
                                tint = BurgundyPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.openEmailCenterScreen() },
                            modifier = Modifier.testTag("top_bar_email_center_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "Zoho CPaaS Email & OTP",
                                tint = BurgundyPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.openSearchScreen() },
                            modifier = Modifier.testTag("top_bar_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Matches in Firestore",
                                tint = BurgundyPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.openUserProfileScreen() },
                            modifier = Modifier.testTag("top_bar_user_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Edit Profile & Biodata",
                                tint = BurgundyPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.openLoginScreen() },
                            modifier = Modifier.testTag("top_bar_account_auth_btn")
                        ) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                                contentDescription = if (currentUser != null) "Account Details" else "Sign In",
                                tint = if (currentUser != null) BurgundyPrimary else NeutralMedium
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("prem_setu_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.HOME,
                        onClick = { viewModel.navigateTo(AppDestination.HOME) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurgundyPrimary,
                            selectedTextColor = BurgundyDark,
                            indicatorColor = RoseBlush
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.DISCOVERY,
                        onClick = { viewModel.navigateTo(AppDestination.DISCOVERY) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.DISCOVERY) Icons.Filled.Search else Icons.Outlined.Search,
                                contentDescription = "Matches"
                            )
                        },
                        label = { Text("Matches", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurgundyPrimary,
                            selectedTextColor = BurgundyDark,
                            indicatorColor = RoseBlush
                        ),
                        modifier = Modifier.testTag("nav_item_matches")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.JOURNEY,
                        onClick = { viewModel.navigateTo(AppDestination.JOURNEY) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.JOURNEY) Icons.Filled.Timeline else Icons.Outlined.Timeline,
                                contentDescription = "Journey"
                            )
                        },
                        label = { Text("Journey", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurgundyPrimary,
                            selectedTextColor = BurgundyDark,
                            indicatorColor = RoseBlush
                        ),
                        modifier = Modifier.testTag("nav_item_journey")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.FAMILY,
                        onClick = { viewModel.navigateTo(AppDestination.FAMILY) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.FAMILY) Icons.Filled.Groups else Icons.Outlined.Groups,
                                contentDescription = "Family"
                            )
                        },
                        label = { Text("Family", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurgundyPrimary,
                            selectedTextColor = BurgundyDark,
                            indicatorColor = RoseBlush
                        ),
                        modifier = Modifier.testTag("nav_item_family")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.CHAT,
                        onClick = { viewModel.navigateTo(AppDestination.CHAT) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.CHAT) Icons.Filled.Chat else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat"
                            )
                        },
                        label = { Text("Chat", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurgundyPrimary,
                            selectedTextColor = BurgundyDark,
                            indicatorColor = RoseBlush
                        ),
                        modifier = Modifier.testTag("nav_item_chat")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.MORE,
                        onClick = { viewModel.navigateTo(AppDestination.MORE) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.MORE) Icons.Filled.Shield else Icons.Outlined.Shield,
                                contentDescription = "Safety"
                            )
                        },
                        label = { Text("Safety", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurgundyPrimary,
                            selectedTextColor = BurgundyDark,
                            indicatorColor = RoseBlush
                        ),
                        modifier = Modifier.testTag("nav_item_safety")
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestination.HOME -> HomeScreen(viewModel = viewModel)
                    AppDestination.DISCOVERY -> DiscoveryScreen(viewModel = viewModel)
                    AppDestination.JOURNEY -> JourneyScreen(viewModel = viewModel)
                    AppDestination.FAMILY -> FamilyConnectScreen(viewModel = viewModel)
                    AppDestination.CHAT -> ChatScreen(viewModel = viewModel)
                    AppDestination.MORE -> MoreHubScreen(viewModel = viewModel)
                }
            }
        }
    }
}
