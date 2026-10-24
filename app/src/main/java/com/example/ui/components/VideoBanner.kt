package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.ui.theme.LocalArtmuseumColors

/**
 * A looping, muted video banner using AndroidX Media3 ExoPlayer.
 * Features rounded corners, no controls, and handles Android lifecycle properly.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoBanner(
    modifier: Modifier = Modifier,
    height: Dp = 190.dp,
    cornerRadius: Dp = 16.dp,
    videoRawResId: Int = R.raw.paint_anim
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = LocalArtmuseumColors.current
    val dimens = com.example.ui.theme.LocalArtmuseumDimensions.current
    val effectiveHeight = if (height == 190.dp) dimens.bannerHeight else height

    val shape = RoundedCornerShape(if (dimens.isCompact) 14.dp else cornerRadius)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val rawResourceUri = Uri.parse("android.resource://${context.packageName}/$videoRawResId")
            val mediaItem = MediaItem.fromUri(rawResourceUri)
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f // Muted
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Surface(
        shape = shape,
        color = colors.cardSurface,
        border = BorderStroke(0.6.dp, colors.border),
        shadowElevation = 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(effectiveHeight)
            .testTag("home_video_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
