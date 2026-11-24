package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.bokuhon.LocalBokuhonContentColor

@Immutable
data class ListItemColors(val containerColor: Color)

object ListItemDefaults {
    @Composable
    fun colors(containerColor: Color = LocalBokuhonColors.current.surface): ListItemColors =
        ListItemColors(containerColor)
}

@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
) {
    ListItemLayout(
        headlineContent = headlineContent,
        modifier = modifier,
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
    )
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    ListItemLayout(
        headlineContent = content,
        modifier = modifier,
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
    )
}

@Composable
private fun ListItemLayout(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier,
    overlineContent: (@Composable () -> Unit)?,
    supportingContent: (@Composable () -> Unit)?,
    leadingContent: (@Composable () -> Unit)?,
    trailingContent: (@Composable () -> Unit)?,
    colors: ListItemColors,
) {
    val typography = LocalBokuhonTypography.current
    val contentColor = LocalBokuhonColors.current.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.containerColor)
            .heightIn(min = if (supportingContent != null || overlineContent != null) 72.dp else 56.dp)
            .padding(
                horizontal = 16.dp,
                vertical = if (supportingContent != null ||
                    overlineContent != null
                ) {
                    12.dp
                } else {
                    8.dp
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.let {
            Box(modifier = Modifier.padding(end = 16.dp)) {
                CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) { it() }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            overlineContent?.let {
                CompositionLocalProvider(
                    LocalBokuhonContentColor provides contentColor,
                    LocalBokuhonTextStyle provides typography.labelSmall,
                    content = it,
                )
            }
            CompositionLocalProvider(
                LocalBokuhonContentColor provides contentColor,
                LocalBokuhonTextStyle provides typography.bodyLarge,
                content = headlineContent,
            )
            supportingContent?.let {
                CompositionLocalProvider(
                    LocalBokuhonContentColor provides contentColor,
                    LocalBokuhonTextStyle provides typography.bodyMedium,
                    content = it,
                )
            }
        }
        trailingContent?.let {
            Box(modifier = Modifier.padding(start = 16.dp)) {
                CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) { it() }
            }
        }
    }
}
