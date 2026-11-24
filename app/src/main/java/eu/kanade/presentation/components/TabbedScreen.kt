package eu.kanade.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.zIndex
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.launch
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.PrimaryTabRow
import tachiyomi.presentation.core.bokuhon.SnackbarHost
import tachiyomi.presentation.core.bokuhon.SnackbarHostState
import tachiyomi.presentation.core.bokuhon.Tab
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.TabText
import tachiyomi.presentation.core.components.motion.pagerFlingBehavior
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun TabbedScreen(
    titleRes: StringResource,
    tabs: List<TabContent>,
    state: PagerState = rememberPagerState { tabs.size },
    searchQuery: String? = null,
    onChangeSearchQuery: (String?) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            val tab = tabs[state.currentPage]
            val searchEnabled = tab.searchEnabled

            SearchToolbar(
                titleContent = { AppBarTitle(stringResource(titleRes)) },
                searchEnabled = searchEnabled,
                searchQuery = if (searchEnabled) searchQuery else null,
                onChangeSearchQuery = onChangeSearchQuery,
                actions = { AppBarActions(tab.actions) },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
            ),
        ) {
            PrimaryTabRow(
                selectedTabIndex = state.currentPage,
                modifier = Modifier.zIndex(1f),
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.currentPage == index,
                        onClick = { scope.launch { state.scrollToPage(index) } },
                        text = { TabText(text = stringResource(tab.titleRes), badgeCount = tab.badgeNumber) },
                        unselectedContentColor = LocalBokuhonColors.current.onSurface,
                    )
                }
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = state,
                verticalAlignment = Alignment.Top,
                flingBehavior = pagerFlingBehavior(state),
            ) { page ->
                tabs[page].content(
                    PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                    snackbarHostState,
                )
            }
        }
    }
}

data class TabContent(
    val titleRes: StringResource,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)
