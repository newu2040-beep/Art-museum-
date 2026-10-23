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
    val useDark = palette.isDark || darkTheme
    val bg = if (useDark) Color(palette.primaryBackgroundHex) else Color(palette.primaryBackgroundHex)
    val card = if (useDark) Color(palette.cardSurfaceHex) else Color(palette.cardSurfaceHex)
    val subtle = if (useDark) Color(0xFF33383E) else WarmPaperSubtle
    val primaryAccent = Color(palette.accentHex)
    val pillFill = Color(palette.chipFillHex)
    val ink = if (useDark) Color(0xFFECEFF1) else InkPrimary
    val muted = if (useDark) Color(0xFFA1AAB0) else MutedTaupe
    val border = if (useDark) Color(0xFF3D444A) else MuseumBorder

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = Color(0xFF121214),
            secondary = GallerySage,
            onSecondary = Color(0xFF121214),
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
        pillSelectedText = if (useDark) Color(0xFFF5F7F8) else WarmPaper,
        pillUnselectedFill = subtle,
        pillUnselectedText = ink,
        navActivePill = if (useDark) Color(0xFF424A52) else NavPillBrown,
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
