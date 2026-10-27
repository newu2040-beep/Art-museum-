package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AutumnChocolate
import com.example.ui.theme.CaveatFontFamily
import com.example.ui.theme.LocalArtmuseumColors

/**
 * Reusable Doodle composable with slight rotation and optional gentle wobble animation.
 */
@Composable
fun Doodle(
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    rotation: Float = 0f,
    alpha: Float = 0.3f,
    tintColor: androidx.compose.ui.graphics.Color? = null,
    enableWobble: Boolean = false
) {
    val wobbleRotation = if (enableWobble) {
        val infiniteTransition = rememberInfiniteTransition(label = "doodleWobble")
        val animRotation by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "doodleRotationAnim"
        )
        animRotation
    } else 0f

    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = tintColor?.let { ColorFilter.tint(it) },
        modifier = modifier
            .size(size)
            .rotate(rotation + wobbleRotation)
            .alpha(alpha)
    )
}

/**
 * Reusable Sticker composable (size, rotation, optional wobble or bounce animation)
 */
@Composable
fun Sticker(
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    rotation: Float = 0f,
    enableWobble: Boolean = false,
    enableBounce: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stickerAnim")

    val wobbleAngle = if (enableWobble) {
        val wobble by infiniteTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "stickerWobble"
        )
        wobble
    } else 0f

    val bounceScale = if (enableBounce) {
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.93f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "stickerBounce"
        )
        scale
    } else 1f

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .rotate(rotation + wobbleAngle)
            .scale(bounceScale)
            .then(clickModifier)
    )
}

/**
 * Paper background layer with subtle paper texture at low opacity
 */
@Composable
fun PaperBackgroundOverlay(
    modifier: Modifier = Modifier,
    opacity: Float = 0.12f
) {
    Image(
        painter = painterResource(id = R.drawable.paper_texture),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxSize()
            .alpha(opacity)
    )
}

/**
 * Plaid header strip for cozy scrapbook cards
 */
@Composable
fun PlaidCardHeader(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    cornerRadius: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
    ) {
        Image(
            painter = painterResource(id = R.drawable.plaid_tile),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Corner Pumpkin decoration partially visible at a screen corner
 */
@Composable
fun CornerPumpkin(
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    alignment: Alignment = Alignment.TopStart
) {
    Box(
        modifier = modifier
            .size(size),
        contentAlignment = alignment
    ) {
        Image(
            painter = painterResource(id = R.drawable.pumpkin),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size)
                .offset {
                    when (alignment) {
                        Alignment.TopStart -> IntOffset(-15, -15)
                        Alignment.BottomEnd -> IntOffset(20, 20)
                        else -> IntOffset(0, 0)
                    }
                }
        )
    }
}

/**
 * Doodles scattered at 30% opacity behind screen content
 */
@Composable
fun ScatteredDoodlesBackground(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Star doodle top-right
        Doodle(
            drawableRes = R.drawable.doodle_star,
            size = 38.dp,
            rotation = 12f,
            alpha = 0.28f,
            enableWobble = true,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 70.dp)
        )
        // Heart doodle mid-right
        Doodle(
            drawableRes = R.drawable.doodle_heart,
            size = 34.dp,
            rotation = -10f,
            alpha = 0.28f,
            enableWobble = false,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-16).dp, y = (-40).dp)
        )
        // Swirl doodle left
        Doodle(
            drawableRes = R.drawable.doodle_swirl,
            size = 36.dp,
            rotation = 18f,
            alpha = 0.26f,
            enableWobble = true,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 12.dp, y = 60.dp)
        )
        // Leaf doodle bottom-right
        Doodle(
            drawableRes = R.drawable.doodle_leaf,
            size = 40.dp,
            rotation = -15f,
            alpha = 0.30f,
            enableWobble = false,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-24).dp, y = (-90).dp)
        )
        // Daisy doodle bottom-left
        Doodle(
            drawableRes = R.drawable.doodle_daisy,
            size = 42.dp,
            rotation = 20f,
            alpha = 0.28f,
            enableWobble = true,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-70).dp)
        )
    }
}

/**
 * Empty state styled for autumn cozy scrapbook: char_autumn or camera/jam sticker with friendly text
 */
@Composable
fun ArtmuseumScrapbookEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    useCharacter: Boolean = false,
    useJamSticker: Boolean = false,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = LocalArtmuseumColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        if (useCharacter) {
            Image(
                painter = painterResource(id = R.drawable.char_autumn),
                contentDescription = "Autumn Friend",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(175.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        } else {
            val stickerRes = if (useJamSticker) R.drawable.st_jam else R.drawable.st_camera
            Sticker(
                drawableRes = stickerRes,
                size = 72.dp,
                rotation = -6f,
                enableWobble = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = CaveatFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            ),
            color = colors.inkPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = colors.mutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(18.dp))
            ArtmuseumPrimaryButton(
                text = actionButtonText,
                onClick = onActionClick
            )
        }
    }
}
