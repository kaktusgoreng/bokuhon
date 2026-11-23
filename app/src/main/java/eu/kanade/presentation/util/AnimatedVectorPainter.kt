package eu.kanade.presentation.util

import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter

/**
 * Returns the end frame of [enter] or [exit] without playing anything; the app has no motion.
 */
@Composable
fun rememberAnimatedVectorPainter(
    enter: AnimatedImageVector,
    exit: AnimatedImageVector,
    atEnd: Boolean,
): Painter {
    return rememberAnimatedVectorPainter(if (atEnd) enter else exit, atEnd = true)
}
