package tachiyomi.presentation.core.bokuhon

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import tachiyomi.presentation.core.R

@Composable
private fun DisableDialogWindowAnimation() {
    val view = LocalView.current
    SideEffect {
        var parent: android.view.ViewParent? = view.parent
        while (parent != null) {
            if (parent is DialogWindowProvider) {
                parent.window?.setWindowAnimations(0)
                break
            }
            parent = (parent as? android.view.View)?.parent
        }
    }
}

@Composable
private fun rememberNoAnimationDialogContext(): Context {
    val context = LocalContext.current
    return remember(context) { androidx.appcompat.view.ContextThemeWrapper(context, R.style.Theme_Bokuhon_Dialog) }
}

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = RectangleShape,
    containerColor: Color = LocalBokuhonColors.current.surfaceVariant,
    contentColor: Color = LocalBokuhonColors.current.onSurface,
    tonalElevation: Dp = 0.dp,
    properties: DialogProperties = DialogProperties(),
) {
    val dialogContext = rememberNoAnimationDialogContext()
    CompositionLocalProvider(LocalContext provides dialogContext) {
        Dialog(onDismissRequest = onDismissRequest, properties = properties) {
            DisableDialogWindowAnimation()
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .sizeIn(minWidth = 280.dp),
                shape = shape,
                color = containerColor,
                contentColor = contentColor,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    icon?.let {
                        CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) {
                            Box(modifier = Modifier.padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                                it()
                            }
                        }
                    }
                    title?.let {
                        CompositionLocalProvider(
                            LocalBokuhonContentColor provides contentColor,
                            LocalBokuhonTextStyle provides LocalBokuhonTypography.current.headlineSmall,
                        ) {
                            Column(modifier = Modifier.padding(bottom = 16.dp)) { it() }
                        }
                    }
                    text?.let {
                        CompositionLocalProvider(
                            LocalBokuhonContentColor provides contentColor,
                            LocalBokuhonTextStyle provides LocalBokuhonTypography.current.bodyMedium,
                        ) {
                            Column(modifier = Modifier.padding(bottom = 24.dp)) { it() }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
fun BasicAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    val dialogContext = rememberNoAnimationDialogContext()
    CompositionLocalProvider(LocalContext provides dialogContext) {
        Dialog(onDismissRequest = onDismissRequest, properties = properties) {
            DisableDialogWindowAnimation()
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .sizeIn(minWidth = 280.dp),
                color = LocalBokuhonColors.current.surfaceVariant,
            ) {
                content()
            }
        }
    }
}
