package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA

private val CheckboxSize = 20.dp

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    interactionSource: Any? = null,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        onValueChange = onCheckedChange,
                        enabled = enabled,
                        role = Role.Checkbox,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        CheckboxBody(checked = checked, indeterminate = false, enabled = enabled)
    }
}

@Composable
fun TriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    interactionSource: Any? = null,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (onClick != null) {
                    Modifier.triStateToggleable(
                        state = state,
                        onClick = onClick,
                        enabled = enabled,
                        role = Role.Checkbox,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        CheckboxBody(
            checked = state == ToggleableState.On,
            indeterminate = state == ToggleableState.Indeterminate,
            enabled = enabled,
        )
    }
}

@Composable
private fun CheckboxBody(checked: Boolean, indeterminate: Boolean, enabled: Boolean) {
    val c = LocalBokuhonColors.current
    val active = checked || indeterminate
    val alpha = if (enabled) 1f else DISABLED_ALPHA
    Box(
        modifier = Modifier
            .size(CheckboxSize)
            .background(if (active) c.primary.copy(alpha = alpha) else Color.Transparent)
            .border(2.dp, (if (active) c.primary else c.outline).copy(alpha = alpha)),
    ) {
        if (active) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 2.dp.toPx()
                val markColor = c.onPrimary.copy(alpha = alpha)
                if (indeterminate) {
                    drawLine(
                        color = markColor,
                        start = Offset(size.width * 0.22f, size.height / 2),
                        end = Offset(size.width * 0.78f, size.height / 2),
                        strokeWidth = stroke,
                        cap = StrokeCap.Square,
                    )
                } else {
                    drawLine(
                        color = markColor,
                        start = Offset(size.width * 0.22f, size.height * 0.52f),
                        end = Offset(size.width * 0.42f, size.height * 0.72f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Square,
                    )
                    drawLine(
                        color = markColor,
                        start = Offset(size.width * 0.42f, size.height * 0.72f),
                        end = Offset(size.width * 0.80f, size.height * 0.28f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Square,
                    )
                }
            }
        }
    }
}

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    interactionSource: Any? = null,
) {
    val alpha = if (enabled) 1f else DISABLED_ALPHA
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (onClick != null) {
                    Modifier.selectable(
                        selected = selected,
                        onClick = onClick,
                        enabled = enabled,
                        role = Role.RadioButton,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val c = LocalBokuhonColors.current
        Canvas(modifier = Modifier.size(20.dp)) {
            val stroke = 2.dp.toPx()
            drawCircle(
                color = c.outline.copy(alpha = alpha),
                radius = (size.minDimension - stroke) / 2,
                style = Stroke(width = stroke),
            )
            if (selected) {
                drawCircle(
                    color = c.primary.copy(alpha = alpha),
                    radius = size.minDimension * 0.25f,
                )
            }
        }
    }
}
