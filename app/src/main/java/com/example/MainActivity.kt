package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.Post
import com.example.data.model.UserFollow
import com.example.data.repository.ArtmuseumRepository
import com.example.ui.auth.ArtmuseumIntroPermissionsDialog
import com.example.ui.auth.ArtmuseumWelcomeAuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutArtmuseum
import com.example.ui.components.ArtmuseumBottomNavigation
import com.example.ui.components.ArtmuseumTab
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.NotificationsBottomSheet
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedPostsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OlivePillActive
import com.example.ui.theme.WarmPaper
import com.example.ui.viewmodel.ArtmuseumViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtmuseumRootApp()
        }
    }
}

sealed interface ArtmuseumDestination {
    data class MainTab(val tab: ArtmuseumTab) : ArtmuseumDestination
    data class PostDetail(val postId: String) : ArtmuseumDestination
    data object CreatePost : ArtmuseumDestination
}

@Composable
fun ArtmuseumRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        MyApplicationTheme(palette = ArtmuseumThemePalette.CREAM) {
            ArtmuseumWelcomeAuthScreen(
                onAuthSuccess = { /* AuthStateListener updates currentUser automatically */ }
            )
        }
    } else {
        ArtmuseumAuthenticatedShell(
            currentUserId = user.uid,
            initialDisplayName = user.displayName ?: "Sophia"
        )
    }
}

