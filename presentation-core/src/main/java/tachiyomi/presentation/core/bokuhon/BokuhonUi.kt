package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
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
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA

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
    val onBackground: Color get() = if (isDark) paper else ink
    val surface: Color get() = background
    val onSurface: Color get() = onBackground
    val surfaceVariant: Color get() = if (isDark) ash else mist
    val onSurfaceVariant: Color get() = if (isDark) mist else ash
    val surfaceBright: Color get() = if (isDark) ash else paper
    val surfaceDim: Color get() = if (isDark) ink else mist
    val surfaceContainerLowest: Color get() = surface
    val surfaceContainerLow: Color get() = surface
    val surfaceContainer: Color get() = surface
    val surfaceContainerHigh: Color get() = surfaceVariant
    val surfaceContainerHighest: Color get() = surfaceVariant
    val surfaceTint: Color get() = primary
    val inverseSurface: Color get() = onSurface
    val inverseOnSurface: Color get() = surface
    val scrim: Color get() = ink

    val outline: Color get() = ash
    val outlineVariant: Color get() = if (isDark) ash else mist

    val primary: Color get() = onSurface
    val onPrimary: Color get() = surface
    val primaryContainer: Color get() = primary
    val onPrimaryContainer: Color get() = onPrimary

    val secondary: Color get() = ash
    val onSecondary: Color get() = paper
    val secondaryContainer: Color get() = surfaceVariant
    val onSecondaryContainer: Color get() = onSurface

    val tertiary: Color get() = ash
    val onTertiary: Color get() = paper
    val tertiaryContainer: Color get() = surfaceVariant
    val onTertiaryContainer: Color get() = onSurface

    val error: Color get() = onSurface
    val onError: Color get() = surface
    val errorContainer: Color get() = surfaceVariant
    val onErrorContainer: Color get() = onSurface

    val active: Color get() = if (isDark) Color(0xFFEBEB3B) else Color(0xFFFFC107)

    // Elevation has no meaning on an e-ink panel; tinted surfaces just muddy the four tones.
    fun surfaceColorAtElevation(elevation: Dp): Color = surface

    val container: Color get() = surfaceVariant
}

val BokuhonPaper = Color(0xFFF7F7F7)
val BokuhonMist = Color(0xFFE3E3E3)
val BokuhonAsh = Color(0xFF767676)
val BokuhonInk = Color(0xFF141414)

val BokuhonLightColors = BokuhonColors(BokuhonPaper, BokuhonMist, BokuhonAsh, BokuhonInk, isDark = false)
val BokuhonDarkColors = BokuhonColors(BokuhonPaper, BokuhonMist, BokuhonAsh, BokuhonInk, isDark = true)

@Immutable
data class BokuhonTypography(
    val displayLarge: TextStyle = TextStyle(fontSize = 57.sp, lineHeight = 64.sp),
    val displayMedium: TextStyle = TextStyle(fontSize = 45.sp, lineHeight = 52.sp),
    val displaySmall: TextStyle = TextStyle(fontSize = 36.sp, lineHeight = 44.sp),
    val headlineLarge: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 40.sp),
    val headlineMedium: TextStyle = TextStyle(fontSize = 28.sp, lineHeight = 36.sp),
    val headlineSmall: TextStyle = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    val titleLarge: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp),
    val titleMedium: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    val titleSmall: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    val bodyLarge: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    val bodyMedium: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    val bodySmall: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    val labelLarge: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    val labelMedium: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    val labelSmall: TextStyle = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

val LocalBokuhonColors = staticCompositionLocalOf { BokuhonLightColors }
val LocalBokuhonTypography = staticCompositionLocalOf { BokuhonTypography() }
val LocalBokuhonTextStyle = staticCompositionLocalOf { BokuhonTypography().bodyLarge }
val LocalBokuhonContentColor = staticCompositionLocalOf { Color.Unspecified }

@Composable
fun ProvideBokuhonTheme(isDark: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDark) BokuhonDarkColors else BokuhonLightColors
    CompositionLocalProvider(
        LocalBokuhonColors provides colors,
        LocalBokuhonTypography provides BokuhonTypography(),
        LocalBokuhonContentColor provides colors.onBackground,
        // Lists stop dead at their ends: the stretch overscroll is motion like any other.
        LocalOverscrollFactory provides null,
        content = content,
    )
}

@Composable
@ReadOnlyComposable
fun contentColorFor(backgroundColor: Color): Color {
    val c = LocalBokuhonColors.current
    return when (backgroundColor) {
        c.background, c.surface -> c.onBackground
        c.primary -> c.onPrimary
        c.primaryContainer -> c.onPrimaryContainer
        c.secondary -> c.onSecondary
        c.secondaryContainer -> c.onSecondaryContainer
        c.tertiary -> c.onTertiary
        c.tertiaryContainer -> c.onTertiaryContainer
        c.error -> c.onError
        c.errorContainer -> c.onErrorContainer
        c.surfaceVariant -> c.onSurfaceVariant
        else -> LocalBokuhonContentColor.current.takeOrElse { c.onSurface }
    }
}

