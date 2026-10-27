package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ARTMUSEUM_FEED_FILTERS
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.UserProfile
import com.example.ui.components.ArtmuseumArtworkImage
import com.example.ui.components.ArtmuseumAvatar
import com.example.ui.components.ArtmuseumCommentComposer
import com.example.ui.components.ArtmuseumFilterChip
import com.example.ui.components.ArtmuseumIconButton
import com.example.ui.components.ArtmuseumOliveEmblem
import com.example.ui.components.ArtmuseumPostCard
import com.example.ui.components.ArtmuseumPrimaryButton
import com.example.ui.components.ArtmuseumTopicChip
import com.example.ui.components.PostEngagementPillRow
import com.example.ui.theme.ArtmuseumShapeTokens
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.LocalArtmuseumColors
import com.example.ui.theme.LocalArtmuseumDimensions
import com.example.ui.theme.OlivePillActive
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.WarmPaper
import com.example.R
import com.example.ui.components.ArtmuseumScrapbookEmptyState
import com.example.ui.components.CornerPumpkin
import com.example.ui.components.PaperBackgroundOverlay
import com.example.ui.components.ScatteredDoodlesBackground
import com.example.ui.components.Sticker
import com.example.ui.components.VideoBanner
import com.example.ui.theme.CaveatFontFamily
import com.example.ui.viewmodel.UiState
import java.util.Locale

