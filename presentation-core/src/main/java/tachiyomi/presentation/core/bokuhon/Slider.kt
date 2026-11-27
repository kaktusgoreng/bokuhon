package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Stable
class SliderState(
    initialValue: Float,
    val steps: Int,
    val trackRange: ClosedFloatingPointRange<Float>,
) {
    var value: Float by mutableFloatStateOf(initialValue.coerceIn(trackRange))
}

@Composable
fun rememberSliderState(
    value: Float,
    steps: Int = 0,
    trackRange: ClosedFloatingPointRange<Float> = 0f..1f,
): SliderState = remember(trackRange, steps) { SliderState(value, steps, trackRange) }

@Composable
fun Slider(
    state: SliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChange: ((Float) -> Unit)? = null,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    SliderImpl(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interactionSource,
        vertical = false,
    )
}

@Composable
fun VerticalSlider(
    state: SliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChange: ((Float) -> Unit)? = null,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    SliderImpl(
        state = state,
        modifier = modifier
            .fillMaxHeight()
            .width(48.dp),
        enabled = enabled,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interactionSource,
        vertical = true,
    )
}

@Composable
private fun SliderImpl(
    state: SliderState,
    modifier: Modifier,
    enabled: Boolean,
    onValueChange: ((Float) -> Unit)?,
    onValueChangeFinished: (() -> Unit)?,
    interactionSource: MutableInteractionSource?,
    vertical: Boolean,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val reversed = !vertical && LocalLayoutDirection.current == LayoutDirection.Rtl

    // A vertical slider runs bottom-to-top and a horizontal one runs right-to-left in RTL, so the
    // drag math is flipped where the track's start isn't its left or top edge.
    fun fractionToValue(fraction: Float): Float {
        val f = if (vertical || reversed) 1f - fraction else fraction
        return state.valueAt(f)
    }

    fun update(value: Float) {
        val snapped = state.snap(value)
        if (snapped != state.value) {
            state.value = snapped
            onValueChange?.invoke(snapped)
        }
    }

    val stateModifier = Modifier
        .semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = state.value,
                range = state.trackRange,
                steps = state.steps,
            )
            if (enabled) {
                setProgress { target ->
                    update(target.let { state.snap(it) })
                    true
                }
            }
        }
        .pointerInput(enabled, vertical, reversed, state.trackRange, state.steps) {
            if (!enabled) return@pointerInput
            detectTapGestures { offset ->
                val size = if (vertical) this.size.height else this.size.width
                val position = if (vertical) offset.y else offset.x
                update(fractionToValue(position / size))
            }
        }
        .pointerInput(enabled, vertical, reversed, state.trackRange, state.steps) {
            if (!enabled) return@pointerInput
            var dragStart: DragInteraction.Start? = null
            val onStart: (Offset) -> Unit = { offset ->
                val start = DragInteraction.Start()
                dragStart = start
                source.tryEmit(start)
                val size = if (vertical) this.size.height else this.size.width
                val position = if (vertical) offset.y else offset.x
                update(fractionToValue(position / size))
            }
            val onEnd: () -> Unit = {
                dragStart?.let { source.tryEmit(DragInteraction.Stop(it)) }
                dragStart = null
                onValueChangeFinished?.invoke()
            }
            val onCancel: () -> Unit = {
                dragStart?.let { source.tryEmit(DragInteraction.Cancel(it)) }
                dragStart = null
                onValueChangeFinished?.invoke()
            }
            if (vertical) {
                detectVerticalDragGestures(
                    onDragStart = onStart,
                    onDragEnd = onEnd,
                    onDragCancel = onCancel,
                ) { change, _ ->
                    change.consume()
                    update(fractionToValue(change.position.y / size.height))
                }
            } else {
                detectHorizontalDragGestures(
                    onDragStart = onStart,
                    onDragEnd = onEnd,
                    onDragCancel = onCancel,
                ) { change, _ ->
                    change.consume()
                    update(fractionToValue(change.position.x / size.width))
                }
            }
        }

    Box(
        modifier = modifier.then(stateModifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        val c = LocalBokuhonColors.current
        val fraction = state.fractionOf(state.value)
        Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            if (vertical) {
                val centerX = size.width / 2
                val top = 8.dp.toPx()
                val bottom = size.height - 8.dp.toPx()
                val activeY = bottom - (bottom - top) * fraction
                drawLine(
                    color = c.primary,
                    start = Offset(centerX, top),
                    end = Offset(centerX, bottom),
                    strokeWidth = TRACK_HAIRLINE.toPx(),
                )
                drawRoundRect(
                    color = c.primary,
                    topLeft = Offset(centerX - READ_BAR.toPx() / 2, activeY),
                    size = Size(READ_BAR.toPx(), (bottom - activeY).coerceAtLeast(0f)),
                    cornerRadius = CornerRadius(READ_BAR.toPx() / 2),
                )
                drawHandle(Offset(centerX, activeY), c.surface, c.primary, vertical = true)
            } else {
                val centerY = size.height / 2
                val left = 8.dp.toPx()
                val right = size.width - 8.dp.toPx()
                val activeX = left + (right - left) * (if (reversed) 1f - fraction else fraction)
                drawLine(
                    color = c.primary,
                    start = Offset(left, centerY),
                    end = Offset(right, centerY),
                    strokeWidth = TRACK_HAIRLINE.toPx(),
                )
                // In RTL the track fills from its right end, the direction the thumb moves towards.
                val fillStart = if (reversed) activeX else left
                val fillEnd = if (reversed) right else activeX
                drawRoundRect(
                    color = c.primary,
                    topLeft = Offset(fillStart, centerY - READ_BAR.toPx() / 2),
                    size = Size((fillEnd - fillStart).coerceAtLeast(0f), READ_BAR.toPx()),
                    cornerRadius = CornerRadius(READ_BAR.toPx() / 2),
                )
                drawHandle(Offset(activeX, centerY), c.surface, c.primary, vertical = false)
            }
        }
    }
}

