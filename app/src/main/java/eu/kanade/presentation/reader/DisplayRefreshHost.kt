package eu.kanade.presentation.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import kotlinx.coroutines.delay
import tachiyomi.presentation.core.util.collectAsState
import kotlin.time.Duration.Companion.milliseconds

@Stable
class DisplayRefreshHost(
    readerPreferences: ReaderPreferences,
    /**
     * Lets the panel do the full refresh itself, answering whether it did. The drawn flash below
     * is what happens when it can't, which is everywhere but a BOOX device.
     */
    private val panelRefresh: (() -> Boolean)? = null,
) {

    internal var currentDisplayRefresh by mutableStateOf(false)
    internal val flashMillis = readerPreferences.flashDurationMillis
    internal val flashMode = readerPreferences.flashColor

    // Read on every page turn rather than once, so a change to it applies to the panel refresh
    // too, which never draws the flash below and so never moves the counter.
    internal val flashIntervalPref = readerPreferences.flashPageInterval

    private var timesCalled = 0

    fun flash() {
        if (timesCalled % flashIntervalPref.get().coerceAtLeast(1) == 0 && panelRefresh?.invoke() != true) {
            currentDisplayRefresh = true
        }
        timesCalled += 1
    }
}

@Composable
fun DisplayRefreshHost(
    hostState: DisplayRefreshHost,
    modifier: Modifier = Modifier,
) {
    val currentDisplayRefresh = hostState.currentDisplayRefresh
    val refreshDuration by hostState.flashMillis.collectAsState()
    val flashMode by hostState.flashMode.collectAsState()

    var currentColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(currentDisplayRefresh) {
        if (!currentDisplayRefresh) {
            currentColor = null
            return@LaunchedEffect
        }

        val refreshDurationHalf = refreshDuration.milliseconds / 2
        currentColor = if (flashMode == ReaderPreferences.FlashColor.BLACK) {
            Color.Black
        } else {
            Color.White
        }
        delay(refreshDurationHalf)
        if (flashMode == ReaderPreferences.FlashColor.WHITE_BLACK) {
            currentColor = Color.Black
        }
        delay(refreshDurationHalf)
        hostState.currentDisplayRefresh = false
    }

    Canvas(
        modifier = modifier.fillMaxSize(),
    ) {
        currentColor?.let { drawRect(it) }
    }
}
