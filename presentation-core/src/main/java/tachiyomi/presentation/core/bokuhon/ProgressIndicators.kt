package tachiyomi.presentation.core.bokuhon

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
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
    val transition = rememberInfiniteTransition(label = "indeterminate")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1333, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    CircularIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = strokeCap,
        progress = null,
        startAngle = rotation,
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
    val transition = rememberInfiniteTransition(label = "indeterminate")
    val fraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "position",
    )
    LinearIndicator(modifier = modifier, color = color, trackColor = trackColor, progress = null, fraction = fraction)
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
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
    ) {
        drawRect(color = trackColor)
        if (progress != null) {
            drawRect(
                color = color,
                size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
            )
        } else {
            val segment = size.width * 0.3f
            val start = (size.width + segment) * fraction - segment
            drawRect(
                color = color,
                topLeft = Offset(start, 0f),
                size = Size(segment, size.height),
            )
        }
    }
}
