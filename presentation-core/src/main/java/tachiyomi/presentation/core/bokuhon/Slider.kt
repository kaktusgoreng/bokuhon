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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
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

    // A vertical slider runs bottom-to-top; the drag math is flipped so the top is the max value.
    fun fractionToValue(fraction: Float): Float {
        val f = if (vertical) 1f - fraction else fraction
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
        .pointerInput(enabled, vertical, state.trackRange, state.steps) {
            if (!enabled) return@pointerInput
            detectTapGestures { offset ->
                val size = if (vertical) this.size.height else this.size.width
                val position = if (vertical) offset.y else offset.x
                update(fractionToValue(position / size))
            }
        }
        .pointerInput(enabled, vertical, state.trackRange, state.steps) {
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
                val trackWidth = 4.dp.toPx()
                val centerX = size.width / 2
                val top = 8.dp.toPx()
                val bottom = size.height - 8.dp.toPx()
                val activeY = bottom - (bottom - top) * fraction
                drawRect(
                    color = c.surfaceVariant,
                    topLeft = Offset(centerX - trackWidth / 2, top),
                    size = Size(trackWidth, bottom - top),
                )
                drawRect(
                    color = c.primary,
                    topLeft = Offset(centerX - trackWidth / 2, activeY),
                    size = Size(trackWidth, bottom - activeY),
                )
                val thumb = 14.dp.toPx()
                drawRect(
                    color = c.primary,
                    topLeft = Offset(centerX - thumb / 2, activeY - thumb / 2),
                    size = Size(thumb, thumb),
                )
            } else {
                val trackHeight = 4.dp.toPx()
                val centerY = size.height / 2
                val left = 8.dp.toPx()
                val right = size.width - 8.dp.toPx()
                val activeX = left + (right - left) * fraction
                drawRect(
                    color = c.surfaceVariant,
                    topLeft = Offset(left, centerY - trackHeight / 2),
                    size = Size(right - left, trackHeight),
                )
                drawRect(
                    color = c.primary,
                    topLeft = Offset(left, centerY - trackHeight / 2),
                    size = Size((activeX - left).coerceAtLeast(0f), trackHeight),
                )
                val thumb = 14.dp.toPx()
                drawRect(
                    color = c.primary,
                    topLeft = Offset(activeX - thumb / 2, centerY - thumb / 2),
                    size = Size(thumb, thumb),
                )
            }
        }
    }
}

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
