package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import eu.kanade.core.preference.PreferenceMutableState
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibraryManga

@Composable
fun LibraryContent(
    categories: List<Category>,
    searchQuery: String?,
    selection: Set<Long>,
    contentPadding: PaddingValues,
    currentPage: Int,
    hasActiveFilters: Boolean,
    onChangeCurrentPage: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onContinueReadingClicked: ((LibraryManga) -> Unit)?,
    onToggleSelection: (Category, LibraryManga) -> Unit,
    onToggleRangeSelection: (Category, LibraryManga) -> Unit,
    onGlobalSearchClicked: () -> Unit,
    getDisplayMode: (Int) -> PreferenceMutableState<LibraryDisplayMode>,
    getColumnsForOrientation: (Boolean) -> PreferenceMutableState<Int>,
    getItemsForCategory: (Category) -> List<LibraryItem>,
) {
    Column(
        modifier = Modifier.padding(
            top = contentPadding.calculateTopPadding(),
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
        ),
    ) {
        val pagerState = rememberPagerState(currentPage) { categories.size }

        LaunchedEffect(categories) {
            if (categories.isNotEmpty() && categories.size <= pagerState.currentPage) {
                pagerState.scrollToPage(categories.lastIndex)
            }
        }

        // Picks up a category chosen from the toolbar's menu
        LaunchedEffect(currentPage, categories) {
            if (currentPage in categories.indices && currentPage != pagerState.currentPage) {
                pagerState.scrollToPage(currentPage)
            }
        }

        LibraryPager(
            state = pagerState,
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
            hasActiveFilters = hasActiveFilters,
            selection = selection,
            searchQuery = searchQuery,
            onGlobalSearchClicked = onGlobalSearchClicked,
            getCategoryForPage = { page -> categories[page] },
            getDisplayMode = getDisplayMode,
            getColumnsForOrientation = getColumnsForOrientation,
            getItemsForCategory = getItemsForCategory,
            onClickManga = { category, manga ->
                if (selection.isNotEmpty()) {
                    onToggleSelection(category, manga)
                } else {
                    onClickManga(manga.manga.id)
                }
            },
            onLongClickManga = onToggleRangeSelection,
            onClickContinueReading = onContinueReadingClicked,
        )

        LaunchedEffect(pagerState.currentPage) {
            onChangeCurrentPage(pagerState.currentPage)
        }
    }
}
