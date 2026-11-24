package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalBokuhonColors.current.surface,
    contentColor: Color = LocalBokuhonColors.current.onSurface,
    tabs: @Composable () -> Unit,
) {
    val height = with(LocalDensity.current) { 48.dp.roundToPx() }
    Column(modifier = modifier.fillMaxWidth().background(containerColor)) {
        Layout(
            content = tabs,
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
        ) { measurables, constraints ->
            val tabWidth = if (measurables.isEmpty()) 0 else constraints.maxWidth / measurables.size
            val placeables = measurables.map {
                it.measure(Constraints.fixed(tabWidth, height))
            }
            layout(constraints.maxWidth, height) {
                var x = 0
                placeables.forEach {
                    it.place(x, 0)
                    x += tabWidth
                }
            }
        }
        HorizontalDivider(color = LocalBokuhonColors.current.outlineVariant)
    }
}

@Composable
fun PrimaryScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalBokuhonColors.current.surface,
    contentColor: Color = LocalBokuhonColors.current.onSurface,
    edgePadding: androidx.compose.ui.unit.Dp = 0.dp,
    tabs: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().background(containerColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = edgePadding)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) {
                tabs()
            }
        }
        HorizontalDivider(color = LocalBokuhonColors.current.outlineVariant)
    }
}

@Composable
fun Tab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    selectedContentColor: Color = LocalBokuhonColors.current.primary,
    unselectedContentColor: Color = LocalBokuhonColors.current.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
            )
            .padding(horizontal = 8.dp)
            .drawBehind {
                if (selected) {
                    val thickness = 2.dp.toPx()
                    drawRect(
                        color = selectedContentColor,
                        topLeft = Offset(0f, size.height - thickness),
                        size = Size(size.width, thickness),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalBokuhonContentColor provides if (selected) selectedContentColor else unselectedContentColor,
            LocalBokuhonTextStyle provides LocalBokuhonTypography.current.labelLarge,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                icon?.invoke()
                text?.invoke()
            }
        }
    }
}
