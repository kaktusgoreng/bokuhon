package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// A deliberately tiny UI layer over Compose Foundation: no Material widgets, four tones, square
// corners, and the handful of primitives the app actually uses. Names and parameters mirror the
// Material components they replace, so screens migrate by changing imports only.

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
val LocalBokuhonTextStyle = staticCompositionLocalOf { BokuhonTypography().bodyLarge }
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
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalBokuhonTextStyle.current,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    softWrap: Boolean = true,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    // While Material still owns the theme, honour its ambient content colour so text inside
    // Material buttons and cards keeps its contrast.
    val resolvedColor = color.takeOrElse {
        LocalContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
    }
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = resolvedColor,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            fontWeight = fontWeight ?: style.fontWeight,
            fontStyle = fontStyle ?: style.fontStyle,
            fontFamily = fontFamily ?: style.fontFamily,
            letterSpacing = if (letterSpacing != TextUnit.Unspecified) letterSpacing else style.letterSpacing,
            lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight else style.lineHeight,
            textAlign = textAlign ?: style.textAlign,
        ),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalBokuhonTextStyle.current,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    val resolvedColor = color.takeOrElse {
        LocalContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
    }
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = resolvedColor,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            fontWeight = fontWeight ?: style.fontWeight,
            letterSpacing = if (letterSpacing != TextUnit.Unspecified) letterSpacing else style.letterSpacing,
            textAlign = textAlign ?: style.textAlign,
        ),
        overflow = overflow,
        maxLines = maxLines,
    )
}

@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(resolveIconTint(tint)),
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(resolveIconTint(tint)),
    )
}

@Composable
private fun resolveIconTint(tint: Color): Color = tint.takeOrElse {
    LocalContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
}

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = LocalBokuhonColors.current.outlineVariant,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = LocalBokuhonColors.current.outlineVariant,
) {
    Box(
        modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color),
    )
}

@Composable
private fun ButtonSurface(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    container: Color,
    contentColor: Color,
    border: androidx.compose.foundation.BorderStroke?,
    borderColor: Color,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .then(if (border != null) Modifier.border(border.width, borderColor) else Modifier)
            .background(container)
            .clickable(enabled = enabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) { content() }
    }
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Any? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val c = LocalBokuhonColors.current
    ButtonSurface(onClick, modifier, enabled, c.primary, c.onPrimary, border, c.outline, content)
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Any? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val c = LocalBokuhonColors.current
    ButtonSurface(onClick, modifier, enabled, Color.Transparent, c.onSurface, border, c.outline, content)
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Any? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val c = LocalBokuhonColors.current
    ButtonSurface(
        onClick,
        modifier,
        enabled,
        Color.Transparent,
        c.onSurface,
        border ?: androidx.compose.foundation.BorderStroke(1.dp, c.outline),
        c.outline,
        content,
    )
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    shape: Any? = null,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(enabled = enabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    shape: Any? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(LocalBokuhonColors.current.primary)
            .clickable(enabled = enabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides LocalBokuhonColors.current.onPrimary) {
            content()
        }
    }
}

@Composable
fun BokuhonBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(16.dp)
            .background(LocalBokuhonColors.current.primary),
    ) {
        Text(
            text = text,
            modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
            style = LocalBokuhonTypography.current.labelSmall,
            color = LocalBokuhonColors.current.onPrimary,
            maxLines = 1,
        )
    }
}
