package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.dp

@Stable
class TopAppBarState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f,
) {
    var heightOffset by mutableFloatStateOf(initialHeightOffset)
    var contentOffset by mutableFloatStateOf(initialContentOffset)
    var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)

    val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) {
            (heightOffset / heightOffsetLimit).coerceIn(0f, 1f)
        } else {
            0f
        }
}

@Composable
fun rememberTopAppBarState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f,
): TopAppBarState = remember {
    TopAppBarState(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset)
}

@Stable
interface TopAppBarScrollBehavior {
    val state: TopAppBarState
    val isPinned: Boolean
    val nestedScrollConnection: NestedScrollConnection
}

@Stable
class PinnedTopAppBarScrollBehavior(override val state: TopAppBarState) : TopAppBarScrollBehavior {
    override val isPinned: Boolean get() = true
    override val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {}
}

@Immutable
data class TopAppBarColors(
    val containerColor: Color,
    val titleContentColor: Color,
    val navigationIconContentColor: Color,
    val actionIconContentColor: Color,
)

object TopAppBarDefaults {
    @Composable
    fun pinnedScrollBehavior(state: TopAppBarState = rememberTopAppBarState()): TopAppBarScrollBehavior =
        PinnedTopAppBarScrollBehavior(state)

    @Composable
    fun topAppBarColors(
        containerColor: Color = LocalBokuhonColors.current.surface,
        scrolledContainerColor: Color = containerColor,
        navigationIconContentColor: Color = LocalBokuhonColors.current.onSurface,
        titleContentColor: Color = LocalBokuhonColors.current.onSurface,
        actionIconContentColor: Color = LocalBokuhonColors.current.onSurface,
    ): TopAppBarColors = TopAppBarColors(
        containerColor = containerColor,
        titleContentColor = titleContentColor,
        navigationIconContentColor = navigationIconContentColor,
        actionIconContentColor = actionIconContentColor,
    )

    val windowInsets: WindowInsets
        @Composable get() = WindowInsets.statusBars
}

@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.containerColor)
            .windowInsetsPadding(windowInsets),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalBokuhonContentColor provides colors.navigationIconContentColor) {
                navigationIcon()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                CompositionLocalProvider(
                    LocalBokuhonContentColor provides colors.titleContentColor,
                    LocalBokuhonTextStyle provides LocalBokuhonTypography.current.titleLarge,
                ) {
                    title()
                }
            }
            CompositionLocalProvider(LocalBokuhonContentColor provides colors.actionIconContentColor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
                    content = actions,
                )
            }
        }
    }
}
