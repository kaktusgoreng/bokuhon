package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA
import kotlin.math.roundToInt

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    shape: Shape = RectangleShape,
    containerColor: Color = LocalBokuhonColors.current.surface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 3.dp,
    border: BorderStroke? = BorderStroke(1.dp, LocalBokuhonColors.current.outline),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return

    val density = LocalDensity.current
    val positionProvider = remember(offset, density) {
        MenuPositionProvider(
            contentOffset = with(density) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) },
            margin = with(density) { 8.dp.roundToPx() },
        )
    }

    Popup(
        onDismissRequest = onDismissRequest,
        properties = properties,
        popupPositionProvider = positionProvider,
    ) {
        Surface(shape = shape, color = containerColor, border = border) {
            Column(
                modifier = modifier
                    .width(IntrinsicSize.Max)
                    .heightIn(max = 560.dp)
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp),
                content = content,
            )
        }
    }
}

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: Any? = null,
    contentPadding: PaddingValues = MenuItemContentPadding,
    interactionSource: Any? = null,
) {
    val contentColor = LocalBokuhonColors.current.onSurface.let {
        if (enabled) it else it.copy(alpha = DISABLED_ALPHA)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompositionLocalProvider(
            LocalBokuhonContentColor provides contentColor,
            LocalBokuhonTextStyle provides LocalBokuhonTypography.current.bodyLarge,
        ) {
            leadingIcon?.invoke()
            Box(modifier = Modifier.weight(1f)) { text() }
            trailingIcon?.invoke()
        }
    }
}

private val MenuItemContentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

private class MenuPositionProvider(
    private val contentOffset: IntOffset,
    private val margin: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val popupWidth = popupContentSize.width
        val popupHeight = popupContentSize.height
        val fitsBelow = anchorBounds.bottom + contentOffset.y + popupHeight <= windowSize.height - margin
        val y = if (fitsBelow) {
            anchorBounds.bottom + contentOffset.y
        } else {
            (anchorBounds.top - popupHeight + contentOffset.y).coerceAtLeast(margin)
        }
        val x = if (layoutDirection == LayoutDirection.Rtl) {
            anchorBounds.right - popupWidth - contentOffset.x
        } else {
            anchorBounds.left + contentOffset.x
        }
        val maxX = (windowSize.width - popupWidth - margin).coerceAtLeast(margin)
        val maxY = (windowSize.height - popupHeight - margin).coerceAtLeast(margin)
        return IntOffset(x.coerceIn(margin, maxX), y.coerceIn(margin, maxY))
    }
}

enum class ExposedDropdownMenuAnchorType {
    PrimaryNotEditable,
    PrimaryEditable,
    SecondaryEditable,
}

@Composable
fun ExposedDropdownMenuBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        ExposedDropdownMenuBoxScope(expanded, onExpandedChange).content()
    }
}

class ExposedDropdownMenuBoxScope internal constructor(
    val expanded: Boolean,
    private val onExpandedChange: (Boolean) -> Unit,
) {
    fun Modifier.menuAnchor(type: ExposedDropdownMenuAnchorType): Modifier = this
        .pointerInput(expanded) {
            awaitEachGesture {
                // Read-only fields still consume taps, so toggle from the initial pass.
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                onExpandedChange(!expanded)
                waitForUpOrCancellation()
            }
        }

    @Composable
    fun ExposedDropdownMenu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            content = content,
        )
    }
}

object ExposedDropdownMenuDefaults {
    val ItemContentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    @Composable
    fun TrailingIcon(expanded: Boolean) {
        val color = LocalBokuhonColors.current.onSurface
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.28f, h * 0.4f)
                lineTo(w * 0.5f, h * 0.62f)
                lineTo(w * 0.72f, h * 0.4f)
                close()
            }
            drawPath(path, color)
        }
    }

    @Composable
    fun textFieldColors(): Any? = null
}
