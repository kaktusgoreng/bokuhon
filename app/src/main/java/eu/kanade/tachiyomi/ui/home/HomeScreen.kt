package eu.kanade.tachiyomi.ui.home

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import eu.kanade.presentation.util.rememberAnimatedVectorPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.BrowseShowExtensionsEvent
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibrarySearchEvent
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import mihon.app.di.appGraph
import mihon.core.navigation.DownloadQueueRoute
import mihon.core.navigation.LocalNavigator
import mihon.core.navigation.MangaRoute
import mihon.core.navigation.Navigator
import mihon.core.navigation.NavigatorDisplay
import mihon.core.navigation.rememberNavigator
import mihon.presentation.core.util.isTabletUi
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.BokuhonBadge
import tachiyomi.presentation.core.bokuhon.Icon
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Serializable
sealed interface TopLevelRoute : NavKey {

    val reselectEventKey: String
        get() = "TabReselectEvent:$this"

    @Composable
    fun options(isSelected: Boolean): TabOptions

    @Serializable
    data object Library : TopLevelRoute {
        @Composable
        override fun options(isSelected: Boolean): TabOptions {
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_library)
            return TabOptions(
                title = stringResource(MR.strings.label_library),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }
    }

    @Serializable
    data object Updates : TopLevelRoute {
        @Composable
        override fun options(isSelected: Boolean): TabOptions {
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_updates)
            return TabOptions(
                title = stringResource(MR.strings.label_recent_updates),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }
    }

    @Serializable
    data object History : TopLevelRoute {
        @Composable
        override fun options(isSelected: Boolean): TabOptions {
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_history)
            return TabOptions(
                title = stringResource(MR.strings.label_recent_manga),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }
    }

    @Serializable
    data object Browse : TopLevelRoute {
        @Composable
        override fun options(isSelected: Boolean): TabOptions {
            val enter = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_enter)
            val exit = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_exit)
            return TabOptions(
                title = stringResource(MR.strings.browse),
                icon = rememberAnimatedVectorPainter(enter, exit, isSelected),
            )
        }
    }

    @Serializable
    data object More : TopLevelRoute {
        @Composable
        override fun options(isSelected: Boolean): TabOptions {
            val enter = AnimatedImageVector.animatedVectorResource(R.drawable.animated_more_enter)
            val exit = AnimatedImageVector.animatedVectorResource(R.drawable.animated_more_exit)
            return TabOptions(
                title = stringResource(MR.strings.label_more),
                icon = rememberAnimatedVectorPainter(enter, exit, isSelected),
            )
        }
    }
}

@Composable
fun HomeScreen() {
    val tabNavigator = rememberNavigator(TopLevelRoute.Library)
    val navigator = LocalNavigator.current
    val resultEventBus = LocalResultEventBus.current
    val tabletUi = isTabletUi()
    var bottomBarVisible by rememberSaveable { mutableStateOf(true) }

    ResultEffect<ShowBottomNavEvent> {
        bottomBarVisible = tabletUi || it.visible
    }

    if (tabletUi) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabletNavigationRail(tabNavigator)
            HomeContent(tabNavigator, Modifier.weight(1f))
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeContent(tabNavigator, Modifier.weight(1f))
            if (bottomBarVisible) {
                BottomNavigationBar(tabNavigator)
            }
        }
    }

    ResultEffect<TabEvent> {
        val route = when (it) {
            is TabEvent.Library -> {
                if (it.query != null) resultEventBus.sendResult(LibrarySearchEvent(it.query))
                TopLevelRoute.Library
            }
            TabEvent.Updates -> TopLevelRoute.Updates
            TabEvent.History -> TopLevelRoute.History
            is TabEvent.Browse -> {
                if (it.toExtensions) resultEventBus.sendResult(BrowseShowExtensionsEvent)
                TopLevelRoute.Browse
            }
            is TabEvent.More -> TopLevelRoute.More
        }
        tabNavigator.replaceAll(route)

        if (it is TabEvent.Library && it.mangaIdToOpen != null) {
            navigator.push(MangaRoute(it.mangaIdToOpen))
        }
        if (it is TabEvent.More && it.toDownloads) {
            navigator.push(DownloadQueueRoute)
        }
    }
}

