package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.ScrollIndicatorState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

/**
 * Draws a passive scrollbar thumb at the trailing edge; the fast scroller handles interaction.
 */
fun Modifier.nonInteractiveScrollbar(
    state: ScrollIndicatorState,
    orientation: Orientation = Orientation.Vertical,
): Modifier = composed {
    val color = LocalBokuhonColors.current.ash.copy(alpha = 0.5f)
    drawWithContent {
        drawContent()
        if (orientation != Orientation.Vertical) return@drawWithContent
        val contentSize = state.contentSize
        val viewportSize = state.viewportSize
        if (contentSize <= 0 || viewportSize <= 0 || contentSize <= viewportSize) return@drawWithContent

        val thumbWidth = 3.dp.toPx()
        val inset = 2.dp.toPx()
        val thumbHeight = (size.height * viewportSize / contentSize)
            .coerceIn(24.dp.toPx(), size.height)
        val maxOffset = (contentSize - viewportSize).toFloat()
        val top = (size.height - thumbHeight) * (state.scrollOffset / maxOffset).coerceIn(0f, 1f)
        drawRect(
            color = color,
            topLeft = Offset(size.width - thumbWidth - inset, top),
            size = Size(thumbWidth, thumbHeight),
        )
    }
}
