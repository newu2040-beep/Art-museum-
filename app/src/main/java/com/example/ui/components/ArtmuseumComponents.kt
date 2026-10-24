package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.Post
import com.example.ui.theme.AgedGold
import com.example.ui.theme.ArtmuseumShapeTokens
import com.example.ui.theme.GallerySage
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.LocalArtmuseumColors
import com.example.ui.theme.LocalArtmuseumDimensions
import com.example.ui.theme.MuseumBorder
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.OlivePillActive
import com.example.ui.theme.VerifiedBadgeTaupe
import com.example.ui.theme.WarmPaper
import java.util.Locale

@Composable
fun ArtmuseumOliveEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    showCircleMedallion: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showCircleMedallion) {
                    Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFEDE5D5))
                        .border(1.dp, Color(0xFFDAD0BC), CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.76f)) {
            val w = this.size.width
            val h = this.size.height
            // Olive stem
            val stemPath = Path().apply {
                moveTo(w * 0.22f, h * 0.82f)
                cubicTo(w * 0.38f, h * 0.60f, w * 0.56f, h * 0.40f, w * 0.80f, h * 0.20f)
            }
            drawPath(
                path = stemPath,
                color = Color(0xFF5A624C),
                style = Stroke(width = w * 0.065f, cap = StrokeCap.Round)
            )
            // Leaves
            drawOval(
                color = Color(0xFF6B775B),
                topLeft = Offset(w * 0.22f, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.15f)
            )
            drawOval(
                color = Color(0xFF77846F),
                topLeft = Offset(w * 0.42f, h * 0.22f),
                size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.15f)
            )
            drawOval(
                color = Color(0xFF5B654D),
                topLeft = Offset(w * 0.45f, h * 0.50f),
                size = androidx.compose.ui.geometry.Size(w * 0.25f, h * 0.14f)
            )
            drawOval(
                color = Color(0xFF6E7A60),
                topLeft = Offset(w * 0.60f, h * 0.34f),
                size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.13f)
            )
            // Warm olive fruit accent
            drawCircle(
                color = AgedGold,
                radius = w * 0.065f,
                center = Offset(w * 0.44f, h * 0.48f)
            )
        }
    }
}

@Composable
fun ArtmuseumArtworkImage(
    imageSource: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val drawableRes = remember(imageSource) {
        when (imageSource) {
            "preset:welcome_sea" -> R.drawable.art_welcome_sea
            "preset:coastal_villa" -> R.drawable.art_coastal_villa
            "preset:alpine_valley" -> R.drawable.art_alpine_valley
            "preset:profile_header" -> R.drawable.art_profile_header
            "preset:meadow_reading" -> R.drawable.art_meadow_reading
            "preset:golden_river" -> R.drawable.art_golden_river
            "preset:portrait_study" -> R.drawable.art_portrait_study
            "preset:avatar_sophia" -> R.drawable.avatar_sophia
            "preset:avatar_daniel" -> R.drawable.avatar_daniel
            else -> null
        }
    }

    if (drawableRes != null) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }

    if (imageSource.startsWith("data:image")) {
        val decodedBitmap = remember(imageSource) {
            try {
                val base64Part = imageSource.substringAfter("base64,", "")
                if (base64Part.isNotEmpty()) {
                    val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } else null
            } catch (t: Throwable) {
                null
            }
        }
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
            return
        }
    }

    if (imageSource.isNotBlank()) {
        AsyncImage(
            model = imageSource,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = R.drawable.art_coastal_villa),
            placeholder = painterResource(id = R.drawable.art_coastal_villa)
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.art_coastal_villa),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun ArtmuseumAvatar(
    photoURL: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color(0xFFE0D7C6),
    onClick: (() -> Unit)? = null
) {
    val resolvedSource = photoURL.ifBlank { "preset:avatar_sophia" }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        ArtmuseumArtworkImage(
            imageSource = resolvedSource,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ArtmuseumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    testTag: String? = null
) {
    val colors = LocalArtmuseumColors.current
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = ArtmuseumShapeTokens.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.pillSelectedFill,
            contentColor = colors.pillSelectedText,
            disabledContainerColor = colors.pillSelectedFill.copy(alpha = 0.55f),
            disabledContentColor = colors.pillSelectedText.copy(alpha = 0.75f)
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 13.dp),
        modifier = modifier
            .height(50.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.pillSelectedText,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = colors.pillSelectedText
                )
            }
        }
    }
}