@Composable
private fun HomeContent(tabNavigator: Navigator, modifier: Modifier = Modifier) {
    val tabStateHolder = rememberSaveableStateHolder()
    NavigatorDisplay(
        navigator = tabNavigator,
        modifier = modifier,
        entryDecorators = listOf(
            remember(tabStateHolder) {
                NavEntryDecorator { entry ->
                    tabStateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() }
                }
            },
        ),
        transitionSpec = { spec },
        popTransitionSpec = { spec },
        predictivePopTransitionSpec = { spec },
        entryProvider = entryProvider {
            entry<TopLevelRoute.Library> {
                LibraryTab()
            }

            entry<TopLevelRoute.Updates> {
                UpdatesTab()
            }

            entry<TopLevelRoute.History> {
                HistoryTab()
            }

            entry<TopLevelRoute.Browse> {
                BrowseTab()
            }

            entry<TopLevelRoute.More> {
                MoreTab()
            }
        },
    )
}

@Composable
private fun TabletNavigationRail(tabNavigator: Navigator) {
    val colors = LocalBokuhonColors.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(88.dp)
            .background(colors.container),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TABS.fastForEach { tab ->
            val resultEventBus = LocalResultEventBus.current
            val selected = tabNavigator.current == tab
            val options = tab.options(selected)
            val contentColor = if (selected) colors.onPrimary else colors.onSurface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = {
                            if (!selected) {
                                tabNavigator.replaceAll(tab)
                            } else {
                                resultEventBus.sendResult(resultKey = tab.reselectEventKey, result = Unit)
                            }
                        },
                    )
                    .background(if (selected) colors.primary else Color.Transparent)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Icon(
                        painter = options.icon,
                        contentDescription = options.title,
                        tint = contentColor,
                    )
                    TabBadge(tab, Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-6).dp))
                }
                Text(
                    text = options.title,
                    style = LocalBokuhonTypography.current.labelSmall,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(tabNavigator: Navigator) {
    val colors = LocalBokuhonColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.container)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(BottomBarHeight),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TABS.fastForEach { tab ->
            val resultEventBus = LocalResultEventBus.current
            val selected = tabNavigator.current == tab
            val options = tab.options(selected)
            val contentColor = if (selected) colors.onPrimary else colors.onSurface
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = {
                            if (!selected) {
                                tabNavigator.replaceAll(tab)
                            } else {
                                resultEventBus.sendResult(resultKey = tab.reselectEventKey, result = Unit)
                            }
                        },
                    )
                    .background(if (selected) colors.primary else Color.Transparent)
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box {
                    Icon(
                        painter = options.icon,
                        contentDescription = options.title,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp),
                    )
                    TabBadge(tab, Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-4).dp))
                }
                Text(
                    text = options.title,
                    style = LocalBokuhonTypography.current.labelSmall,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TabBadge(tab: TopLevelRoute, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val count by produceState(initialValue = 0, tab) {
        val graph = context.appGraph
        when (tab) {
            is TopLevelRoute.Updates -> {
                combine(
                    graph.libraryPreferences.newShowUpdatesCount.changes(),
                    graph.libraryPreferences.newUpdatesCount.changes(),
                ) { show, count ->
                    if (show) count else 0
                }
                    .collectLatest { value = it }
            }

            is TopLevelRoute.Browse -> {
                graph.sourcePreferences.extensionUpdatesCount.changes()
                    .collectLatest { value = it }
            }

            else -> value = 0
        }
    }
    if (count <= 0) return

    val desc = when (tab) {
        is TopLevelRoute.Updates -> pluralStringResource(
            MR.plurals.notification_chapters_generic,
            count = count,
            count,
        )

        is TopLevelRoute.Browse -> pluralStringResource(
            MR.plurals.update_check_notification_ext_updates,
            count = count,
            count,
        )

        else -> null
    }

    BokuhonBadge(
        text = count.toString(),
        modifier = modifier.semantics { if (desc != null) contentDescription = desc },
    )
}

sealed interface TabEvent {
    data class Library(val mangaIdToOpen: Long? = null, val query: String? = null) : TabEvent
    data object Updates : TabEvent
    data object History : TabEvent
    data class Browse(val toExtensions: Boolean = false) : TabEvent
    data class More(val toDownloads: Boolean) : TabEvent
}

data class ShowBottomNavEvent(val visible: Boolean)

data class TabOptions(
    val title: String,
    val icon: Painter,
)

private val spec: ContentTransform = EnterTransition.None togetherWith ExitTransition.None

private val BottomBarHeight = 64.dp

private val TABS: List<TopLevelRoute> = listOf(
    TopLevelRoute.Library,
    TopLevelRoute.Updates,
    TopLevelRoute.History,
    TopLevelRoute.Browse,
    TopLevelRoute.More,
)
