package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

@Stable
class PullToRefreshState {
    var distanceFraction: Float by mutableFloatStateOf(0f)
}

@Composable
fun rememberPullToRefreshState(): PullToRefreshState = remember { PullToRefreshState() }

fun Modifier.pullToRefresh(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    enabled: Boolean = true,
    threshold: Dp = 80.dp,
    onRefresh: () -> Unit,
): Modifier = composed {
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    nestedScroll(
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (!enabled || isRefreshing) return Offset.Zero
                if (available.y > 0f) {
                    state.distanceFraction = (state.distanceFraction + available.y / thresholdPx).coerceIn(0f, 1f)
                    return Offset(0f, available.y)
                }
                if (state.distanceFraction > 0f) {
                    state.distanceFraction = (state.distanceFraction + available.y / thresholdPx).coerceIn(0f, 1f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (enabled && !isRefreshing && state.distanceFraction >= 1f) {
                    onRefresh()
                }
                state.distanceFraction = 0f
                return Velocity.Zero
            }
        },
    )
}

@Composable
fun PullToRefreshIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalBokuhonColors.current.surfaceVariant,
    color: Color = LocalBokuhonColors.current.onSurfaceVariant,
) {
    if (!isRefreshing && state.distanceFraction <= 0f) return
    Box(modifier = modifier.size(48.dp)) {
        if (isRefreshing) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = color)
        } else {
            Surface(modifier = Modifier.size(32.dp), color = containerColor) {
                Box(modifier = Modifier.size(32.dp)) {
                    CircularProgressIndicator(
                        progress = { state.distanceFraction },
                        modifier = Modifier.size(32.dp),
                        color = color,
                    )
                }
            }
        }
    }
}
