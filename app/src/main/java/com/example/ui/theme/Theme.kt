package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArtmuseumThemePalette

object ArtmuseumShapeTokens {
    val Pill = CircleShape
    val SmallRounded = RoundedCornerShape(12.dp)
    val ArtworkFrame = RoundedCornerShape(18.dp)
    val Card = RoundedCornerShape(20.dp)
    val LargeSheetTop = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}

val ArtmuseumShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

data class ArtmuseumDimensions(
    val isCompact: Boolean = false,
    val isVerySmall: Boolean = false,
    val horizontalPadding: Dp = 18.dp,
    val topBarHeight: Dp = 56.dp,
    val headerTitleSize: TextUnit = 22.sp,
    val bannerHeight: Dp = 195.dp,
    val cardPadding: Dp = 16.dp,
    val cardArtworkHeight: Dp = 250.dp,
    val bottomNavHeight: Dp = 62.dp,
    val bottomNavCenterButtonSize: Dp = 50.dp,
    val avatarSizeLarge: Dp = 80.dp,
    val avatarSizeMedium: Dp = 40.dp,
    val avatarSizeSmall: Dp = 34.dp,
    val profileBannerHeight: Dp = 145.dp,
    val buttonHeight: Dp = 44.dp,
    val chipHeight: Dp = 36.dp,
    val postActionPillHeight: Dp = 34.dp
)

val LocalArtmuseumDimensions = staticCompositionLocalOf { ArtmuseumDimensions() }

data class ArtmuseumExtendedColors(
    val palette: ArtmuseumThemePalette = ArtmuseumThemePalette.CREAM,
    val background: Color = MuseumCream,
    val cardSurface: Color = WarmPaper,
    val subtleSurface: Color = WarmPaperSubtle,
    val pillSelectedFill: Color = OlivePillActive,
    val pillSelectedText: Color = Color.White,
    val pillUnselectedFill: Color = WarmPaperSubtle,
    val pillUnselectedText: Color = InkPrimary,
    val navActivePill: Color = NavPillBrown,
    val inkPrimary: Color = InkPrimary,
    val mutedText: Color = MutedTaupe,
    val border: Color = MuseumBorder,
    val heartColor: Color = HeartCrimson
)

val LocalArtmuseumColors = staticCompositionLocalOf { ArtmuseumExtendedColors() }

@Composable
fun MyApplicationTheme(
    palette: ArtmuseumThemePalette = ArtmuseumThemePalette.CREAM,
    darkTheme: Boolean = isSystemInDarkTheme() || palette.isDark,
    forceCompact: Boolean = false,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isCompact = forceCompact || screenWidth < 380.dp || screenHeight < 720.dp
    val isVerySmall = forceCompact || screenWidth < 340.dp || screenHeight < 640.dp

    val dimensions = remember(isCompact, isVerySmall) {
        if (isVerySmall) {
            ArtmuseumDimensions(
                isCompact = true,
                isVerySmall = true,
                horizontalPadding = 12.dp,
                topBarHeight = 44.dp,
                headerTitleSize = 18.sp,
                bannerHeight = 130.dp,
                cardPadding = 10.dp,
                cardArtworkHeight = 175.dp,
                bottomNavHeight = 52.dp,
                bottomNavCenterButtonSize = 42.dp,
                avatarSizeLarge = 64.dp,
                avatarSizeMedium = 32.dp,
                avatarSizeSmall = 28.dp,
                profileBannerHeight = 100.dp,
                buttonHeight = 36.dp,
                chipHeight = 28.dp,
                postActionPillHeight = 28.dp
            )
        } else if (isCompact) {
            ArtmuseumDimensions(
                isCompact = true,
                isVerySmall = false,
                horizontalPadding = 14.dp,
                topBarHeight = 50.dp,
                headerTitleSize = 19.5.sp,
                bannerHeight = 150.dp,
                cardPadding = 12.dp,
                cardArtworkHeight = 205.dp,
                bottomNavHeight = 56.dp,
                bottomNavCenterButtonSize = 46.dp,
                avatarSizeLarge = 72.dp,
                avatarSizeMedium = 36.dp,
                avatarSizeSmall = 30.dp,
                profileBannerHeight = 120.dp,
                buttonHeight = 40.dp,
                chipHeight = 32.dp,
                postActionPillHeight = 30.dp
            )
        } else {
            ArtmuseumDimensions(
                isCompact = false,
                isVerySmall = false,
                horizontalPadding = 18.dp,
                topBarHeight = 56.dp,
                headerTitleSize = 22.sp,
                bannerHeight = 195.dp,
                cardPadding = 16.dp,
                cardArtworkHeight = 250.dp,
                bottomNavHeight = 62.dp,
                bottomNavCenterButtonSize = 50.dp,
                avatarSizeLarge = 80.dp,
                avatarSizeMedium = 40.dp,
                avatarSizeSmall = 34.dp,
                profileBannerHeight = 145.dp,
                buttonHeight = 44.dp,
                chipHeight = 36.dp,
                postActionPillHeight = 34.dp
            )
        }
    }

    val useDark = palette.isDark || darkTheme
    val bg = Color(palette.primaryBackgroundHex)
    val card = Color(palette.cardSurfaceHex)
    val subtle = Color(palette.subtleSurfaceHex)
    val primaryAccent = Color(palette.accentHex)
    val pillFill = Color(palette.chipFillHex)
    val navPill = Color(palette.navActivePillHex)
    val border = Color(palette.borderHex)

    // Bold, crisp Apple-style high contrast typography ink
    val ink = if (useDark) Color(0xFFF7F9FA) else Color(0xFF181A1C)
    val muted = if (useDark) Color(0xFFA1ACB4) else Color(0xFF6B7280)

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = Color(0xFF101214),
            secondary = pillFill,
            onSecondary = Color(0xFF101214),
            tertiary = primaryAccent,
            background = bg,
            onBackground = ink,
            surface = card,
            onSurface = ink,
            surfaceVariant = subtle,
            onSurfaceVariant = muted,
            outline = border
        )
    } else {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            secondary = pillFill,
            onSecondary = Color.White,
            tertiary = primaryAccent,
            background = bg,
            onBackground = ink,
            surface = card,
            onSurface = ink,
            surfaceVariant = subtle,
            onSurfaceVariant = muted,
            outline = border
        )
    }

    val extended = ArtmuseumExtendedColors(
        palette = palette,
        background = bg,
        cardSurface = card,
        subtleSurface = subtle,
        pillSelectedFill = pillFill,
        pillSelectedText = Color.White,
        pillUnselectedFill = subtle,
        pillUnselectedText = ink,
        navActivePill = navPill,
        inkPrimary = ink,
        mutedText = muted,
        border = border,
        heartColor = HeartCrimson
    )

    CompositionLocalProvider(
        LocalArtmuseumColors provides extended,
        LocalArtmuseumDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ArtmuseumShapes,
            content = content
        )
    }
}

