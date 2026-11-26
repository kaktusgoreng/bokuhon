package eu.kanade.presentation.reader.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.util.system.OnyxEpd
import eu.kanade.tachiyomi.util.system.hasDisplayCutout
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.FilterChip
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.DropdownItem
import tachiyomi.presentation.core.components.MultiDropdownItem
import tachiyomi.presentation.core.components.SettingsChipRow
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

private val themes = listOf(
    MR.strings.black_background to 1,
    MR.strings.gray_background to 2,
    MR.strings.white_background to 0,
    MR.strings.automatic_background to 3,
)

private val flashColors = listOf(
    MR.strings.pref_flash_style_black to ReaderPreferences.FlashColor.BLACK,
    MR.strings.pref_flash_style_white to ReaderPreferences.FlashColor.WHITE,
    MR.strings.pref_flash_style_white_black to ReaderPreferences.FlashColor.WHITE_BLACK,
)

@Composable
internal fun ColumnScope.GeneralPage(viewModel: ReaderSettingsViewModel) {
    val readerTheme by viewModel.preferences.readerTheme.collectAsState()

    val flashPageState by viewModel.preferences.flashOnPageChange.collectAsState()

    // The same rules as Settings -> Reader -> E-Ink: where the panel refreshes the page itself
    // there is nothing for the drawn flash to do, and only its cadence still applies.
    val onyxRefreshModes by viewModel.preferences.onyxRefreshModes.collectAsState()
    val panelRefreshes = DeviceUtil.isOnyx && OnyxEpd.isSupported && onyxRefreshModes

    val flashMillisPref = viewModel.preferences.flashDurationMillis
    val flashMillis by flashMillisPref.collectAsState()

    val flashIntervalPref = viewModel.preferences.flashPageInterval
    val flashInterval by flashIntervalPref.collectAsState()

    val flashColorPref = viewModel.preferences.flashColor
    val flashColor by flashColorPref.collectAsState()

    DropdownItem(
        label = stringResource(MR.strings.pref_reader_theme),
        options = themes.map { stringResource(it.first) },
        selectedIndex = themes.indexOfFirst { it.second == readerTheme },
        onSelect = { viewModel.preferences.readerTheme.set(themes[it].second) },
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_show_page_number),
        pref = viewModel.preferences.showPageNumber,
    )

    val verticalNavigatorModes by viewModel.preferences.verticalNavigator.collectAsState()

    val navigatorModes = ReadingMode.entries.filter { it != ReadingMode.DEFAULT }
    MultiDropdownItem(
        label = stringResource(MR.strings.pref_vertical_navigator),
        options = navigatorModes.map { stringResource(it.stringRes) },
        selectedIndices = navigatorModes.mapIndexedNotNull { index, mode ->
            index.takeIf { verticalNavigatorModes.contains(mode) }
        }.toSet(),
        onToggle = { index ->
            val mode = navigatorModes[index]
            val newModes = if (verticalNavigatorModes.contains(mode)) {
                verticalNavigatorModes - mode
            } else {
                verticalNavigatorModes + mode
            }
            viewModel.preferences.verticalNavigator.set(newModes)
        },
    )

    if (verticalNavigatorModes.isNotEmpty()) {
        val verticalNavigatorHeightPref = viewModel.preferences.verticalNavigatorHeight
        val verticalNavigatorHeight by verticalNavigatorHeightPref.collectAsState()

        CheckboxItem(
            label = stringResource(MR.strings.pref_webtoon_vertical_navigator_on_left),
            pref = viewModel.preferences.verticalNavigatorOnLeft,
        )

        SliderItem(
            label = stringResource(MR.strings.pref_vertical_navigator_height),
            value = verticalNavigatorHeight,
            valueRange = 65..100,
            steps = 6,
            onChange = { verticalNavigatorHeightPref.set(it) },
        )
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_fullscreen),
        pref = viewModel.preferences.fullscreen,
    )

    val isFullscreen by viewModel.preferences.fullscreen.collectAsState()
    if (LocalActivity.current?.hasDisplayCutout() == true && isFullscreen) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_cutout_short),
            pref = viewModel.preferences.drawUnderCutout,
        )
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_keep_screen_on),
        pref = viewModel.preferences.keepScreenOn,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_read_with_long_tap),
        pref = viewModel.preferences.readWithLongTap,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_always_show_chapter_transition),
        pref = viewModel.preferences.alwaysShowChapterTransition,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_page_transitions),
        pref = viewModel.preferences.pageTransitions,
    )

    if (DeviceUtil.isOnyx) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_onyx_refresh_modes),
            pref = viewModel.preferences.onyxRefreshModes,
        )
    }
    if (!panelRefreshes) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_flash_page),
            pref = viewModel.preferences.flashOnPageChange,
        )
    }
    if (flashPageState && !panelRefreshes) {
        SliderItem(
            value = flashMillis / ReaderPreferences.MILLI_CONVERSION,
            valueRange = 1..15,
            label = stringResource(MR.strings.pref_flash_duration),
            valueString = stringResource(MR.strings.pref_flash_duration_summary, flashMillis),
            onChange = { flashMillisPref.set(it * ReaderPreferences.MILLI_CONVERSION) },
            pillColor = LocalBokuhonColors.current.surfaceContainerHighest,
        )
    }
    if (flashPageState || panelRefreshes) {
        SliderItem(
            value = flashInterval,
            valueRange = 1..10,
            label = stringResource(MR.strings.pref_flash_page_interval),
            valueString = pluralStringResource(MR.plurals.pref_pages, flashInterval, flashInterval),
            onChange = {
                flashIntervalPref.set(it)
            },
            pillColor = LocalBokuhonColors.current.surfaceContainerHighest,
        )
    }
    if (flashPageState && !panelRefreshes) {
        SettingsChipRow(MR.strings.pref_flash_with) {
            flashColors.map { (labelRes, value) ->
                FilterChip(
                    selected = flashColor == value,
                    onClick = { flashColorPref.set(value) },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }
    }
}
