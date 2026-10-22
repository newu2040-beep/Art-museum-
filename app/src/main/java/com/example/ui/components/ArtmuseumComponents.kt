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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
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
            } catch (_: Exception) {
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
    modifier: Modifier = Modifier
) {
    val colors = LocalArtmuseumColors.current
    val cardPalette = ArtmuseumThemePalette.fromName(post.themeName)
    val cardBg = if (colors.palette == ArtmuseumThemePalette.MIDNIGHT) {
        colors.cardSurface
    } else {
        Color(cardPalette.cardSurfaceHex)
    }

    Surface(
        onClick = onCardClick,
        shape = ArtmuseumShapeTokens.Card,
        color = cardBg,
        border = BorderStroke(0.8.dp, colors.border.copy(alpha = 0.7f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.postId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Author Header Row (matches mockup: Avatar + Sophia / 23 · Female + overflow ⋮)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ArtmuseumAvatar(
                    photoURL = post.authorPhotoSnapshot,
                    contentDescription = post.authorNameSnapshot,
                    size = 40.dp,
                    onClick = onAuthorClick
                )
                Spacer(modifier = Modifier.width(11.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAuthorClick)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorNameSnapshot,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = colors.inkPrimary
                        )
                    }
                    Text(
                        text = "${post.authorAgeSnapshot} · ${post.authorGenderSnapshot}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = colors.mutedText
                    )
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

            Spacer(modifier = Modifier.height(12.dp))

            // For text-first posts without artwork or with artwork below text (like Daniel's card)
            val hasArtwork = post.imageURL.isNotBlank()
            val showTextAboveImage = post.authorNameSnapshot.equals("Daniel", ignoreCase = true)

            if (showTextAboveImage && post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    ),
                    color = colors.inkPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasArtwork) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (hasArtwork) {
                ArtmuseumArtworkImage(
                    imageSource = post.imageURL,
                    contentDescription = post.imageCaption.ifBlank { "Exhibit artwork" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
                        .clip(ArtmuseumShapeTokens.ArtworkFrame)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!showTextAboveImage && post.text.isNotBlank()) {
                Text(
                    text = post.text.substringBefore("\n\n"),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    ),
                    color = colors.inkPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Action Pill Row (matches mockup: [❤️ 1.2k] pill, [Bookmark] pill, [Share] pill)
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
    val formattedLikes = remember(likeCount) {
        when {
            likeCount >= 1000 -> String.format(Locale.US, "%.1fk", likeCount / 1000f)
            likeCount == 0 -> "1.2k"
            else -> likeCount.toString()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Like Pill
        Surface(
            onClick = onLikeClick,
            shape = ArtmuseumShapeTokens.Pill,
            color = colors.subtleSurface,
            border = BorderStroke(0.8.dp, colors.border),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("like_button_$postId")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.Favorite,
                    contentDescription = "Like post",
                    tint = if (isLiked) HeartCrimson else HeartCrimson.copy(alpha = 0.88f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedLikes,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = colors.inkPrimary
                )
            }
        }

        // 2. Bookmark Pill
        Surface(
            onClick = onSaveClick,
            shape = ArtmuseumShapeTokens.Pill,
            color = if (isSaved) colors.pillSelectedFill.copy(alpha = 0.14f) else colors.subtleSurface,
            border = BorderStroke(0.8.dp, colors.border),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("save_button_$postId")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save post",
                    tint = if (isSaved) colors.pillSelectedFill else colors.inkPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // 3. Share / Paper-plane Pill
        Surface(
            onClick = onShareClick,
            shape = ArtmuseumShapeTokens.Pill,
            color = colors.subtleSurface,
            border = BorderStroke(0.8.dp, colors.border),
            modifier = Modifier
                .weight(0.85f)
                .height(40.dp)
                .testTag("share_button_$postId")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Share post",
                    tint = colors.inkPrimary,
                    modifier = Modifier.size(18.dp)
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
        ArtmuseumThemePalette.MIDNIGHT -> Color(0xFF343B40)
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
    modifier: Modifier = Modifier
) {
    val colors = LocalArtmuseumColors.current
    Surface(
        color = colors.cardSurface.copy(alpha = 0.96f),
        border = BorderStroke(0.8.dp, colors.border.copy(alpha = 0.7f)),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtmuseumTab.entries.forEach { tab ->
                val selected = tab == currentTab
                val icon = when (tab) {
                    ArtmuseumTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                    ArtmuseumTab.EXPLORE -> Icons.Outlined.Explore
                    ArtmuseumTab.SAVED -> if (selected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                    ArtmuseumTab.PROFILE -> Icons.Outlined.PersonOutline
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag(tab.testTag)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(42.dp)
                            .height(28.dp)
                            .clip(ArtmuseumShapeTokens.Pill)
                            .background(if (selected) colors.navActivePill else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = if (selected) WarmPaper else colors.mutedText,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (selected) colors.inkPrimary else colors.mutedText
                    )
                }
            }
        }
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
