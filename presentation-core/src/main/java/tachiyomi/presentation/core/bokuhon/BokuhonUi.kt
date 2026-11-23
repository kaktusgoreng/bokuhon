package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// A deliberately tiny UI layer over Compose Foundation: no Material library, four tones, square
// corners, and the handful of primitives the app actually uses.

@Immutable
data class BokuhonColors(
    val paper: Color,
    val mist: Color,
    val ash: Color,
    val ink: Color,
    val isDark: Boolean,
) {
    val background: Color get() = if (isDark) ink else paper
    val surface: Color get() = if (isDark) ink else paper
    val surfaceVariant: Color get() = if (isDark) ash else mist
    val onSurface: Color get() = if (isDark) paper else ink
    val onSurfaceVariant: Color get() = if (isDark) paper else mist
    val outline: Color get() = ash
    val outlineVariant: Color get() = if (isDark) ash else mist
    val primary: Color get() = onSurface
    val onPrimary: Color get() = background
    val container: Color get() = surfaceVariant
    val onContainer: Color get() = onSurface
}

val BokuhonPaper = Color(0xFFF7F7F7)
val BokuhonMist = Color(0xFFE3E3E3)
val BokuhonAsh = Color(0xFF767676)
val BokuhonInk = Color(0xFF141414)

val BokuhonLightColors = BokuhonColors(BokuhonPaper, BokuhonMist, BokuhonAsh, BokuhonInk, isDark = false)
val BokuhonDarkColors = BokuhonColors(BokuhonPaper, BokuhonMist, BokuhonAsh, BokuhonInk, isDark = true)

@Immutable
data class BokuhonTypography(
    val displaySmall: TextStyle = TextStyle(fontSize = 36.sp, lineHeight = 44.sp),
    val headlineSmall: TextStyle = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    val titleLarge: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp),
    val titleMedium: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    val bodyLarge: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    val bodyMedium: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    val bodySmall: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    val labelLarge: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    val labelSmall: TextStyle = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

val LocalBokuhonColors = staticCompositionLocalOf { BokuhonLightColors }
val LocalBokuhonTypography = staticCompositionLocalOf { BokuhonTypography() }
val LocalBokuhonContentColor = staticCompositionLocalOf { Color.Unspecified }

@Composable
fun ProvideBokuhonTheme(isDark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalBokuhonColors provides if (isDark) BokuhonDarkColors else BokuhonLightColors,
        LocalBokuhonTypography provides BokuhonTypography(),
        content = content,
    )
}

@Composable
fun BokuhonText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalBokuhonTypography.current.bodyLarge,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color.takeOrElse {
                LocalBokuhonContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
            },
            textAlign = textAlign ?: style.textAlign,
        ),
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun BokuhonIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    val resolved = tint.takeOrElse {
        LocalBokuhonContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
    }
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(resolved),
    )
}

@Composable
fun BokuhonDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalBokuhonColors.current.outlineVariant),
    )
}

@Composable
fun BokuhonVerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(LocalBokuhonColors.current.outlineVariant),
    )
}

@Composable
fun BokuhonBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(16.dp)
            .background(LocalBokuhonColors.current.primary),
    ) {
        BokuhonText(
            text = text,
            modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
            style = LocalBokuhonTypography.current.labelSmall,
            color = LocalBokuhonColors.current.onPrimary,
            maxLines = 1,
        )
    }
}