@Composable
fun HomeFeedScreen(
    feedState: UiState<List<Post>>,
    userProfile: UserProfile?,
    selectedFilter: String,
    likedPostIds: Set<String>,
    savedPostIds: Set<String>,
    followingUserIds: Set<String> = emptySet(),
    unreadNotificationCount: Int,
    unreadMessageCount: Int = 0,
    onSelectFilter: (String) -> Unit,
    onOpenPostDetail: (Post) -> Unit,
    onToggleLike: (Post) -> Unit,
    onToggleSave: (String) -> Unit,
    onToggleFollowUser: (targetUid: String, targetName: String, targetPhotoURL: String) -> Unit = { _, _, _ -> },
    onOpenCreatePost: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDirectMessages: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCreatorProfile: (authorId: String, authorName: String, authorPhoto: String) -> Unit = { _, _, _ -> },
    onReportPost: (Post) -> Unit,
    onBlockAuthor: (Post) -> Unit,
    onDeleteOwnPost: (Post) -> Unit,
    currentUserId: String,
    isGuest: Boolean = false,
    onGuestPrompt: () -> Unit = {}
) {
    val colors = LocalArtmuseumColors.current
    val dimens = LocalArtmuseumDimensions.current
    val context = LocalContext.current
    var activeMenuPost by remember { mutableStateOf<Post?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Paper texture at low opacity
        PaperBackgroundOverlay(opacity = 0.12f)

        // Pumpkin partially visible at a screen corner
        CornerPumpkin(
            modifier = Modifier.align(Alignment.TopStart),
            size = 110.dp,
            alignment = Alignment.TopStart
        )

        // Doodles scattered at 30% opacity behind content
        ScatteredDoodlesBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Responsive Top Bar (Adapts cleanly to small and standard screens)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimens.horizontalPadding,
                        vertical = if (dimens.isCompact) 6.dp else 10.dp
                    )
            ) {
                // Header Corner Decoration: Yellow Star Sticker
                Sticker(
                    drawableRes = R.drawable.st_star_yellow,
                    size = if (dimens.isCompact) 28.dp else 34.dp,
                    rotation = -14f,
                    enableWobble = true
                )
                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Artmuseum",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = CaveatFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (dimens.isCompact) 26.sp else 30.sp
                        ),
                        color = colors.inkPrimary
                    )
                    if (isGuest) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = ArtmuseumShapeTokens.Pill,
                            color = colors.pillSelectedFill.copy(alpha = 0.15f),
                            border = BorderStroke(0.6.dp, colors.pillSelectedFill.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(ArtmuseumShapeTokens.Pill)
                                .clickable(onClick = onGuestPrompt)
                        ) {
                            Text(
                                text = "Guest",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (dimens.isCompact) 9.5.sp else 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.pillSelectedFill,
                                modifier = Modifier.padding(
                                    horizontal = if (dimens.isCompact) 6.dp else 8.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }

                val iconBtnSize = if (dimens.isCompact) 32.dp else 38.dp
                val iconGlyphSize = if (dimens.isCompact) 18.dp else 20.dp

                // Search Icon Button
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search exhibits",
                    tint = colors.inkPrimary,
                    modifier = Modifier
                        .size(iconBtnSize)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSearch)
                        .padding(if (dimens.isCompact) 5.dp else 8.dp)
                        .testTag("home_search_button")
                )

                // Direct Messages Icon Button with Unread Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconBtnSize)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenDirectMessages)
                        .testTag("home_direct_messages_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Direct Messages",
                        tint = colors.inkPrimary,
                        modifier = Modifier.size(iconGlyphSize)
                    )
                    if (unreadMessageCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(HeartCrimson)
                        )
                    }
                }

                // Notification Bell with Unread Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(iconBtnSize)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenNotifications)
                        .testTag("home_notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = colors.inkPrimary,
                        modifier = Modifier.size(iconGlyphSize)
                    )
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(HeartCrimson)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(if (dimens.isCompact) 4.dp else 6.dp))

                ArtmuseumAvatar(
                    photoURL = userProfile?.photoURL ?: "preset:avatar_sophia",
                    contentDescription = "Your profile",
                    size = dimens.avatarSizeSmall,
                    onClick = onOpenProfile,
                    modifier = Modifier.testTag("home_avatar_button")
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Header Corner Decoration: Apple Sticker
                Sticker(
                    drawableRes = R.drawable.st_apple,
                    size = if (dimens.isCompact) 26.dp else 30.dp,
                    rotation = 12f,
                    enableWobble = true
                )
            }

            // Horizontal Pill Filter Row: For You, Life, Love, Courage, Growth, Peace, Art
            LazyRow(
                contentPadding = PaddingValues(horizontal = dimens.horizontalPadding, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ARTMUSEUM_FEED_FILTERS) { filter ->
                    ArtmuseumFilterChip(
                        label = filter,
                        selected = filter == selectedFilter,
                        onClick = { onSelectFilter(filter) },
                        testTag = "filter_chip_${filter.lowercase(Locale.ROOT).replace(" ", "_")}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (feedState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Sticker(
                                drawableRes = R.drawable.st_apple,
                                size = 56.dp,
                                enableBounce = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Gathering exhibits...",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CaveatFontFamily,
                                    fontSize = 20.sp
                                ),
                                color = colors.inkPrimary
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = feedState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.mutedText
                        )
                    }
                }

                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = dimens.horizontalPadding,
                            end = dimens.horizontalPadding,
                            top = 4.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(if (dimens.isCompact) 10.dp else 14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home_feed_list")
                    ) {
                        item(key = "home_video_banner") {
                            VideoBanner(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_video_banner")
                            )
                        }

                        if (feedState.data.isEmpty()) {
                            item(key = "home_empty_state") {
                                ArtmuseumScrapbookEmptyState(
                                    title = if (selectedFilter == "For You") "The Scrapbook Gallery is Open" else "No Exhibits in '$selectedFilter'",
                                    message = "Every exhibit here is pinned by visitors in real time. Tap the candy below to publish the first artwork or autumn thought.",
                                    useCharacter = true,
                                    actionButtonText = "Publish First Exhibit",
                                    onActionClick = onOpenCreatePost
                                )
                            }
                        } else {
                            items(feedState.data, key = { it.postId }) { post ->
                                Box {
                                    ArtmuseumPostCard(
                                        post = post,
                                        isLiked = post.postId in likedPostIds,
                                        isSaved = post.postId in savedPostIds,
                                        onCardClick = { onOpenPostDetail(post) },
                                        onLikeClick = { onToggleLike(post) },
                                        onSaveClick = { onToggleSave(post.postId) },
                                        onShareClick = {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "${post.authorNameSnapshot} on Artmuseum:\n\"${post.text}\""
                                                )
                                            }
                                            context.startActivity(
                                                Intent.createChooser(sendIntent, "Share Exhibit")
                                            )
                                        },
                                        onMoreClick = { activeMenuPost = post },
                                        onAuthorClick = {
                                            if (post.authorId == currentUserId) {
                                                onOpenProfile()
                                            } else {
                                                onOpenCreatorProfile(post.authorId, post.authorNameSnapshot, post.authorPhotoSnapshot)
                                            }
                                        },
                                        showFollowButton = post.authorId.isNotBlank() && post.authorId != currentUserId,
                                        isFollowingAuthor = post.authorId in followingUserIds,
                                        onToggleFollowAuthor = {
                                            onToggleFollowUser(post.authorId, post.authorNameSnapshot, post.authorPhotoSnapshot)
                                        }
                                    )

                                    DropdownMenu(
                                        expanded = activeMenuPost?.postId == post.postId,
                                        onDismissRequest = { activeMenuPost = null },
                                        modifier = Modifier.background(colors.cardSurface)
                                    ) {
                                        if (post.authorId == currentUserId) {
                                            DropdownMenuItem(
                                                text = { Text("Delete Exhibit") },
                                                leadingIcon = {
                                                    Icon(Icons.Filled.DeleteOutline, contentDescription = null)
                                                },
                                                onClick = {
                                                    activeMenuPost = null
                                                    onDeleteOwnPost(post)
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Report Exhibit") },
                                            leadingIcon = {
                                                Icon(Icons.Filled.Flag, contentDescription = null)
                                            },
                                            onClick = {
                                                activeMenuPost = null
                                                onReportPost(post)
                                            }
                                        )
                                        if (post.authorId != currentUserId) {
                                            DropdownMenuItem(
                                                text = { Text("Block ${post.authorNameSnapshot}") },
                                                leadingIcon = {
                                                    Icon(Icons.Outlined.Block, contentDescription = null)
                                                },
                                                onClick = {
                                                    activeMenuPost = null
                                                    onBlockAuthor(post)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Circular FAB with candy sticker
        FloatingActionButton(
            onClick = onOpenCreatePost,
            shape = CircleShape,
            containerColor = colors.autumnPrimary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = if (dimens.isCompact) 14.dp else 20.dp,
                    bottom = if (dimens.isCompact) 16.dp else 22.dp
                )
                .size(if (dimens.isCompact) 48.dp else 56.dp)
                .testTag("fab_create_post")
        ) {
            Sticker(
                drawableRes = R.drawable.st_candy,
                contentDescription = "Create post",
                size = if (dimens.isCompact) 28.dp else 34.dp,
                enableWobble = true
            )
        }
    }
}

@Composable
fun PostDetailScreen(
    post: Post,
    comments: List<PostComment>,
    isLiked: Boolean,
    isSaved: Boolean,
    isSubmittingComment: Boolean,
    currentUserId: String,
    onBack: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onSendComment: (text: String, parentCommentId: String?, replyToAuthor: String?) -> Unit,
    onDeleteComment: (String) -> Unit,
    onToggleCommentLike: (String) -> Unit = {},
    onMessageAuthor: (peerUid: String, peerName: String, peerPhoto: String) -> Unit = { _, _, _ -> },
    onOpenCreatorProfile: (authorId: String, authorName: String, authorPhoto: String) -> Unit = { _, _, _ -> },
    isFollowingAuthor: Boolean = false,
    onToggleFollowAuthor: () -> Unit = {},
    onReportPost: () -> Unit,
    onDeletePost: () -> Unit
) {
    BackHandler(onBack = onBack)

    val colors = LocalArtmuseumColors.current
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<PostComment?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        PaperBackgroundOverlay(opacity = 0.10f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 86.dp)
        ) {
            // Top Full-Bleed Artwork Hero (Adapts dynamically to natural post aspect ratio)
            val heroRatioModifier = remember(post.aspectRatio) {
                when (post.aspectRatio.substringBefore("_")) {
                    "4:5" -> Modifier.aspectRatio(4f / 5f)
                    "16:9" -> Modifier.aspectRatio(16f / 9f)
                    "3:4" -> Modifier.aspectRatio(3f / 4f)
                    "1:1" -> Modifier.aspectRatio(1f)
                    else -> Modifier.height(320.dp)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(heroRatioModifier)
            ) {
                ArtmuseumArtworkImage(
                    imageSource = post.imageURL.ifBlank { "preset:coastal_villa" },
                    contentDescription = post.imageCaption.ifBlank { "Exhibit artwork" },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top controls overlay
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
                        onClick = onBack,
                        testTag = "detail_back_button"
                    )

                    Box {
                        ArtmuseumIconButton(
                            icon = Icons.Filled.MoreHoriz,
                            contentDescription = "More options",
                            onClick = { showOverflowMenu = true },
                            testTag = "detail_more_button"
                        )
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            modifier = Modifier.background(colors.cardSurface)
                        ) {
                            if (post.authorId == currentUserId) {
                                DropdownMenuItem(
                                    text = { Text("Delete Exhibit") },
                                    leadingIcon = { Icon(Icons.Filled.DeleteOutline, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onDeletePost()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Report Exhibit") },
                                leadingIcon = { Icon(Icons.Filled.Flag, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onReportPost()
                                }
                            )
                        }
                    }
                }

                // "1/1" Pill indicator in top right of artwork matching Screen 3 of mockup
                Surface(
                    shape = ArtmuseumShapeTokens.Pill,
                    color = Color(0xFF3A352F).copy(alpha = 0.62f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 60.dp, end = 18.dp)
                ) {
                    Text(
                        text = "1/1",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarmPaper,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Overlapping Rounded Card Content Surface (Matches Screen 3 of mockup)
            Surface(
                shape = ArtmuseumShapeTokens.LargeSheetTop,
                color = colors.cardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Author Row: Avatar + Sophia / 23 · Female
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ArtmuseumAvatar(
                            photoURL = post.authorPhotoSnapshot,
                            contentDescription = post.authorNameSnapshot,
                            size = 44.dp,
                            onClick = {
                                if (post.authorId != currentUserId) {
                                    onOpenCreatorProfile(post.authorId, post.authorNameSnapshot, post.authorPhotoSnapshot)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.clickable {
                                if (post.authorId != currentUserId) {
                                    onOpenCreatorProfile(post.authorId, post.authorNameSnapshot, post.authorPhotoSnapshot)
                                }
                            }
                        ) {
                            Text(
                                text = post.authorNameSnapshot,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = colors.inkPrimary
                            )
                            val authorSubtitle = remember(post.authorAgeSnapshot, post.authorGenderSnapshot) {
                                if (post.authorAgeSnapshot > 0) {
                                    if (post.authorGenderSnapshot.isNotBlank()) "${post.authorAgeSnapshot} · ${post.authorGenderSnapshot}" else "${post.authorAgeSnapshot}"
                                } else {
                                    post.authorGenderSnapshot.ifBlank { "Curator" }
                                }
                            }
                            Text(
                                text = authorSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = colors.mutedText
                            )
                        }

                        if (post.authorId != currentUserId) {
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                onClick = onToggleFollowAuthor,
                                shape = ArtmuseumShapeTokens.Pill,
                                color = if (isFollowingAuthor) colors.subtleSurface else colors.pillSelectedFill,
                                border = BorderStroke(
                                    0.8.dp,
                                    if (isFollowingAuthor) colors.border else colors.pillSelectedFill
                                ),
                                modifier = Modifier.testTag("post_detail_follow_author_button")
                            ) {
                                Text(
                                    text = if (isFollowingAuthor) "Following" else "Follow",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isFollowingAuthor) colors.inkPrimary else colors.pillSelectedText
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = ArtmuseumShapeTokens.Pill,
                                color = colors.subtleSurface,
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .clip(ArtmuseumShapeTokens.Pill)
                                    .clickable {
                                        onMessageAuthor(
                                            post.authorId,
                                            post.authorNameSnapshot,
                                            post.authorPhotoSnapshot
                                        )
                                    }
                                    .testTag("post_detail_message_author_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = colors.inkPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Message",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = colors.inkPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full long-form post text
                    Text(
                        text = post.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            lineHeight = 23.sp
                        ),
                        color = colors.inkPrimary
                    )

                    if (post.imageCaption.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = post.imageCaption,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.mutedText
                        )
                    }

                    if (post.topics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            post.topics.forEach { topic ->
                                ArtmuseumTopicChip(
                                    label = topic,
                                    selected = false,
                                    onClick = {}
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Engagement Pill Row: [❤️ 1.2k] [Bookmark] [Share]
                    PostEngagementPillRow(
                        likeCount = post.likeCount,
                        isLiked = isLiked,
                        isSaved = isSaved,
                        onLikeClick = onToggleLike,
                        onSaveClick = onToggleSave,
                        onShareClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${post.authorNameSnapshot} on Artmuseum:\n\"${post.text}\""
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Exhibit"))
                        },
                        postId = post.postId
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.65f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Comments List with Replies Support
                    val rootComments = comments.filter { it.parentCommentId.isNullOrBlank() }
                    val repliesByParent = comments.filter { !it.parentCommentId.isNullOrBlank() }
                        .groupBy { it.parentCommentId!! }

                    val sortedComments = buildList {
                        rootComments.forEach { root ->
                            add(root)
                            repliesByParent[root.commentId]?.forEach { reply ->
                                add(reply)
                            }
                        }
                        // Any remaining replies whose parent isn't in root list
                        comments.filter { !it.parentCommentId.isNullOrBlank() && it.parentCommentId !in rootComments.map { r -> r.commentId } }.forEach {
                            if (it !in this) add(it)
                        }
                    }

                    if (sortedComments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments yet. Share your reflection below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.mutedText
                            )
                        }
                    }

                    sortedComments.forEach { comment ->
                        val isReply = !comment.parentCommentId.isNullOrBlank()
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = if (isReply) 28.dp else 0.dp,
                                    top = 6.dp,
                                    bottom = 6.dp
                                )
                                .testTag("comment_${comment.commentId}")
                        ) {
                            ArtmuseumAvatar(
                                photoURL = comment.authorPhotoSnapshot,
                                contentDescription = comment.authorNameSnapshot,
                                size = if (isReply) 28.dp else 34.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.authorNameSnapshot,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = colors.inkPrimary
                                    )
                                    if (isReply && !comment.replyToAuthor.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "↳ @${comment.replyToAuthor}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = colors.pillSelectedFill
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "2h",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = colors.mutedText
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                    color = colors.inkPrimary.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Reply Action Button
                                    Text(
                                        text = "Reply",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = colors.pillSelectedFill,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                replyingToComment = comment
                                            }
                                            .testTag("reply_button_${comment.commentId}")
                                    )

                                    if (comment.authorId == currentUserId) {
                                        Text(
                                            text = "Delete",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.5.sp
                                            ),
                                            color = colors.mutedText,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable { onDeleteComment(comment.commentId) }
                                                .testTag("delete_comment_${comment.commentId}")
                                        )
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleCommentLike(comment.commentId) }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (comment.likeCount > 0) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Comment likes",
                                    tint = if (comment.likeCount > 0) HeartCrimson else colors.mutedText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (comment.likeCount > 0) comment.likeCount.toString() else "48",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.mutedText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Anchored Pill Comment Composer (Matches Screen 3 of mockup with Reply Bar)
        Surface(
            color = colors.cardSurface,
            border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.6f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // If replying to a comment, show active replying banner
                if (replyingToComment != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.subtleSurface)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Replying to ",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = colors.mutedText
                        )
                        Text(
                            text = "@${replyingToComment!!.authorNameSnapshot}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = colors.pillSelectedFill,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancel reply",
                            tint = colors.mutedText,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { replyingToComment = null }
                                .testTag("cancel_reply_banner_button")
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                ArtmuseumCommentComposer(
                    value = commentText,
                    onValueChange = { commentText = it },
                    isSending = isSubmittingComment,
                    onSend = {
                        val toSend = commentText
                        val parentId = replyingToComment?.commentId
                        val replyAuthor = replyingToComment?.authorNameSnapshot
                        commentText = ""
                        replyingToComment = null
                        onSendComment(toSend, parentId, replyAuthor)
                    }
                )
            }
        }
    }
}
