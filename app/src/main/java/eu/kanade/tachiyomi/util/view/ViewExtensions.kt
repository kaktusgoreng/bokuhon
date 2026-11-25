@file:Suppress("NOTHING_TO_INLINE")

package eu.kanade.tachiyomi.util.view

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.inputmethod.EditorInfoCompat
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import eu.kanade.domain.base.BasePreferences
import eu.kanade.presentation.theme.TachiyomiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mihon.app.di.appGraph
import tachiyomi.presentation.core.bokuhon.BokuhonDialogHost
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonContentColor
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTextStyle
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.ProvideBokuhonDialogHost

inline fun ComponentActivity.setComposeContent(
    parent: CompositionContext? = null,
    crossinline content: @Composable () -> Unit,
) {
    setContent(parent) {
        TachiyomiTheme {
            CompositionLocalProvider(
                LocalBokuhonTextStyle provides LocalBokuhonTypography.current.bodySmall,
                LocalBokuhonContentColor provides LocalBokuhonColors.current.onBackground,
                LocalMetroViewModelFactory provides appGraph.viewModelFactory,
            ) {
                // Inside the provider above: dialog bodies are composed by the host, not where
                // they're opened, and their screens need the factory to build view models
                ProvideBokuhonDialogHost(host = remember { BokuhonDialogHost() }) {
                    content()
                }
            }
        }
    }
}

fun ComposeView.setComposeContent(
    content: @Composable () -> Unit,
) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        TachiyomiTheme {
            CompositionLocalProvider(
                LocalBokuhonTextStyle provides LocalBokuhonTypography.current.bodySmall,
                LocalBokuhonContentColor provides LocalBokuhonColors.current.onBackground,
            ) {
                content()
            }
        }
    }
}

fun View?.isVisibleOnScreen(): Boolean {
    if (this == null) {
        return false
    }
    if (!this.isShown) {
        return false
    }
    val actualPosition = Rect()
    this.getGlobalVisibleRect(actualPosition)
    val screen =
        Rect(0, 0, Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
    return actualPosition.intersect(screen)
}

/**
 * Sets [EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING] in imeOptions while [BasePreferences.incognitoMode] is
 * true. Some IMEs may not respect this flag.
 */
fun EditText.setIncognito(basePreferences: BasePreferences, viewScope: CoroutineScope) {
    basePreferences.incognitoMode.changes()
        .onEach {
            imeOptions = if (it) {
                imeOptions or EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
            } else {
                imeOptions and EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING.inv()
            }
        }
        .launchIn(viewScope)
}