/**
 * The knob, drawn the way BOOX's own reader draws it: a small white card ruled with two grip lines,
 * which is what stands in for a shadow or highlight on a panel that shows neither.
 */
private fun DrawScope.drawHandle(centre: Offset, fill: Color, stroke: Color, vertical: Boolean) {
    val width = (if (vertical) HANDLE_ACROSS else HANDLE_ALONG).toPx()
    val height = (if (vertical) HANDLE_ALONG else HANDLE_ACROSS).toPx()
    val corner = CornerRadius(HANDLE_CORNER.toPx())
    val topLeft = Offset(centre.x - width / 2, centre.y - height / 2)
    val size = Size(width, height)
    drawRoundRect(color = fill, topLeft = topLeft, size = size, cornerRadius = corner)
    drawRoundRect(
        color = stroke,
        topLeft = topLeft,
        size = size,
        cornerRadius = corner,
        style = Stroke(width = STROKE.toPx()),
    )
    val reach = GRIP_LENGTH.toPx() / 2
    val gap = GRIP_GAP.toPx() / 2
    if (vertical) {
        for (sign in intArrayOf(-1, 1)) {
            val y = centre.y + sign * gap
            drawLine(stroke, Offset(centre.x - reach, y), Offset(centre.x + reach, y), STROKE.toPx())
        }
    } else {
        for (sign in intArrayOf(-1, 1)) {
            val x = centre.x + sign * gap
            drawLine(stroke, Offset(x, centre.y - reach), Offset(x, centre.y + reach), STROKE.toPx())
        }
    }
}

/** The part of the run already read: a heavy bar with rounded ends. */
private val READ_BAR = 14.dp

/** The part still ahead: a single ruled line, as BOOX draws it. */
private val TRACK_HAIRLINE = 1.dp

/** The knob: taller across the track than along it, so it stands on the bar. */
private val HANDLE_ALONG = 15.dp
private val HANDLE_ACROSS = 21.dp
private val HANDLE_CORNER = 2.dp
private val STROKE = 1.dp

/** The two grip lines inside the knob. */
private val GRIP_LENGTH = 10.dp
private val GRIP_GAP = 5.dp

private fun SliderState.valueAt(fraction: Float): Float {
    val f = fraction.coerceIn(0f, 1f)
    val min = trackRange.start
    val max = trackRange.endInclusive
    return min + (max - min) * f
}

private fun SliderState.fractionOf(value: Float): Float {
    val min = trackRange.start
    val max = trackRange.endInclusive
    if (max == min) return 0f
    return ((value - min) / (max - min)).coerceIn(0f, 1f)
}

private fun SliderState.snap(value: Float): Float {
    val min = trackRange.start
    val max = trackRange.endInclusive
    val coerced = value.coerceIn(min, max)
    if (steps <= 0) return coerced
    val segments = steps + 1
    val step = (max - min) / segments
    return (min + ((coerced - min) / step).roundToInt() * step).coerceIn(min, max)
}
