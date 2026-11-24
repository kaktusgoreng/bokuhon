package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

enum class FabPosition {
    End,
    Center,
    Start,
}

@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState(),
    ),
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = LocalBokuhonColors.current.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
    content: @Composable (PaddingValues) -> Unit,
) {
    var topBarHeight by remember { mutableIntStateOf(0) }
    var bottomBarHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val insets = contentWindowInsets.asPaddingValues(density)

    val topBarHeightDp = with(density) { topBarHeight.toDp() }
    val bottomBarHeightDp = with(density) { bottomBarHeight.toDp() }

    val contentPadding = PaddingValues(
        start = insets.calculateStartPadding(layoutDirection),
        top = if (topBarHeight > 0) topBarHeightDp else insets.calculateTopPadding(),
        end = insets.calculateEndPadding(layoutDirection),
        bottom = if (bottomBarHeight > 0) bottomBarHeightDp else insets.calculateBottomPadding(),
    )

    Box(
        modifier = modifier
            .background(containerColor)
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
    ) {
        CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) {
            content(contentPadding)
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = it.height },
        ) {
            topBar(topBarScrollBehavior)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .onSizeChanged { bottomBarHeight = it.height },
        ) {
            bottomBar()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = bottomBarHeightDp),
        ) {
            snackbarHost()
        }

        Box(
            modifier = Modifier
                .align(
                    when (floatingActionButtonPosition) {
                        FabPosition.End -> Alignment.BottomEnd
                        FabPosition.Center -> Alignment.BottomCenter
                        FabPosition.Start -> Alignment.BottomStart
                    },
                )
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = bottomBarHeightDp + 16.dp, start = 16.dp, end = 16.dp),
        ) {
            floatingActionButton()
        }
    }
}
