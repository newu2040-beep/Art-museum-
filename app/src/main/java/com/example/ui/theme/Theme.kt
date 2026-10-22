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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ArtmuseumThemePalette

object ArtmuseumShapeTokens {
    val Pill = CircleShape
    val SmallRounded = RoundedCornerShape(14.dp)
    val ArtworkFrame = RoundedCornerShape(18.dp)
    val Card = RoundedCornerShape(22.dp)
    val LargeSheetTop = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}

val ArtmuseumShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

data class ArtmuseumExtendedColors(
    val palette: ArtmuseumThemePalette = ArtmuseumThemePalette.CREAM,
    val background: Color = MuseumCream,
    val cardSurface: Color = WarmPaper,
    val subtleSurface: Color = WarmPaperSubtle,
    val pillSelectedFill: Color = OlivePillActive,
    val pillSelectedText: Color = WarmPaper,
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
    content: @Composable () -> Unit
) {
    val useDark = palette == ArtmuseumThemePalette.MIDNIGHT || (darkTheme && palette == ArtmuseumThemePalette.MIDNIGHT)
    val bg = if (useDark) MidnightBackground else Color(palette.primaryBackgroundHex)
    val card = if (useDark) MidnightSurface else Color(palette.cardSurfaceHex)
    val subtle = if (useDark) MidnightElevated else WarmPaperSubtle
    val primaryAccent = if (useDark) Color(0xFF8F9E84) else Color(palette.accentHex)
    val pillFill = if (useDark) Color(0xFF4B5645) else Color(palette.chipFillHex)
    val ink = if (useDark) MidnightInk else InkPrimary
    val muted = if (useDark) MidnightTaupe else MutedTaupe
    val border = if (useDark) MidnightBorder else MuseumBorder

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = MidnightInk,
            secondary = GallerySage,
            onSecondary = MidnightInk,
            tertiary = AgedGold,
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
            onPrimary = WarmPaper,
            secondary = GallerySage,
            onSecondary = WarmPaper,
            tertiary = AgedGold,
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
        pillSelectedText = WarmPaper,
        pillUnselectedFill = subtle,
        pillUnselectedText = ink,
        navActivePill = if (useDark) Color(0xFF5A5248) else NavPillBrown,
        inkPrimary = ink,
        mutedText = muted,
        border = border,
        heartColor = HeartCrimson
    )

    CompositionLocalProvider(LocalArtmuseumColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ArtmuseumShapes,
            content = content
        )
    }
}
