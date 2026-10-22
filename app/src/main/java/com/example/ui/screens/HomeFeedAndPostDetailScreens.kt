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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
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
import com.example.ui.components.ArtmuseumTopicChip
import com.example.ui.components.PostEngagementPillRow
import com.example.ui.theme.ArtmuseumShapeTokens
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.LocalArtmuseumColors
import com.example.ui.theme.OlivePillActive
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.WarmPaper
import com.example.ui.viewmodel.UiState
import java.util.Locale

@Composable
fun HomeFeedScreen(
    feedState: UiState<List<Post>>,
    userProfile: UserProfile?,
    selectedFilter: String,
    likedPostIds: Set<String>,
    savedPostIds: Set<String>,
    unreadNotificationCount: Int,
    onSelectFilter: (String) -> Unit,
    onOpenPostDetail: (Post) -> Unit,
    onToggleLike: (Post) -> Unit,
    onToggleSave: (String) -> Unit,
    onOpenCreatePost: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onReportPost: (Post) -> Unit,
    onBlockAuthor: (Post) -> Unit,
    onDeleteOwnPost: (Post) -> Unit,
    currentUserId: String
) {
    val colors = LocalArtmuseumColors.current
    val context = LocalContext.current
    var activeMenuPost by remember { mutableStateOf<Post?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar (Matches Screen 2 of mockup: Circular Olive Emblem + "Artmuseum" serif + Search icon + Notifications + Profile Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                ArtmuseumOliveEmblem(
                    size = 34.dp,
                    showCircleMedallion = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Artmuseum",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    ),
                    color = colors.inkPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Search Icon Button
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search exhibits",
                    tint = colors.inkPrimary,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSearch)
                        .padding(8.dp)
                        .testTag("home_search_button")
                )

                // Notification Bell with Unread Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenNotifications)
                        .testTag("home_notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = colors.inkPrimary,
                        modifier = Modifier.size(21.dp)
                    )
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-7).dp, y = 7.dp)
                                .clip(CircleShape)
                                .background(HeartCrimson)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                ArtmuseumAvatar(
                    photoURL = userProfile?.photoURL ?: "preset:avatar_sophia",
                    contentDescription = "Your profile",
                    size = 34.dp,
                    onClick = onOpenProfile,
                    modifier = Modifier.testTag("home_avatar_button")
                )
            }

            // Horizontal Pill Filter Row: For You, Life, Love, Courage, Growth, Peace, Art
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
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
                        CircularProgressIndicator(color = colors.pillSelectedFill)
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
                    if (feedState.data.isEmpty()) {
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
                                    text = "No exhibits in '$selectedFilter' yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.inkPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Share a sincere thought or painting to begin this gallery.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.mutedText
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 6.dp,
                                bottom = 104.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("home_feed_list")
                        ) {
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
                                                onOpenPostDetail(post)
                                            }
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

        // Floating Circular Olive "+" FAB above bottom right (Matches Screen 2 of mockup)
        FloatingActionButton(
            onClick = onOpenCreatePost,
            shape = CircleShape,
            containerColor = OlivePillActive,
            contentColor = WarmPaper,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 84.dp)
                .size(52.dp)
                .testTag("fab_create_post")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create post",
                modifier = Modifier.size(26.dp)
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
    onSendComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onReportPost: () -> Unit,
    onDeletePost: () -> Unit
) {
    BackHandler(onBack = onBack)

    val colors = LocalArtmuseumColors.current
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 86.dp)
        ) {
            // Top Full-Bleed Artwork Hero (Matches Screen 3 of mockup with < button, overflow button, and "1/1" pill badge)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
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
                            size = 44.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = post.authorNameSnapshot,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = colors.inkPrimary
                            )
                            Text(
                                text = "${post.authorAgeSnapshot} · ${post.authorGenderSnapshot}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = colors.mutedText
                            )
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

                    // Comments List (Matches Arjun 2h / "This is so true. Beautifully said. ✨" + ♡ 48)
                    comments.forEach { comment ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            ArtmuseumAvatar(
                                photoURL = comment.authorPhotoSnapshot,
                                contentDescription = comment.authorNameSnapshot,
                                size = 34.dp
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
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Comment likes",
                                    tint = colors.mutedText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (comment.likeCount > 0) comment.likeCount.toString() else "48",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.mutedText
                                )
                                if (comment.authorId == currentUserId) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete comment",
                                        tint = colors.mutedText,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { onDeleteComment(comment.commentId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Anchored Pill Comment Composer (Matches Screen 3 of mockup)
        Surface(
            color = colors.cardSurface,
            border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.6f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                ArtmuseumCommentComposer(
                    value = commentText,
                    onValueChange = { commentText = it },
                    isSending = isSubmittingComment,
                    onSend = {
                        val toSend = commentText
                        commentText = ""
                        onSendComment(toSend)
                    }
                )
            }
        }
    }
}
