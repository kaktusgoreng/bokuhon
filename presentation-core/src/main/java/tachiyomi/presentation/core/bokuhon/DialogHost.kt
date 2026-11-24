package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Draws dialogs inside the activity window, above everything else, newest on top.
 *
 * Dialog windows come with a window-manager scale and fade that can't be turned off from the app,
 * which smears on an e-ink panel. Nothing here animates: a layer is either drawn or it isn't.
 */
@Stable
class BokuhonDialogHost {
    internal class Layer(val content: @Composable () -> Unit)

    internal val layers = mutableStateListOf<Layer>()
}

val LocalBokuhonDialogHost = staticCompositionLocalOf<BokuhonDialogHost?> { null }

@Composable
fun ProvideBokuhonDialogHost(host: BokuhonDialogHost, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBokuhonDialogHost provides host) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            host.layers.forEach { layer ->
                key(layer) { layer.content() }
            }
        }
    }
}

/**
 * Draws [content] over the rest of the app, in the activity window.
 *
 * Draws nothing when there's no host, so it needs [ProvideBokuhonDialogHost] above it.
 */
@Composable
fun BokuhonDialogLayer(content: @Composable () -> Unit) {
    val host = LocalBokuhonDialogHost.current ?: return
    val currentContent = rememberUpdatedState(content)
    DisposableEffect(host) {
        val layer = BokuhonDialogHost.Layer { currentContent.value() }
        host.layers.add(layer)
        onDispose { host.layers.remove(layer) }
    }
}
