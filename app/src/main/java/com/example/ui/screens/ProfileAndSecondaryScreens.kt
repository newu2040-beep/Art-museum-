package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Share
import com.example.util.ArtmuseumNotificationHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ARTMUSEUM_TOPICS
import com.example.data.model.ActivityNotification
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.Post
import com.example.data.model.UserBlock
import com.example.data.model.UserFollow
import com.example.data.model.UserProfile
import com.example.ui.components.ArtmuseumArtworkImage
import com.example.ui.components.ArtmuseumAvatar
import com.example.ui.components.ArtmuseumFilterChip
import com.example.ui.components.ArtmuseumIconButton
import com.example.ui.components.ArtmuseumOliveEmblem
import com.example.ui.components.ArtmuseumPostCard
import com.example.ui.components.ArtmuseumPrimaryButton
import com.example.ui.components.ArtmuseumSearchField
import com.example.ui.components.ArtmuseumSecondaryButton
import com.example.ui.components.ArtmuseumSegmentedControl
import com.example.ui.components.ArtmuseumThemeSwatch
import com.example.ui.components.VerifiedMuseumBadge
import com.example.ui.theme.ArtmuseumShapeTokens
import com.example.ui.theme.LocalArtmuseumColors
import com.example.ui.theme.LocalArtmuseumDimensions
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.WarmPaper
import com.example.ui.viewmodel.ArtmuseumViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    ownPosts: List<Post>,
    allPublicPosts: List<Post>,
    savedPostIds: Set<String>,
    blockedUsers: List<UserBlock>,
    activeTheme: ArtmuseumThemePalette,
    followersList: List<UserFollow> = emptyList(),
    followingList: List<UserFollow> = emptyList(),
    followingUserIds: Set<String> = emptySet(),
    isGuest: Boolean = false,
    onSignInWithGoogle: () -> Unit = {},
    onBackToHome: () -> Unit,
    onOpenPostDetail: (Post) -> Unit,
    onSaveProfileChanges: (
        displayName: String,
        bio: String,
        ageInput: String,
        gender: String,
        photoURL: String,
        bannerURL: String,
        themePalette: ArtmuseumThemePalette,
        notificationsEnabled: Boolean,
        profileVisibility: String,
        hideAge: Boolean,
        inviteCode: String
    ) -> Unit,
    onToggleFollowUser: (targetUid: String, targetName: String, targetPhotoURL: String) -> Unit = { _, _, _ -> },
    onFindUserByInviteCode: suspend (String) -> UserProfile? = { null },
    onOpenDirectChat: (peerUid: String, peerName: String, peerPhoto: String) -> Unit = { _, _, _ -> },
    onUnblockUser: (String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    BackHandler(onBack = onBackToHome)

    val colors = LocalArtmuseumColors.current
    val dimens = LocalArtmuseumDimensions.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentProfile = profile ?: UserProfile(
        displayName = if (isGuest) "Guest Explorer" else "Art Enthusiast",
        age = 0,
        gender = "Not specified",
        bio = if (isGuest) "Exploring real-time public exhibits in guest mode." else "Art enthusiast and collector.",
        photoURL = "",
        bannerURL = "",
        postCount = ownPosts.size,
        followerCount = followersList.size,
        followingCount = followingList.size,
        isVerified = false
    )

    var selectedSegment by remember { mutableIntStateOf(0) } // 0 = Posts, 1 = Saved
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showBlockedUsersSheet by remember { mutableStateOf(false) }
    var showFollowListSheet by remember { mutableStateOf<String?>(null) } // "followers" or "following"
    var showQRCodeDialog by remember { mutableStateOf(false) }
    var showConnectDialog by remember { mutableStateOf(false) }

    // Avatar photo picker
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val dataUri = ArtmuseumViewModel.encodeUriToCompactDataUri(context, uri)
                if (dataUri != null) {
                    onSaveProfileChanges(
                        currentProfile.displayName.ifBlank { "Sophia" },
                        currentProfile.bio.take(500),
                        currentProfile.age.coerceIn(13, 120).toString(),
                        if (currentProfile.gender == "Male") "Male" else "Female",
                        dataUri,
                        currentProfile.bannerURL.ifBlank { "preset:profile_header" },
                        activeTheme,
                        currentProfile.notificationsEnabled,
                        currentProfile.profileVisibility,
                        currentProfile.hideAge,
                        currentProfile.inviteCode
                    )
                    onShowMessage("Profile photo updated")
                }
            }
        }
    }

    // Banner photo picker
    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val dataUri = ArtmuseumViewModel.encodeUriToCompactDataUri(context, uri)
                if (dataUri != null) {
                    onSaveProfileChanges(
                        currentProfile.displayName.ifBlank { "Sophia" },
                        currentProfile.bio.take(500),
                        currentProfile.age.coerceIn(13, 120).toString(),
                        if (currentProfile.gender == "Male") "Male" else "Female",
                        currentProfile.photoURL.ifBlank { "preset:avatar_sophia" },
                        dataUri,
                        activeTheme,
                        currentProfile.notificationsEnabled,
                        currentProfile.profileVisibility,
                        currentProfile.hideAge,
                        currentProfile.inviteCode
                    )
                    onShowMessage("Museum banner updated")
                }
            }
        }
    }

    val savedExhibits = remember(allPublicPosts, ownPosts, savedPostIds) {
        (allPublicPosts + ownPosts)
            .distinctBy { it.postId }
            .filter { it.postId in savedPostIds }
    }

    val displayedGridPosts = if (selectedSegment == 0) {
        ownPosts
    } else {
        savedExhibits
    }

    val headerTotalHeight = if (dimens.isCompact) 220.dp else 260.dp
    val bannerArtworkHeight = if (dimens.isCompact) 160.dp else 200.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        // Top Banner + Avatar Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerTotalHeight)
        ) {
            // High-quality customizable botanical/artistic profile banner
            ArtmuseumArtworkImage(
                imageSource = currentProfile.bannerURL.ifBlank { "preset:profile_header" },
                contentDescription = "Botanical profile banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerArtworkHeight)
            )

            // Edit Banner Camera Pill Button (Top Right of Banner)
            Surface(
                onClick = {
                    bannerPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = ArtmuseumShapeTokens.Pill,
                color = Color.Black.copy(alpha = 0.52f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.6f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 56.dp, end = 16.dp)
                    .testTag("edit_banner_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Edit Banner",
                        tint = WarmPaper,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit Banner",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarmPaper,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Top bar icons: < Back on left, ⋯ Settings on right
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                ArtmuseumIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = onBackToHome,
                    testTag = "profile_back_button"
                )

                Box {
                    ArtmuseumIconButton(
                        icon = Icons.Filled.MoreHoriz,
                        contentDescription = "Profile settings",
                        onClick = { showSettingsMenu = true },
                        testTag = "profile_more_button"
                    )
                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false },
                        modifier = Modifier.background(colors.cardSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Profile & Theme") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                            onClick = {
                                showSettingsMenu = false
                                showEditProfileSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Museum QR Code") },
                            leadingIcon = { Icon(Icons.Outlined.QrCode, null) },
                            onClick = {
                                showSettingsMenu = false
                                showQRCodeDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Connect by Invite ID") },
                            leadingIcon = { Icon(Icons.Outlined.PersonAdd, null) },
                            onClick = {
                                showSettingsMenu = false
                                showConnectDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Test Push Notification") },
                            leadingIcon = { Icon(Icons.Outlined.NotificationsActive, null) },
                            onClick = {
                                showSettingsMenu = false
                                ArtmuseumNotificationHelper.sendNotification(
                                    context = context,
                                    title = "Museum Activity",
                                    message = "Sophia just liked your recent exhibit reflection! ✨",
                                    notificationId = 1002
                                )
                                onShowMessage("Test push notification sent!")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Blocked Accounts (${blockedUsers.size})") },
                            onClick = {
                                showSettingsMenu = false
                                showBlockedUsersSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Logout, null) },
                            onClick = {
                                showSettingsMenu = false
                                onSignOut()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Account", color = Color(0xFFB33A3A)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.DeleteForever,
                                    contentDescription = null,
                                    tint = Color(0xFFB33A3A)
                                )
                            },
                            onClick = {
                                showSettingsMenu = false
                                showDeleteAccountDialog = true
                            }
                        )
                    }
                }
            }

            // Rounded Card Sheet Backdrop starting smoothly under avatar center
            Surface(
                shape = ArtmuseumShapeTokens.LargeSheetTop,
                color = colors.cardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .align(Alignment.BottomCenter)
            ) {}

            // Centered Circular Avatar (102dp) + Camera Button
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                ArtmuseumAvatar(
                    photoURL = currentProfile.photoURL,
                    contentDescription = currentProfile.displayName,
                    size = if (dimens.isCompact) 82.dp else 102.dp,
                    borderWidth = if (dimens.isCompact) 2.5.dp else 3.5.dp,
                    borderColor = colors.cardSurface,
                    onClick = {
                        avatarPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                Surface(
                    onClick = {
                        avatarPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = CircleShape,
                    color = colors.cardSurface,
                    border = BorderStroke(1.dp, colors.border),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .testTag("change_avatar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Change profile photo",
                            tint = colors.inkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Main Profile Content Surface (Clean hierarchy with no overlapping)
        Surface(
            color = colors.cardSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            ) {
                // Name + Verified Checkmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentProfile.displayName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp
                        ),
                        color = colors.inkPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VerifiedMuseumBadge()
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Age · Gender (respects hideAge)
                val profileSubtitle = remember(currentProfile.hideAge, currentProfile.age, currentProfile.gender) {
                    if (!currentProfile.hideAge && currentProfile.age > 0) {
                        if (currentProfile.gender.isNotBlank()) "${currentProfile.age} · ${currentProfile.gender}" else "${currentProfile.age}"
                    } else {
                        currentProfile.gender.ifBlank { "Curator" }
                    }
                }
                Text(
                    text = profileSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = colors.mutedText
                )

                // Unique Invite ID & QR Code Action Pills
                val resolvedInviteCode = remember(currentProfile.inviteCode, currentProfile.uid) {
                    currentProfile.inviteCode.ifBlank { UserProfile.generateDefaultInviteCode(currentProfile.uid) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = ArtmuseumShapeTokens.Pill,
                        color = colors.subtleSurface,
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier.testTag("profile_invite_id_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "ID: $resolvedInviteCode",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = colors.inkPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("Artmuseum Invite ID", resolvedInviteCode)
                                    clipboard?.setPrimaryClip(clip)
                                    onShowMessage("Invite ID copied: $resolvedInviteCode")
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("copy_invite_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy invite code",
                                    tint = colors.pillSelectedFill,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = { showQRCodeDialog = true },
                        shape = ArtmuseumShapeTokens.Pill,
                        color = colors.subtleSurface,
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("profile_qr_code_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode,
                                contentDescription = "QR Code",
                                tint = colors.inkPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "QR Code",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = colors.inkPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bio
                Text(
                    text = currentProfile.bio.ifBlank {
                        "Just a girl who finds beauty in meaningful conversations, art, nature and real connections. 🌿"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    ),
                    color = colors.inkPrimary.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Row: Posts | Followers | Following
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileStatItem(
                        value = ownPosts.size.toString(),
                        label = "Posts"
                    )
                    ProfileStatItem(
                        value = followersList.size.toString(),
                        label = "Followers",
                        onClick = { showFollowListSheet = "followers" }
                    )
                    ProfileStatItem(
                        value = followingList.size.toString(),
                        label = "Following",
                        onClick = { showFollowListSheet = "following" }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isGuest) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.subtleSurface,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Browsing in Guest Mode",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.inkPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sign in with Google to backup your exhibits, personalize your name & avatar, and chat with creators.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.mutedText,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ArtmuseumPrimaryButton(
                                    text = "Sign In",
                                    onClick = onSignInWithGoogle,
                                    modifier = Modifier.weight(1f),
                                    testTag = "guest_profile_sign_in_button"
                                )
                                ArtmuseumSecondaryButton(
                                    text = "Exit Guest",
                                    onClick = onSignOut,
                                    modifier = Modifier.weight(1f),
                                    testTag = "guest_profile_exit_button"
                                )
                            }
                        }
                    }
                }

                // Action Buttons Row: [✏️ Edit Profile] [👥 Connect with ID]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ArtmuseumSecondaryButton(
                        text = "Edit Profile",
                        onClick = { showEditProfileSheet = true },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = colors.inkPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        testTag = "edit_profile_button"
                    )
                    ArtmuseumSecondaryButton(
                        text = "Connect ID",
                        onClick = { showConnectDialog = true },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = colors.inkPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        testTag = "connect_by_id_button"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Pill Tabs: [Posts] [Saved]
                ArtmuseumSegmentedControl(
                    options = listOf("Posts", "Saved"),
                    selectedIndex = selectedSegment,
                    onSelect = { selectedSegment = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2x2 Rounded Artwork Grid (Matches Screen 5 of mockup)
                if (displayedGridPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedSegment == 0) {
                                "No published exhibits yet."
                            } else {
                                "No saved exhibits yet. Tap the bookmark pill on any exhibit to save it."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.mutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val rows = displayedGridPosts.chunked(2)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rows.forEach { rowPosts ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowPosts.forEach { post ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(126.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onOpenPostDetail(post) }
                                            .testTag("profile_grid_post_${post.postId}")
                                    ) {
                                        ArtmuseumArtworkImage(
                                            imageSource = post.imageURL.ifBlank { "preset:coastal_villa" },
                                            contentDescription = post.text.take(40),
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                if (rowPosts.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showEditProfileSheet) {
        EditProfileModalSheet(
            profile = currentProfile,
            activeTheme = activeTheme,
            onDismiss = { showEditProfileSheet = false },
            onSave = { name, bio, ageStr, gender, photo, palette, notif, vis, hideAge, inviteCode ->
                onSaveProfileChanges(
                    name,
                    bio,
                    ageStr,
                    gender,
                    photo,
                    currentProfile.bannerURL,
                    palette,
                    notif,
                    vis,
                    hideAge,
                    inviteCode
                )
                showEditProfileSheet = false
            },
            onSignOut = {
                showEditProfileSheet = false
                onSignOut()
            },
            onRequestNotificationPermission = onShowMessage
        )
    }

    if (showQRCodeDialog) {
        ArtmuseumQRCodeDialog(
            user = currentProfile,
            onDismiss = { showQRCodeDialog = false },
            onShare = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Connect with me on Artmuseum! My Invite ID is: ${currentProfile.inviteCode.ifBlank { UserProfile.generateDefaultInviteCode(currentProfile.uid) }}\nLet's share quiet reflections and museum exhibits together. ✨"
                    )
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Museum Pass"))
            }
        )
    }

    if (showConnectDialog) {
        ConnectByInviteCodeDialog(
            onFindUser = onFindUserByInviteCode,
            isFollowing = { targetUid -> targetUid in followingUserIds },
            onToggleFollow = onToggleFollowUser,
            onMessageUser = { uid, name, photo ->
                showConnectDialog = false
                onOpenDirectChat(uid, name, photo)
            },
            onDismiss = { showConnectDialog = false }
        )
    }

    if (showFollowListSheet != null) {
        val sheetTitle = if (showFollowListSheet == "followers") "Followers" else "Following"
        val userList = if (showFollowListSheet == "followers") followersList else followingList
        FollowersFollowingSheet(
            title = sheetTitle,
            users = userList,
            followingUserIds = followingUserIds,
            onToggleFollow = onToggleFollowUser,
            onDismiss = { showFollowListSheet = null }
        )
    }

    if (showBlockedUsersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBlockedUsersSheet = false },
            containerColor = colors.cardSurface,
            shape = ArtmuseumShapeTokens.LargeSheetTop
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Blocked Accounts",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (blockedUsers.isEmpty()) {
                    Text(
                        text = "You haven't blocked any accounts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.mutedText
                    )
                } else {
                    blockedUsers.forEach { blocked ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = blocked.blockedName,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.inkPrimary
                            )
                            TextButton(onClick = { onUnblockUser(blocked.blockedUid) }) {
                                Text("Unblock")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = colors.cardSurface,
            title = { Text("Delete Artmuseum Account?") },
            text = {
                Text(
                    "This permanently deletes your profile and all exhibits you have published from Cloud Firestore."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete Forever", color = Color(0xFFB33A3A))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun formatStatCount(count: Int): String {
    return when {
        count >= 1000 -> String.format(Locale.US, "%.1fk", count / 1000f)
        else -> count.toString()
    }
}

@Composable
private fun ProfileStatItem(
    value: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalArtmuseumColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(onClick = onClick)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = colors.inkPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = colors.mutedText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersFollowingSheet(
    title: String,
    users: List<UserFollow>,
    followingUserIds: Set<String>,
    onToggleFollow: (targetUid: String, targetName: String, targetPhotoURL: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalArtmuseumColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        shape = ArtmuseumShapeTokens.LargeSheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (users.isEmpty()) {
                Text(
                    text = "No members in $title yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedText,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(
                        users,
                        key = { it.documentId.ifBlank { it.followingUid.ifBlank { it.followerUid } } }
                    ) { follow ->
                        val targetUid = follow.followingUid.ifBlank { follow.followerUid }
                        val isFollowing = targetUid in followingUserIds
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                ArtmuseumAvatar(
                                    photoURL = follow.targetPhotoURL,
                                    contentDescription = follow.targetName,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = follow.targetName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.inkPrimary
                                )
                            }
                            ArtmuseumFilterChip(
                                label = if (isFollowing) "Following" else "Follow",
                                selected = isFollowing,
                                onClick = {
                                    onToggleFollow(targetUid, follow.targetName, follow.targetPhotoURL)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModalSheet(
    profile: UserProfile,
    activeTheme: ArtmuseumThemePalette,
    onDismiss: () -> Unit,
    onSave: (
        displayName: String,
        bio: String,
        ageInput: String,
        gender: String,
        photoURL: String,
        themePalette: ArtmuseumThemePalette,
        notificationsEnabled: Boolean,
        profileVisibility: String,
        hideAge: Boolean,
        inviteCode: String
    ) -> Unit,
    onSignOut: () -> Unit,
    onRequestNotificationPermission: (String) -> Unit
) {
    val colors = LocalArtmuseumColors.current
    val context = LocalContext.current
    var displayName by remember { mutableStateOf(profile.displayName) }
    var bio by remember { mutableStateOf(profile.bio) }
    var ageInput by remember { mutableStateOf(profile.age.toString()) }
    var hideAge by remember { mutableStateOf(profile.hideAge) }
    var inviteCode by remember {
        mutableStateOf(profile.inviteCode.ifBlank { UserProfile.generateDefaultInviteCode(profile.uid) })
    }
    var gender by remember { mutableStateOf(if (profile.gender == "Male") "Male" else "Female") }
    var selectedPalette by remember { mutableStateOf(activeTheme) }
    var notificationsEnabled by remember { mutableStateOf(profile.notificationsEnabled) }
    var profileVisibility by remember { mutableStateOf(profile.profileVisibility) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        onRequestNotificationPermission(
            if (granted) "Activity notifications enabled" else "Notification permission declined"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        shape = ArtmuseumShapeTokens.LargeSheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Edit Museum Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it.take(60) },
                label = { Text("Display Name") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = ageInput,
                onValueChange = { input -> ageInput = input.filter { it.isDigit() }.take(3) },
                label = { Text("Age (13–120)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_age_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hide Age Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hide Age on Profile",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.inkPrimary
                    )
                    Text(
                        text = "Keep age private from your public profile and exhibit cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.mutedText
                    )
                }
                Switch(
                    checked = hideAge,
                    onCheckedChange = { hideAge = it },
                    modifier = Modifier.testTag("edit_hide_age_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Unique Invite ID
            OutlinedTextField(
                value = inviteCode,
                onValueChange = { input ->
                    inviteCode = input.uppercase(Locale.ROOT).filter { it.isLetterOrDigit() || it == '-' }.take(24)
                },
                label = { Text("Unique Invite ID (e.g. ART-XXXXXX)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_invite_code_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Gender",
                style = MaterialTheme.typography.labelLarge,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            ArtmuseumSegmentedControl(
                options = listOf("Female", "Male"),
                selectedIndex = if (gender == "Female") 0 else 1,
                onSelect = { index -> gender = if (index == 0) "Female" else "Male" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it.take(500) },
                label = { Text("Bio") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_bio_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dark Mode Quick Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                    Text(
                        text = "Switch gallery interface to dark theme",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.mutedText
                    )
                }
                Switch(
                    checked = selectedPalette.isDark,
                    onCheckedChange = { isDark ->
                        selectedPalette = if (isDark) ArtmuseumThemePalette.MIDNIGHT else ArtmuseumThemePalette.CREAM
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Museum Theme Palette",
                style = MaterialTheme.typography.labelLarge,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ArtmuseumThemePalette.entries) { palette ->
                    ArtmuseumThemeSwatch(
                        palette = palette,
                        selected = palette == selectedPalette,
                        onClick = { selectedPalette = palette }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Profile Visibility",
                style = MaterialTheme.typography.labelLarge,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            ArtmuseumSegmentedControl(
                options = listOf("Public", "Private"),
                selectedIndex = if (profileVisibility == "public") 0 else 1,
                onSelect = { idx -> profileVisibility = if (idx == 0) "public" else "private" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Contextual Notification Preference Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = colors.inkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Activity Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.inkPrimary
                        )
                        Text(
                            text = "Likes, reflections, and gallery updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.mutedText
                        )
                    }
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notificationsEnabled = enabled
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Push Notification Button
            Surface(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        ArtmuseumNotificationHelper.sendNotification(
                            context = context,
                            title = "Museum Activity",
                            message = "Sophia just liked your recent exhibit reflection! ✨",
                            notificationId = 1002
                        )
                        onRequestNotificationPermission("Test push notification sent!")
                    }
                },
                shape = ArtmuseumShapeTokens.Pill,
                color = colors.subtleSurface,
                border = BorderStroke(0.8.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("test_push_notification_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = colors.pillSelectedFill,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Push Notification",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = colors.inkPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            ArtmuseumPrimaryButton(
                text = "Save Profile",
                onClick = {
                    onSave(
                        displayName,
                        bio,
                        ageInput,
                        gender,
                        profile.photoURL,
                        selectedPalette,
                        notificationsEnabled,
                        profileVisibility,
                        hideAge,
                        inviteCode
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_profile_button"
            )

            Spacer(modifier = Modifier.height(10.dp))

            ArtmuseumSecondaryButton(
                text = "Sign Out",
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                testTag = "sign_out_button"
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun ExploreScreen(
    allPosts: List<Post>,
    publicUsers: List<UserProfile>,
    searchQuery: String,
    likedPostIds: Set<String>,
    savedPostIds: Set<String>,
    followingUserIds: Set<String> = emptySet(),
    onSearchQueryChange: (String) -> Unit,
    onOpenPostDetail: (Post) -> Unit,
    onToggleLike: (Post) -> Unit,
    onToggleSave: (String) -> Unit,
    onToggleFollowUser: ((targetUid: String, targetName: String, targetPhotoURL: String) -> Unit)? = null,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    val colors = LocalArtmuseumColors.current
    var selectedTopic by remember { mutableStateOf<String?>(null) }

    val matchingPosts = remember(allPosts, searchQuery, selectedTopic) {
        allPosts.filter { post ->
            val matchesTopic = selectedTopic == null ||
                post.topics.any { it.equals(selectedTopic, ignoreCase = true) }
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                post.text.contains(q, ignoreCase = true) ||
                post.authorNameSnapshot.contains(q, ignoreCase = true) ||
                post.topics.any { it.contains(q, ignoreCase = true) }
            matchesTopic && matchesQuery
        }
    }

    val matchingUsers = remember(publicUsers, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) emptyList()
        else publicUsers.filter {
            it.displayName.contains(q, ignoreCase = true) ||
                it.bio.contains(q, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Text(
                text = "Explore Museum",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = PlayfairDisplayFontFamily
                ),
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Discover perspectives, themes, and fellow storytellers",
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedText
            )
            Spacer(modifier = Modifier.height(12.dp))
            ArtmuseumSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search thoughts, topics, or people...",
                onClear = { onSearchQueryChange("") }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ArtmuseumFilterChip(
                    label = "All Themes",
                    selected = selectedTopic == null,
                    onClick = { selectedTopic = null }
                )
            }
            items(ARTMUSEUM_TOPICS) { topic ->
                ArtmuseumFilterChip(
                    label = topic,
                    selected = selectedTopic == topic,
                    onClick = { selectedTopic = if (selectedTopic == topic) null else topic }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (matchingUsers.isNotEmpty()) {
                item {
                    Text(
                        text = "People",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                }
                items(matchingUsers, key = { it.uid }) { user ->
                    val isFollowing = user.uid in followingUserIds
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.cardSurface,
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            ArtmuseumAvatar(
                                photoURL = user.photoURL,
                                contentDescription = user.displayName,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = "${user.age} · ${user.gender}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.mutedText
                                )
                            }
                            if (onToggleFollowUser != null) {
                                ArtmuseumFilterChip(
                                    label = if (isFollowing) "Following" else "Follow",
                                    selected = isFollowing,
                                    onClick = {
                                        onToggleFollowUser(user.uid, user.displayName, user.photoURL)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            items(matchingPosts, key = { it.postId }) { post ->
                ArtmuseumPostCard(
                    post = post,
                    isLiked = post.postId in likedPostIds,
                    isSaved = post.postId in savedPostIds,
                    onCardClick = { onOpenPostDetail(post) },
                    onLikeClick = { onToggleLike(post) },
                    onSaveClick = { onToggleSave(post.postId) },
                    onShareClick = {},
                    onMoreClick = { onOpenPostDetail(post) },
                    onAuthorClick = { onOpenPostDetail(post) }
                )
            }

            if (matchingPosts.isEmpty() && matchingUsers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ArtmuseumOliveEmblem(size = 40.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No exhibits matching \"$searchQuery\"" else "No exhibits published in this category yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.inkPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for a different keyword or explore all themes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.mutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPostsScreen(
    allPosts: List<Post>,
    savedPostIds: Set<String>,
    likedPostIds: Set<String>,
    onOpenPostDetail: (Post) -> Unit,
    onToggleLike: (Post) -> Unit,
    onToggleSave: (String) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    val colors = LocalArtmuseumColors.current
    val savedPosts = remember(allPosts, savedPostIds) {
        allPosts.filter { it.postId in savedPostIds }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = "Saved Collection",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = PlayfairDisplayFontFamily
                ),
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Exhibits and reflections bookmarked to your personal archive",
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedText
            )
        }

        if (savedPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ArtmuseumOliveEmblem(size = 48.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Saved Archive is Quiet",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the bookmark pill on any exhibit in the feed to keep it here across sessions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.mutedText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(savedPosts, key = { it.postId }) { post ->
                    ArtmuseumPostCard(
                        post = post,
                        isLiked = post.postId in likedPostIds,
                        isSaved = true,
                        onCardClick = { onOpenPostDetail(post) },
                        onLikeClick = { onToggleLike(post) },
                        onSaveClick = { onToggleSave(post.postId) },
                        onShareClick = {},
                        onMoreClick = { onOpenPostDetail(post) },
                        onAuthorClick = { onOpenPostDetail(post) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    notifications: List<ActivityNotification>,
    onDismiss: () -> Unit,
    onNotificationClick: (ActivityNotification) -> Unit
) {
    val colors = LocalArtmuseumColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        shape = ArtmuseumShapeTokens.LargeSheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Museum Activity",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (notifications.isEmpty()) {
                Text(
                    text = "No activity notifications yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedText,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                notifications.forEach { item ->
                    Surface(
                        onClick = { onNotificationClick(item) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (item.isRead) colors.cardSurface else colors.subtleSurface,
                        border = BorderStroke(0.6.dp, colors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            ArtmuseumAvatar(
                                photoURL = item.actorPhotoURL,
                                contentDescription = item.actorName,
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.actorName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = item.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.mutedText
                                )
                            }
                            if (!item.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.pillSelectedFill)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ArtmuseumQRCodeDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val colors = LocalArtmuseumColors.current
    val context = LocalContext.current
    val inviteCode = remember(user.inviteCode, user.uid) {
        user.inviteCode.ifBlank { UserProfile.generateDefaultInviteCode(user.uid) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                ArtmuseumOliveEmblem(size = 28.dp, showCircleMedallion = true)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Museum Pass & QR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = colors.inkPrimary
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    text = "Scan or share your unique curator code to connect and follow each other instantly.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = colors.mutedText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Canvas QR Code Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WarmPaper,
                    border = BorderStroke(1.dp, colors.border.copy(alpha = 0.8f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(200.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        val qrSeed = remember(inviteCode) { (inviteCode + user.uid).hashCode() }
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val w = size.width
                            val h = size.height
                            val cols = 15
                            val rows = 15
                            val cellW = w / cols
                            val cellH = h / rows
                            val darkColor = Color(0xFF2C2824)

                            fun drawFinder(startX: Float, startY: Float) {
                                drawRect(
                                    color = darkColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(startX, startY),
                                    size = androidx.compose.ui.geometry.Size(cellW * 7, cellH * 7)
                                )
                                drawRect(
                                    color = WarmPaper,
                                    topLeft = androidx.compose.ui.geometry.Offset(startX + cellW, startY + cellH),
                                    size = androidx.compose.ui.geometry.Size(cellW * 5, cellH * 5)
                                )
                                drawRect(
                                    color = darkColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(startX + cellW * 2, startY + cellH * 2),
                                    size = androidx.compose.ui.geometry.Size(cellW * 3, cellH * 3)
                                )
                            }

                            drawFinder(0f, 0f)
                            drawFinder(cellW * 8, 0f)
                            drawFinder(0f, cellH * 8)

                            val rng = java.util.Random(qrSeed.toLong())
                            for (r in 0 until rows) {
                                for (c in 0 until cols) {
                                    val inTopLeft = r < 7 && c < 7
                                    val inTopRight = r < 7 && c >= 8
                                    val inBottomLeft = r >= 8 && c < 7
                                    val inCenter = r in 6..8 && c in 6..8
                                    if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenter) {
                                        if (rng.nextBoolean()) {
                                            drawRoundRect(
                                                color = darkColor,
                                                topLeft = androidx.compose.ui.geometry.Offset(c * cellW + cellW * 0.12f, r * cellH + cellH * 0.12f),
                                                size = androidx.compose.ui.geometry.Size(cellW * 0.76f, cellH * 0.76f),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellW * 0.2f, cellH * 0.2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = WarmPaper,
                            border = BorderStroke(1.2.dp, Color(0xFFC5A880)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                ArtmuseumOliveEmblem(size = 20.dp, showCircleMedallion = false)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArtmuseumAvatar(
                        photoURL = user.photoURL,
                        contentDescription = user.displayName,
                        size = 28.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = colors.inkPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = ArtmuseumShapeTokens.Pill,
                    color = colors.subtleSurface,
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Artmuseum Invite ID", inviteCode)
                        clipboard?.setPrimaryClip(clip)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = inviteCode,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            ),
                            color = colors.pillSelectedFill
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = colors.pillSelectedFill,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            ArtmuseumPrimaryButton(
                text = "Share Pass",
                onClick = onShare,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = colors.pillSelectedText,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth().height(44.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close", color = colors.mutedText)
            }
        }
    )
}

@Composable
fun ConnectByInviteCodeDialog(
    onFindUser: suspend (String) -> UserProfile?,
    isFollowing: (String) -> Boolean,
    onToggleFollow: (targetUid: String, targetName: String, targetPhotoURL: String) -> Unit,
    onMessageUser: (peerUid: String, peerName: String, peerPhoto: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalArtmuseumColors.current
    val scope = rememberCoroutineScope()
    var inputCode by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchedUser by remember { mutableStateOf<UserProfile?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = "Connect via Invite ID",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlayfairDisplayFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.inkPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter another curator's unique invite code (e.g. ART-XXXXXX) to follow their exhibits and start a reflection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { input ->
                        inputCode = input.uppercase(Locale.ROOT).filter { it.isLetterOrDigit() || it == '-' }.take(24)
                        searchError = null
                    },
                    label = { Text("Invite ID Code") },
                    placeholder = { Text("ART-ELENA9") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("connect_invite_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                ArtmuseumSecondaryButton(
                    text = if (isSearching) "Searching..." else "Search Curator",
                    onClick = {
                        val trimmed = inputCode.trim()
                        if (trimmed.isNotBlank()) {
                            scope.launch {
                                isSearching = true
                                searchError = null
                                searchedUser = null
                                val user = onFindUser(trimmed)
                                isSearching = false
                                if (user != null) {
                                    searchedUser = user
                                } else {
                                    searchError = "No curator found with code \"$trimmed\". Check the code and try again."
                                }
                            }
                        }
                    },
                    enabled = inputCode.isNotBlank() && !isSearching,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    testTag = "search_invite_code_button"
                )

                if (searchError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = searchError ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFFC0392B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchedUser != null) {
                    val user = searchedUser!!
                    val following = isFollowing(user.uid)
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.subtleSurface,
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ArtmuseumAvatar(
                                    photoURL = user.photoURL,
                                    contentDescription = user.displayName,
                                    size = 46.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.inkPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerifiedMuseumBadge()
                                    }
                                    val userSubtitle = if (!user.hideAge && user.age > 0) "${user.age} · ${user.gender}" else user.gender.ifBlank { "Curator" }
                                    Text(
                                        text = userSubtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = colors.mutedText
                                    )
                                }
                            }

                            if (user.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = user.bio,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = colors.inkPrimary.copy(alpha = 0.85f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ArtmuseumPrimaryButton(
                                    text = if (following) "Following" else "Follow",
                                    onClick = {
                                        onToggleFollow(user.uid, user.displayName, user.photoURL)
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    testTag = "connect_follow_toggle_button"
                                )
                                ArtmuseumSecondaryButton(
                                    text = "Message",
                                    onClick = {
                                        onMessageUser(user.uid, user.displayName, user.photoURL)
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    testTag = "connect_message_button"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = colors.inkPrimary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorProfileSheet(
    creator: UserProfile,
    creatorPosts: List<Post>,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onMessageCreator: () -> Unit,
    onOpenPostDetail: (Post) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalArtmuseumColors.current
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        shape = ArtmuseumShapeTokens.LargeSheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            // Header Row: Avatar + Name + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ArtmuseumAvatar(
                    photoURL = creator.photoURL,
                    contentDescription = creator.displayName,
                    size = 56.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = creator.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        VerifiedMuseumBadge()
                    }
                    val subtitle = if (!creator.hideAge && creator.age > 0) {
                        "${creator.age} · ${creator.gender}"
                    } else {
                        creator.gender.ifBlank { "Curator" }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = colors.mutedText
                    )
                    val creatorCode = creator.inviteCode.ifBlank { UserProfile.generateDefaultInviteCode(creator.uid) }
                    Text(
                        text = "ID: $creatorCode",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = colors.pillSelectedFill,
                        modifier = Modifier.clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Artmuseum Invite ID", creatorCode)
                            clipboard?.setPrimaryClip(clip)
                        }
                    )
                }
            }

            if (creator.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = creator.bio,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
                    color = colors.inkPrimary.copy(alpha = 0.90f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: [Follow / Following] [Message]
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ArtmuseumPrimaryButton(
                    text = if (isFollowing) "Following" else "Follow",
                    onClick = onToggleFollow,
                    modifier = Modifier.weight(1f).height(44.dp),
                    testTag = "creator_profile_follow_button"
                )
                ArtmuseumSecondaryButton(
                    text = "Message",
                    onClick = {
                        onDismiss()
                        onMessageCreator()
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = colors.inkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    testTag = "creator_profile_message_button"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Exhibits (${creatorPosts.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (creatorPosts.isEmpty()) {
                Text(
                    text = "No exhibits published yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.mutedText,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val rows = creatorPosts.chunked(2)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rows.forEach { rowPosts ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowPosts.forEach { post ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            onDismiss()
                                            onOpenPostDetail(post)
                                        }
                                ) {
                                    ArtmuseumArtworkImage(
                                        imageSource = post.imageURL.ifBlank { "preset:coastal_villa" },
                                        contentDescription = post.text.take(30),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            if (rowPosts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