@Composable
fun ArtmuseumSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    testTag: String? = null
) {
    val colors = LocalArtmuseumColors.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ArtmuseumShapeTokens.Pill,
        border = BorderStroke(1.dp, colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.cardSurface.copy(alpha = 0.88f),
            contentColor = colors.inkPrimary
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 13.dp),
        modifier = modifier
            .height(50.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = colors.inkPrimary
            )
        }
    }
}

@Composable
fun ArtmuseumIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFEFE8D8).copy(alpha = 0.90f),
    contentColor: Color = InkPrimary,
    size: Dp = 38.dp,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = containerColor,
            border = BorderStroke(0.8.dp, MuseumBorder.copy(alpha = 0.6f)),
            modifier = Modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ArtmuseumFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val colors = LocalArtmuseumColors.current
    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.pillSelectedFill else colors.subtleSurface,
        label = "filterChipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.pillSelectedText else colors.inkPrimary.copy(alpha = 0.85f),
        label = "filterChipText"
    )

    Surface(
        onClick = onClick,
        shape = ArtmuseumShapeTokens.Pill,
        color = bgColor,
        border = if (selected) null else BorderStroke(0.8.dp, colors.border.copy(alpha = 0.85f)),
        modifier = modifier
            .height(36.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = textColor
            )
        }
    }
}

@Composable
fun ArtmuseumTopicChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val colors = LocalArtmuseumColors.current
    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.pillSelectedFill else colors.subtleSurface,
        label = "topicChipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.pillSelectedText else colors.inkPrimary.copy(alpha = 0.82f),
        label = "topicChipContent"
    )

    Surface(
        onClick = onClick,
        shape = ArtmuseumShapeTokens.Pill,
        color = bgColor,
        border = if (selected) null else BorderStroke(0.8.dp, colors.border),
        modifier = modifier
            .height(34.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(13.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun ArtmuseumSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalArtmuseumColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                shape = ArtmuseumShapeTokens.Pill,
                color = if (isSelected) colors.pillSelectedFill else colors.subtleSurface,
                border = if (isSelected) null else BorderStroke(0.8.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("segment_${label.lowercase(Locale.ROOT)}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 13.5.sp
                        ),
                        color = if (isSelected) colors.pillSelectedText else colors.mutedText
                    )
                }
            }
        }
    }
}

@Composable
fun ArtmuseumSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null
) {
    val colors = LocalArtmuseumColors.current
    Surface(
        shape = ArtmuseumShapeTokens.Pill,
        color = colors.cardSurface,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = colors.mutedText,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.mutedText
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.inkPrimary),
                    cursorBrush = SolidColor(colors.pillSelectedFill),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )
            }
            if (value.isNotEmpty() && onClear != null) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear search",
                    tint = colors.mutedText,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onClear)
                )
            }
        }
    }
}

