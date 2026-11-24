package eu.kanade.tachiyomi.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import eu.kanade.presentation.reader.ReaderLoadingIcon
import eu.kanade.presentation.theme.TachiyomiTheme

/**
 * The glyph a page shows until its image is ready, in the middle of the page.
 */
class ReaderLoadingIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    init {
        // Filling the page is what centres the glyph in it, whatever the holder lays out around it.
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    @Composable
    override fun Content() {
        TachiyomiTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ReaderLoadingIcon()
            }
        }
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}
