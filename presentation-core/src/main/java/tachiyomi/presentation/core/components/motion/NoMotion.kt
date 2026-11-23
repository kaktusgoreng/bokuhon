package tachiyomi.presentation.core.components.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

// The app has no motion: e-ink refreshes turn any transition into a grey smear. These are
// drop-in replacements for the Compose animation APIs that render the end state immediately, so
// screens keep their structure without animating.

@Composable
fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = EnterTransition.None,
    exit: ExitTransition = ExitTransition.None,
    label: String = "AnimatedVisibility",
    content: @Composable () -> Unit,
) {
    if (visible) {
        Box(modifier = modifier) { content() }
    }
}

@Composable
fun <S> AnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        EnterTransition.None togetherWith ExitTransition.None
    },
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "AnimatedContent",
    sizeTransform: SizeTransform? = null,
    contentKey: (S) -> Any? = { it },
    content: @Composable (S) -> Unit,
) {
    content(targetState)
}

@Composable
fun <T> Crossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(0),
    label: String = "Crossfade",
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) { content(targetState) }
}

@Composable
fun animateFloatAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = tween(0),
    visibilityThreshold: Float = 0.01f,
    label: String = "FloatAnimation",
    finishedListener: ((Float) -> Unit)? = null,
): State<Float> = rememberUpdatedState(targetValue)

@Composable
fun animateDpAsState(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp> = tween(0),
    label: String = "DpAnimation",
    finishedListener: ((Dp) -> Unit)? = null,
): State<Dp> = rememberUpdatedState(targetValue)

@Composable
fun animateColorAsState(
    targetValue: Color,
    animationSpec: AnimationSpec<Color> = tween(0),
    label: String = "ColorAnimation",
    finishedListener: ((Color) -> Unit)? = null,
): State<Color> = rememberUpdatedState(targetValue)