@Composable
fun ArtmuseumPostCard(
    post: Post,
    isLiked: Boolean,
    isSaved: Boolean,
    onCardClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFollowButton: Boolean = false,
    isFollowingAuthor: Boolean = false,
    onToggleFollowAuthor: () -> Unit = {}
) {
    val colors = LocalArtmuseumColors.current
    val dimens = LocalArtmuseumDimensions.current
    // Whole UI active theme card background
    val cardBg = colors.cardSurface

    Surface(
        onClick = onCardClick,
        shape = ArtmuseumShapeTokens.Card,
        color = cardBg,
        border = BorderStroke(0.6.dp, colors.border),
        shadowElevation = 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.postId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.cardPadding)
        ) {
            // Author Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ArtmuseumAvatar(
                    photoURL = post.authorPhotoSnapshot,
                    contentDescription = post.authorNameSnapshot,
                    size = dimens.avatarSizeMedium,
                    onClick = onAuthorClick
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAuthorClick)
                ) {
                    Text(
                        text = post.authorNameSnapshot,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (dimens.isCompact) 14.sp else 15.sp
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
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (dimens.isCompact) 11.5.sp else 12.sp
                        ),
                        color = colors.mutedText
                    )
                }
                if (showFollowButton) {
                    Surface(
                        onClick = onToggleFollowAuthor,
                        shape = ArtmuseumShapeTokens.Pill,
                        color = if (isFollowingAuthor) colors.subtleSurface else colors.pillSelectedFill,
                        border = BorderStroke(
                            0.7.dp,
                            if (isFollowingAuthor) colors.border else colors.pillSelectedFill
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("post_follow_${post.postId}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 11.dp)
                        ) {
                            Text(
                                text = if (isFollowingAuthor) "Following" else "Follow",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = if (isFollowingAuthor) colors.inkPrimary else colors.pillSelectedText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Post options",
                    tint = colors.mutedText,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onMoreClick)
                        .padding(4.dp)
                        .testTag("post_more_${post.postId}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val hasArtwork = post.imageURL.isNotBlank()
            val showTextAboveImage = post.authorNameSnapshot.equals("Daniel", ignoreCase = true)

            if (showTextAboveImage && post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = if (dimens.isCompact) 13.5.sp else 14.5.sp,
                        lineHeight = if (dimens.isCompact) 19.sp else 21.sp
                    ),
                    color = colors.inkPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasArtwork) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (hasArtwork) {
                val baseRatio = post.aspectRatio.substringBefore("_")
                val sizeKey = post.aspectRatio.substringAfter("_", "standard")
                val ratioModifier = remember(baseRatio, sizeKey) {
                    val arMod = when (baseRatio) {
                        "4:5" -> Modifier.aspectRatio(4f / 5f)
                        "16:9" -> Modifier.aspectRatio(16f / 9f)
                        "3:4" -> Modifier.aspectRatio(3f / 4f)
                        else -> Modifier.aspectRatio(1f)
                    }
                    when (sizeKey) {
                        "compact" -> Modifier.heightIn(max = 200.dp).then(arMod)
                        "large" -> Modifier.heightIn(max = 420.dp).then(arMod)
                        else -> arMod
                    }
                }
                ArtmuseumArtworkImage(
                    imageSource = post.imageURL,
                    contentDescription = post.imageCaption.ifBlank { "Exhibit artwork" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(ratioModifier)
                        .clip(ArtmuseumShapeTokens.ArtworkFrame)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (!showTextAboveImage && post.text.isNotBlank()) {
                Text(
                    text = post.text.substringBefore("\n\n"),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = if (dimens.isCompact) 13.5.sp else 14.5.sp,
                        lineHeight = if (dimens.isCompact) 19.sp else 21.sp
                    ),
                    color = colors.inkPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Real-time Action Pill Row
            PostEngagementPillRow(
                likeCount = post.likeCount,
                isLiked = isLiked,
                isSaved = isSaved,
                onLikeClick = onLikeClick,
                onSaveClick = onSaveClick,
                onShareClick = onShareClick,
                postId = post.postId
            )
        }
    }
}

@Composable
fun PostEngagementPillRow(
    likeCount: Int,
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    postId: String = ""
) {
    val colors = LocalArtmuseumColors.current
    val dimens = LocalArtmuseumDimensions.current

    // Real-time honest like count: no fake "1.2k" simulation!
    val formattedLikes = remember(likeCount) {
        when {
            likeCount >= 1000 -> String.format(Locale.US, "%.1fk", likeCount / 1000f)
            else -> likeCount.toString()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Like Pill
        Surface(
            onClick = onLikeClick,
            shape = ArtmuseumShapeTokens.Pill,
            color = if (isLiked) HeartCrimson.copy(alpha = 0.12f) else colors.subtleSurface,
            border = BorderStroke(0.6.dp, if (isLiked) HeartCrimson.copy(alpha = 0.35f) else colors.border),
            modifier = Modifier
                .weight(1f)
                .height(dimens.postActionPillHeight)
                .testTag("like_button_$postId")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like exhibit",
                    tint = if (isLiked) HeartCrimson else colors.mutedText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedLikes,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    ),
                    color = if (isLiked) HeartCrimson else colors.inkPrimary
                )
            }
        }

        // 2. Bookmark Pill
        Surface(
            onClick = onSaveClick,
            shape = ArtmuseumShapeTokens.Pill,
            color = if (isSaved) colors.pillSelectedFill.copy(alpha = 0.15f) else colors.subtleSurface,
            border = BorderStroke(0.6.dp, if (isSaved) colors.pillSelectedFill.copy(alpha = 0.35f) else colors.border),
            modifier = Modifier
                .weight(1f)
                .height(dimens.postActionPillHeight)
                .testTag("save_button_$postId")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save exhibit",
                    tint = if (isSaved) colors.pillSelectedFill else colors.inkPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // 3. Share Pill
        Surface(
            onClick = onShareClick,
            shape = ArtmuseumShapeTokens.Pill,
            color = colors.subtleSurface,
            border = BorderStroke(0.6.dp, colors.border),
            modifier = Modifier
                .weight(0.85f)
                .height(dimens.postActionPillHeight)
                .testTag("share_button_$postId")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Share exhibit",
                    tint = colors.inkPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ArtmuseumCommentComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    isSending: Boolean = false
) {
    val colors = LocalArtmuseumColors.current
    Surface(
        shape = ArtmuseumShapeTokens.Pill,
        color = colors.subtleSurface,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 6.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Add a comment...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.mutedText
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.inkPrimary),
                    cursorBrush = SolidColor(colors.pillSelectedFill),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comment_input")
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                onClick = { if (value.isNotBlank() && !isSending) onSend() },
                shape = CircleShape,
                color = OlivePillActive,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("send_comment_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = WarmPaper,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Send comment",
                            tint = WarmPaper,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtmuseumThemeSwatch(
    palette: ArtmuseumThemePalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalArtmuseumColors.current
    val swatchFill = when (palette) {
        ArtmuseumThemePalette.CREAM -> Color(0xFFEFE6D5)
        ArtmuseumThemePalette.SAGE -> Color(0xFF9AA592)
        ArtmuseumThemePalette.BLUE -> Color(0xFF9AB6C7)
        ArtmuseumThemePalette.ROSE -> Color(0xFFD99B96)
        ArtmuseumThemePalette.TERRACOTTA -> Color(0xFFD08B6E)
        ArtmuseumThemePalette.LAVENDER -> Color(0xFFB5A8CC)
        ArtmuseumThemePalette.SUNSET -> Color(0xFFD88055)
        ArtmuseumThemePalette.OLIVE -> Color(0xFF5B694D)
        ArtmuseumThemePalette.STRAWBERRY_MILK -> Color(0xFFF799B5)
        ArtmuseumThemePalette.PEACH_MOCHI -> Color(0xFFF7A287)
        ArtmuseumThemePalette.MATCHA_LATTE -> Color(0xFF8CAF77)
        ArtmuseumThemePalette.BUTTER_HONEY -> Color(0xFFF2BD44)
        ArtmuseumThemePalette.COTTON_CANDY -> Color(0xFFA594E6)
        ArtmuseumThemePalette.MINT_MACARON -> Color(0xFF67C2AE)
        ArtmuseumThemePalette.VANILLA_CHAI -> Color(0xFFC79872)
        ArtmuseumThemePalette.MIDNIGHT -> Color(0xFF343B40)
        ArtmuseumThemePalette.OBSIDIAN -> Color(0xFF1C1C20)
        ArtmuseumThemePalette.DARK_EMERALD -> Color(0xFF1A2B23)
        ArtmuseumThemePalette.DARK_AMETHYST -> Color(0xFF231E30)
        ArtmuseumThemePalette.DARK_CHARCOAL -> Color(0xFF252529)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("theme_swatch_${palette.label.lowercase(Locale.ROOT)}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .then(
                    if (selected) {
                        Modifier.border(1.8.dp, InkPrimary.copy(alpha = 0.75f), CircleShape)
                    } else {
                        Modifier
                    }
                )
                .padding(3.5.dp)
                .clip(CircleShape)
                .background(swatchFill)
                .border(0.8.dp, MuseumBorder, CircleShape)
        ) {
            // Subtle diagonal painterly sheen inside the swatch matching the mockup
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(size.width * 0.32f, size.height * 0.32f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = palette.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) colors.inkPrimary else colors.mutedText
        )
    }
}

enum class ArtmuseumTab(val label: String, val testTag: String) {
    HOME("Home", "nav_home"),
    EXPLORE("Explore", "nav_explore"),
    SAVED("Saved", "nav_saved"),
    PROFILE("Profile", "nav_profile")
}

@Composable
fun ArtmuseumBottomNavigation(
    currentTab: ArtmuseumTab,
    onTabSelected: (ArtmuseumTab) -> Unit,
    onCreatePostClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalArtmuseumColors.current
    val dimens = LocalArtmuseumDimensions.current

    Surface(
        color = colors.cardSurface,
        border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.8f)),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = if (dimens.isCompact) 3.dp else 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home Tab
            ArtmuseumNavItem(
                tab = ArtmuseumTab.HOME,
                selected = currentTab == ArtmuseumTab.HOME,
                onClick = { onTabSelected(ArtmuseumTab.HOME) }
            )

            // 2. Explore Tab
            ArtmuseumNavItem(
                tab = ArtmuseumTab.EXPLORE,
                selected = currentTab == ArtmuseumTab.EXPLORE,
                onClick = { onTabSelected(ArtmuseumTab.EXPLORE) }
            )

            // 3. Center Post Plus Action Button (Shifted into center between Explore & Saved)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .offset(y = (-3).dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.pillSelectedFill,
                    shadowElevation = 6.dp,
                    border = BorderStroke(2.dp, colors.cardSurface),
                    modifier = Modifier
                        .size(dimens.bottomNavCenterButtonSize)
                        .clip(CircleShape)
                        .clickable(onClick = onCreatePostClick)
                        .testTag("nav_create_post_button")
                        .testTag("fab_create_post")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create exhibit",
                            tint = Color.White,
                            modifier = Modifier.size(if (dimens.isCompact) 22.dp else 25.dp)
                        )
                    }
                }
            }

            // 4. Saved Tab
            ArtmuseumNavItem(
                tab = ArtmuseumTab.SAVED,
                selected = currentTab == ArtmuseumTab.SAVED,
                onClick = { onTabSelected(ArtmuseumTab.SAVED) }
            )

            // 5. Profile Tab
            ArtmuseumNavItem(
                tab = ArtmuseumTab.PROFILE,
                selected = currentTab == ArtmuseumTab.PROFILE,
                onClick = { onTabSelected(ArtmuseumTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun ArtmuseumNavItem(
    tab: ArtmuseumTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalArtmuseumColors.current
    val dimens = LocalArtmuseumDimensions.current
    val icon = when (tab) {
        ArtmuseumTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        ArtmuseumTab.EXPLORE -> Icons.Outlined.Explore
        ArtmuseumTab.SAVED -> if (selected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
        ArtmuseumTab.PROFILE -> Icons.Outlined.PersonOutline
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(tab.testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(if (dimens.isCompact) 38.dp else 42.dp)
                .height(if (dimens.isCompact) 25.dp else 28.dp)
                .clip(ArtmuseumShapeTokens.Pill)
                .background(if (selected) colors.navActivePill else Color.Transparent)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tab.label,
                tint = if (selected) Color.White else colors.mutedText,
                modifier = Modifier.size(if (dimens.isCompact) 17.dp else 19.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (dimens.isCompact) 10.sp else 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) colors.inkPrimary else colors.mutedText
        )
    }
}

@Composable
fun VerifiedMuseumBadge(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "Verified",
        tint = VerifiedBadgeTaupe,
        modifier = modifier.size(17.dp)
    )
}
