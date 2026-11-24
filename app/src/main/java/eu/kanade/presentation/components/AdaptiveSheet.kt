package eu.kanade.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mihon.presentation.core.util.isTabletUi
import tachiyomi.presentation.core.bokuhon.BokuhonDialogLayer
import tachiyomi.presentation.core.components.AdaptiveSheet as AdaptiveSheetImpl

/**
 * Sheet with adaptive position aligned to bottom on small screen, otherwise aligned to center
 * and will not be able to dismissed with swipe gesture.
 *
 * Max width of the content is set to 460 dp.
 */
@Composable
fun AdaptiveSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    enableImplicitDismiss: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isTabletUi = isTabletUi()

    BokuhonDialogLayer {
        AdaptiveSheetImpl(
            isTabletUi = isTabletUi,
            enableImplicitDismiss = enableImplicitDismiss,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
        ) {
            content()
        }
    }
}
