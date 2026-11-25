package tachiyomi.presentation.core.bokuhon

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

object ProgressIndicatorDefaults {
    val ProgressAnimationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
    )
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalBokuhonColors.current.primary,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = LocalBokuhonColors.current.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    CircularIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = strokeCap,
        progress = null,
        startAngle = -90f,
        sweepAngle = 270f,
    )
}

@Composable
fun CircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = LocalBokuhonColors.current.primary,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = LocalBokuhonColors.current.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    CircularIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = strokeCap,
        progress = progress(),
        startAngle = -90f,
        sweepAngle = 360f,
    )
}

@Composable
private fun CircularIndicator(
    modifier: Modifier,
    color: Color,
    strokeWidth: Dp,
    trackColor: Color,
    strokeCap: StrokeCap,
    progress: Float?,
    startAngle: Float,
    sweepAngle: Float,
) {
    Canvas(modifier = modifier.size(40.dp)) {
        val stroke = strokeWidth.toPx()
        val topLeft = Offset(stroke / 2, stroke / 2)
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val style = Stroke(width = stroke, cap = strokeCap)
        if (progress != null) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = style,
            )
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = style,
            )
        } else {
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = style,
            )
        }
    }
}

@Composable
fun LinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalBokuhonColors.current.primary,
    trackColor: Color = LocalBokuhonColors.current.surfaceVariant,
) {
    LinearIndicator(modifier = modifier, color = color, trackColor = trackColor, progress = 0.35f, fraction = 0f)
}

@Composable
fun LinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = LocalBokuhonColors.current.primary,
    trackColor: Color = LocalBokuhonColors.current.surfaceVariant,
) {
    LinearIndicator(
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        progress = progress(),
        fraction = 0f,
    )
}

@Composable
private fun LinearIndicator(
    modifier: Modifier,
    color: Color,
    trackColor: Color,
    progress: Float?,
    fraction: Float,
) {
    val reversed = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
    ) {
        drawRect(color = trackColor)
        if (progress != null) {
            val filled = size.width * progress.coerceIn(0f, 1f)
            drawRect(
                color = color,
                topLeft = Offset(if (reversed) size.width - filled else 0f, 0f),
                size = Size(filled, size.height),
            )
        } else {
            val segment = size.width * 0.3f
            val start = (size.width + segment) * fraction - segment
            drawRect(
                color = color,
                topLeft = Offset(if (reversed) size.width - start - segment else start, 0f),
                size = Size(segment, size.height),
            )
        }
    }
}
