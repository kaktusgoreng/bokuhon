package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA

@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RectangleShape,
    colors: Any? = null,
) {
    OutlinedField(
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        label = label,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        textField = {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                readOnly = readOnly,
                textStyle = fieldTextStyle(enabled),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(LocalBokuhonColors.current.onSurface),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) placeholder?.invoke()
                        inner()
                    }
                },
            )
        },
    )
}

@Composable
fun OutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RectangleShape,
    colors: Any? = null,
) {
    OutlinedField(
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        label = label,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        textField = {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                readOnly = readOnly,
                textStyle = fieldTextStyle(enabled),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(LocalBokuhonColors.current.onSurface),
                decorationBox = { inner ->
                    Box {
                        if (value.text.isEmpty()) placeholder?.invoke()
                        inner()
                    }
                },
            )
        },
    )
}

@Composable
fun OutlinedTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RectangleShape,
    colors: Any? = null,
) {
    OutlinedField(
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        label = label,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        textField = {
            BasicTextField(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                readOnly = readOnly,
                textStyle = fieldTextStyle(enabled),
                keyboardOptions = keyboardOptions,
                lineLimits = lineLimits,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(LocalBokuhonColors.current.onSurface),
                decorator = PlaceholderTextFieldDecorator(
                    showPlaceholder = { state.text.isEmpty() },
                    placeholder = placeholder,
                ),
            )
        },
    )
}

@Composable
fun OutlinedSecureTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    textObfuscationMode: TextObfuscationMode = TextObfuscationMode.Hidden,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RectangleShape,
) {
    OutlinedField(
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        label = label,
        supportingText = supportingText,
        leadingIcon = null,
        trailingIcon = trailingIcon,
        shape = shape,
        textField = {
            BasicTextField(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                textStyle = fieldTextStyle(enabled),
                keyboardOptions = keyboardOptions,
                lineLimits = lineLimits,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(LocalBokuhonColors.current.onSurface),
                outputTransformation = if (textObfuscationMode == TextObfuscationMode.Hidden) {
                    PasswordOutputTransformation
                } else {
                    null
                },
                decorator = PlaceholderTextFieldDecorator(
                    showPlaceholder = { state.text.isEmpty() },
                    placeholder = placeholder,
                ),
            )
        },
    )
}

@Composable
private fun fieldTextStyle(enabled: Boolean): TextStyle {
    val color = LocalBokuhonColors.current.onSurface
    return LocalBokuhonTypography.current.bodyLarge.copy(
        color = if (enabled) color else color.copy(alpha = DISABLED_ALPHA),
    )
}

@Composable
private fun OutlinedField(
    modifier: Modifier,
    enabled: Boolean,
    isError: Boolean,
    label: (@Composable () -> Unit)?,
    supportingText: (@Composable () -> Unit)?,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    shape: Shape,
    textField: @Composable () -> Unit,
) {
    val colors = LocalBokuhonColors.current
    val borderColor = when {
        isError -> colors.onSurface
        else -> colors.outline
    }
    Column(modifier = modifier) {
        label?.let {
            Box(modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                CompositionLocalProvider(
                    LocalBokuhonContentColor provides
                        (if (enabled) colors.ash else colors.ash.copy(alpha = DISABLED_ALPHA)),
                    LocalBokuhonTextStyle provides LocalBokuhonTypography.current.labelSmall,
                ) {
                    it()
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    CompositionLocalProvider(LocalBokuhonContentColor provides colors.onSurface) { leadingIcon() }
                }
            }
            Box(modifier = Modifier.weight(1f)) { textField() }
            trailingIcon?.let {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    CompositionLocalProvider(LocalBokuhonContentColor provides colors.onSurface) { trailingIcon() }
                }
            }
        }
        supportingText?.let {
            Box(modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                CompositionLocalProvider(
                    LocalBokuhonContentColor provides colors.onSurfaceVariant,
                    LocalBokuhonTextStyle provides LocalBokuhonTypography.current.bodySmall,
                ) {
                    it()
                }
            }
        }
    }
}

@Stable
class PlaceholderTextFieldDecorator(
    private val showPlaceholder: () -> Boolean,
    private val placeholder: (@Composable () -> Unit)?,
) : TextFieldDecorator {
    @Composable
    override fun Decoration(content: @Composable () -> Unit) {
        Box {
            if (showPlaceholder()) placeholder?.invoke()
            content()
        }
    }
}

private object PasswordOutputTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val masked = "•".repeat(length)
        replace(0, length, masked)
    }
}
