package eu.kanade.presentation.util

import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

/**
 * The first frame of an animated vector drawable, drawn as a plain vector.
 *
 * AVD painters run their animation whenever their end state flips, which for the tab icons is
 * every selection; these icons only need to be drawn.
 */
@Composable
fun rememberStaticVectorPainter(image: AnimatedImageVector): Painter = rememberVectorPainter(image.imageVector)
