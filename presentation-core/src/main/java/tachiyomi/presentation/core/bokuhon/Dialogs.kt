package tachiyomi.presentation.core.bokuhon

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
private fun DialogOverlay(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit,
) {
    val dismiss = rememberUpdatedState(onDismissRequest)
    BokuhonDialogLayer {
        val colors = LocalBokuhonColors.current
        val interaction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrim.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = properties.dismissOnClickOutside,
                    onClick = { dismiss.value() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            BackHandler(enabled = properties.dismissOnBackPress) { dismiss.value() }
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = {},
                    ),
            ) {
                content()
            }
        }
    }
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
    DialogOverlay(onDismissRequest = onDismissRequest, properties = properties) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .sizeIn(minWidth = 280.dp),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = BorderStroke(1.dp, LocalBokuhonColors.current.onSurface),
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