fun Modifier.minimumInteractiveComponentSize(): Modifier = defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)

object BokuhonPadding {
    val extraLarge = 32.dp
    val large = 24.dp
    val medium = 16.dp
    val small = 8.dp
    val extraSmall = 4.dp
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
    val resolvedColor = color.takeOrElse {
        LocalBokuhonContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
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
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    val resolvedColor = color.takeOrElse {
        LocalBokuhonContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
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
        inlineContent = inlineContent,
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
    LocalBokuhonContentColor.current.takeOrElse { LocalBokuhonColors.current.onSurface }
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
        CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) { content() }
    }
}

@Immutable
data class ButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color = containerColor,
    val disabledContentColor: Color = contentColor,
)

object ButtonDefaults {
    @Composable
    fun buttonColors(
        containerColor: Color = LocalBokuhonColors.current.primary,
        contentColor: Color = LocalBokuhonColors.current.onPrimary,
        disabledContainerColor: Color = LocalBokuhonColors.current.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = LocalBokuhonColors.current.onSurface.copy(alpha = DISABLED_ALPHA),
    ): ButtonColors = ButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    @Composable
    fun outlinedButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = LocalBokuhonColors.current.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = LocalBokuhonColors.current.onSurface.copy(alpha = DISABLED_ALPHA),
    ): ButtonColors = ButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    @Composable
    fun textButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = LocalBokuhonColors.current.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = LocalBokuhonColors.current.onSurface.copy(alpha = DISABLED_ALPHA),
    ): ButtonColors = ButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    @Composable
    fun filledTonalButtonColors(
        containerColor: Color = LocalBokuhonColors.current.surfaceVariant,
        contentColor: Color = LocalBokuhonColors.current.onSurface,
        disabledContainerColor: Color = LocalBokuhonColors.current.surfaceVariant.copy(alpha = DISABLED_ALPHA),
        disabledContentColor: Color = LocalBokuhonColors.current.onSurface.copy(alpha = DISABLED_ALPHA),
    ): ButtonColors = ButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    fun elevatedButtonElevation(): Any? = null
}

private fun ButtonColors.container(enabled: Boolean): Color =
    if (enabled) containerColor else disabledContainerColor

private fun ButtonColors.content(enabled: Boolean): Color =
    if (enabled) contentColor else disabledContentColor

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Any? = null,
    colors: ButtonColors? = null,
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
        colors?.container(enabled) ?: c.primary,
        colors?.content(enabled) ?: c.onPrimary,
        border,
        c.outline,
        content,
    )
}

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Any? = null,
    colors: ButtonColors? = null,
    elevation: Any? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val c = LocalBokuhonColors.current
    val tonal = colors ?: ButtonDefaults.filledTonalButtonColors()
    ButtonSurface(
        onClick,
        modifier,
        enabled,
        tonal.container(enabled),
        tonal.content(enabled),
        border,
        c.outline,
        content,
    )
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Any? = null,
    colors: ButtonColors? = null,
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
        colors?.container(enabled) ?: Color.Transparent,
        colors?.content(enabled) ?: c.onSurface,
        border,
        c.outline,
        content,
    )
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Any? = null,
    colors: ButtonColors? = null,
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
        colors?.container(enabled) ?: Color.Transparent,
        colors?.content(enabled) ?: c.onSurface,
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

@Immutable
data class IconButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

object IconButtonDefaults {
    /** Side of the square filled icon button. */
    val Size = 40.dp

    @Composable
    fun filledIconButtonColors(
        containerColor: Color = LocalBokuhonColors.current.primary,
        contentColor: Color = LocalBokuhonColors.current.onPrimary,
        disabledContainerColor: Color = LocalBokuhonColors.current.onSurface.copy(alpha = DISABLED_ALPHA),
        disabledContentColor: Color = LocalBokuhonColors.current.surface.copy(alpha = DISABLED_ALPHA),
    ): IconButtonColors = IconButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )
}

@Composable
fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors? = null,
    shape: Any? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    val c = LocalBokuhonColors.current
    Box(
        modifier = modifier
            .size(IconButtonDefaults.Size)
            .background(colors?.let { if (enabled) it.containerColor else it.disabledContainerColor } ?: c.primary)
            .then(
                if (border !=
                    null
                ) {
                    Modifier.border(border, androidx.compose.ui.graphics.RectangleShape)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalBokuhonContentColor provides
                (colors?.let { if (enabled) it.contentColor else it.disabledContentColor } ?: c.onPrimary),
        ) {
            content()
        }
    }
}

@Composable
fun FilledIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val c = LocalBokuhonColors.current
    Box(
        modifier = modifier
            .background(if (checked) c.primary else Color.Transparent)
            .border(1.dp, if (checked) c.primary else c.outline)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = androidx.compose.ui.semantics.Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalBokuhonContentColor provides if (checked) c.onPrimary else c.onSurface,
        ) {
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