@Composable
fun ArtmuseumAuthenticatedShell(
    currentUserId: String,
    initialDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: ArtmuseumViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val db = try {
                    val dbId = try { app.getString(R.string.firestore_database_id) } catch (_: Exception) { "" }
                    if (dbId.isNotBlank() && dbId != "(default)" && !dbId.startsWith("ai-studio-android-devpixel")) {
                        try {
                            FirebaseFirestore.getInstance(dbId)
                        } catch (_: Exception) {
                            FirebaseFirestore.getInstance()
                        }
                    } else {
                        FirebaseFirestore.getInstance()
                    }
                } catch (_: Exception) {
                    FirebaseFirestore.getInstance()
                }
                ArtmuseumViewModel(
                    repository = ArtmuseumRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName
                )
            }
        }
    )

    val activeTheme by viewModel.activeTheme.collectAsStateWithLifecycle()
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val likedPostIds by viewModel.likedPostIds.collectAsStateWithLifecycle()
    val savedPostIds by viewModel.savedPostIds.collectAsStateWithLifecycle()
    val followingUserIds by viewModel.followingUserIds.collectAsStateWithLifecycle()
    val blockedUsers by viewModel.blockedUsers.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val publicUsers by viewModel.publicUsers.collectAsStateWithLifecycle()
    val userOwnPosts by viewModel.userOwnPosts.collectAsStateWithLifecycle()
    val selectedPostComments by viewModel.selectedPostComments.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val followersList by viewModel.followersList.collectAsStateWithLifecycle()
    val followingList by viewModel.followingList.collectAsStateWithLifecycle()

    var currentDestination by remember {
        mutableStateOf<ArtmuseumDestination>(ArtmuseumDestination.MainTab(ArtmuseumTab.HOME))
    }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var reportTargetPost by remember { mutableStateOf<Post?>(null) }
    var reportReason by remember { mutableStateOf("") }

    val userProfile = (userProfileState as? UiState.Success)?.data
    val allFeedPosts = (feedState as? UiState.Success)?.data ?: emptyList()
    val unreadNotifications = remember(notifications) { notifications.count { !it.isRead } }

    val permissionsToRequest = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            add(Manifest.permission.CAMERA)
        }.toTypedArray()
    }

    var showShellPermissionsDialog by rememberSaveable { mutableStateOf(false) }
    var hasCheckedShellPermissions by rememberSaveable { mutableStateOf(false) }

    val shellPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        showShellPermissionsDialog = false
    }

    LaunchedEffect(Unit) {
        if (!hasCheckedShellPermissions) {
            hasCheckedShellPermissions = true
            val needsNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            } else false
            val needsStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            }
            if (needsNotif || needsStorage) {
                delay(600L)
                showShellPermissionsDialog = true
            }
        }
    }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            delay(2800L)
            viewModel.clearStatusBanner()
        }
    }

    MyApplicationTheme(palette = activeTheme) {
        val showBottomBar = currentDestination is ArtmuseumDestination.MainTab
        val activeTab = (currentDestination as? ArtmuseumDestination.MainTab)?.tab ?: ArtmuseumTab.HOME

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    ArtmuseumBottomNavigation(
                        currentTab = activeTab,
                        onTabSelected = { tab ->
                            currentDestination = ArtmuseumDestination.MainTab(tab)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() * 0f else 0.dp)
            ) {
                when (val dest = currentDestination) {
                    is ArtmuseumDestination.MainTab -> {
                        when (dest.tab) {
                            ArtmuseumTab.HOME -> {
                                HomeFeedScreen(
                                    feedState = feedState,
                                    userProfile = userProfile,
                                    selectedFilter = selectedFilter,
                                    likedPostIds = likedPostIds,
                                    savedPostIds = savedPostIds,
                                    unreadNotificationCount = unreadNotifications,
                                    onSelectFilter = viewModel::selectFilter,
                                    onOpenPostDetail = { post ->
                                        viewModel.observeCommentsForPost(post.postId)
                                        currentDestination = ArtmuseumDestination.PostDetail(post.postId)
                                    },
                                    onToggleLike = viewModel::toggleLike,
                                    onToggleSave = viewModel::toggleSave,
                                    onOpenCreatePost = {
                                        currentDestination = ArtmuseumDestination.CreatePost
                                    },
                                    onOpenSearch = {
                                        currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.EXPLORE)
                                    },
                                    onOpenNotifications = { showNotificationsSheet = true },
                                    onOpenProfile = {
                                        currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.PROFILE)
                                    },
                                    onReportPost = { post -> reportTargetPost = post },
                                    onBlockAuthor = { post ->
                                        viewModel.blockUser(post.authorId, post.authorNameSnapshot)
                                    },
                                    onDeleteOwnPost = { post ->
                                        viewModel.deletePost(post.postId)
                                    },
                                    currentUserId = currentUserId
                                )
                            }

                            ArtmuseumTab.EXPLORE -> {
                                ExploreScreen(
                                    allPosts = allFeedPosts,
                                    publicUsers = publicUsers,
                                    searchQuery = searchQuery,
                                    likedPostIds = likedPostIds,
                                    savedPostIds = savedPostIds,
                                    followingUserIds = followingUserIds,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onOpenPostDetail = { post ->
                                        viewModel.observeCommentsForPost(post.postId)
                                        currentDestination = ArtmuseumDestination.PostDetail(post.postId)
                                    },
                                    onToggleLike = viewModel::toggleLike,
                                    onToggleSave = viewModel::toggleSave,
                                    onToggleFollowUser = viewModel::toggleFollowUser,
                                    onBackToHome = {
                                        currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                                    }
                                )
                            }

                            ArtmuseumTab.SAVED -> {
                                SavedPostsScreen(
                                    allPosts = (allFeedPosts + userOwnPosts).distinctBy { it.postId },
                                    savedPostIds = savedPostIds,
                                    likedPostIds = likedPostIds,
                                    onOpenPostDetail = { post ->
                                        viewModel.observeCommentsForPost(post.postId)
                                        currentDestination = ArtmuseumDestination.PostDetail(post.postId)
                                    },
                                    onToggleLike = viewModel::toggleLike,
                                    onToggleSave = viewModel::toggleSave,
                                    onBackToHome = {
                                        currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                                    }
                                )
                            }

                            ArtmuseumTab.PROFILE -> {
                                ProfileScreen(
                                    profile = userProfile,
                                    ownPosts = userOwnPosts,
                                    allPublicPosts = allFeedPosts,
                                    savedPostIds = savedPostIds,
                                    blockedUsers = blockedUsers,
                                    activeTheme = activeTheme,
                                    followersList = followersList,
                                    followingList = followingList,
                                    onBackToHome = {
                                        currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                                    },
                                    onOpenPostDetail = { post ->
                                        viewModel.observeCommentsForPost(post.postId)
                                        currentDestination = ArtmuseumDestination.PostDetail(post.postId)
                                    },
                                    onSaveProfileChanges = { name, bio, ageStr, gender, photo, banner, palette, notif, vis ->
                                        viewModel.saveProfileChanges(
                                            displayName = name,
                                            bio = bio,
                                            ageInput = ageStr,
                                            gender = gender,
                                            photoURL = photo,
                                            bannerURL = banner,
                                            themePalette = palette,
                                            notificationsEnabled = notif,
                                            profileVisibility = vis,
                                            onSaved = {}
                                        )
                                    },
                                    onUnblockUser = viewModel::unblockUser,
                                    onSignOut = {
                                        signOutArtmuseum(
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope
                                        )
                                    },
                                    onDeleteAccount = {
                                        viewModel.deleteOwnAccount {
                                            signOutArtmuseum(
                                                credentialManager = credentialManager,
                                                onSignOutComplete = {},
                                                scope = scope
                                            )
                                        }
                                    },
                                    onShowMessage = viewModel::showBannerMessage
                                )
                            }
                        }
                    }

                    is ArtmuseumDestination.PostDetail -> {
                        val post = (allFeedPosts + userOwnPosts).firstOrNull { it.postId == dest.postId }
                        if (post != null) {
                            PostDetailScreen(
                                post = post,
                                comments = selectedPostComments,
                                isLiked = post.postId in likedPostIds,
                                isSaved = post.postId in savedPostIds,
                                isSubmittingComment = isSubmitting,
                                currentUserId = currentUserId,
                                onBack = {
                                    currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                                },
                                onToggleLike = { viewModel.toggleLike(post) },
                                onToggleSave = { viewModel.toggleSave(post.postId) },
                                onSendComment = { text ->
                                    viewModel.sendComment(post, text, onComplete = {})
                                },
                                onDeleteComment = { commentId ->
                                    viewModel.deleteComment(post.postId, commentId)
                                },
                                onReportPost = { reportTargetPost = post },
                                onDeletePost = {
                                    viewModel.deletePost(post.postId) {
                                        currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                                    }
                                }
                            )
                        } else {
                            currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                        }
                    }

                    is ArtmuseumDestination.CreatePost -> {
                        CreatePostScreen(
                            initialTheme = activeTheme,
                            isSubmitting = isSubmitting,
                            onBack = {
                                currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                            },
                            onPublish = { text, imageUrl, caption, topics, palette ->
                                viewModel.publishPost(
                                    text = text,
                                    imageURL = imageUrl,
                                    imageCaption = caption,
                                    topics = topics,
                                    themePalette = palette,
                                    onSuccess = {
                                        currentDestination = ArtmuseumDestination.MainTab(ArtmuseumTab.HOME)
                                    }
                                )
                            },
                            onThemeChanged = viewModel::selectTheme,
                            onShowMessage = viewModel::showBannerMessage
                        )
                    }
                }

                // Subtle Pill Toast / Status Banner
                AnimatedVisibility(
                    visible = statusBanner != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 56.dp, start = 24.dp, end = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = OlivePillActive,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = statusBanner.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = WarmPaper,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        if (showNotificationsSheet) {
            NotificationsBottomSheet(
                notifications = notifications,
                onDismiss = { showNotificationsSheet = false },
                onNotificationClick = { notif ->
                    viewModel.markNotificationRead(notif.notificationId)
                    showNotificationsSheet = false
                    if (notif.postId.isNotBlank()) {
                        viewModel.observeCommentsForPost(notif.postId)
                        currentDestination = ArtmuseumDestination.PostDetail(notif.postId)
                    }
                }
            )
        }

        if (reportTargetPost != null) {
            val postToReport = reportTargetPost!!
            AlertDialog(
                onDismissRequest = { reportTargetPost = null },
                title = { Text("Report Exhibit") },
                text = {
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it.take(300) },
                        label = { Text("Reason for reporting") },
                        placeholder = { Text("Spam, disrespect, or harmful content...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.reportContent(
                                targetId = postToReport.postId,
                                targetType = "post",
                                reason = reportReason.ifBlank { "Reported for review" }
                            )
                            reportReason = ""
                            reportTargetPost = null
                        }
                    ) {
                        Text("Submit Report")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reportTargetPost = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showShellPermissionsDialog) {
            ArtmuseumIntroPermissionsDialog(
                onDismiss = { showShellPermissionsDialog = false },
                onGrantRequested = {
                    shellPermissionsLauncher.launch(permissionsToRequest)
                }
            )
        }
    }
}
