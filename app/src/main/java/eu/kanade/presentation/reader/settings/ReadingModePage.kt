package eu.kanade.presentation.reader.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import eu.kanade.domain.manga.model.readerOrientation
import eu.kanade.domain.manga.model.readingMode
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import eu.kanade.tachiyomi.ui.reader.viewer.webgpu.WebGpuViewerSettings
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.DropdownItem
import tachiyomi.presentation.core.components.HeadingItem
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import java.text.NumberFormat

@Composable
internal fun ColumnScope.ReadingModePage(viewModel: ReaderSettingsViewModel) {
    val isWebGpu by viewModel.isWebGpu.collectAsState()
    if (isWebGpu) {
        WebGpuViewerSettings(viewModel)
        return
    }

    HeadingItem(MR.strings.pref_category_for_this_series)
    val manga by viewModel.mangaFlow.collectAsState()
    val readingMode = remember(manga) { ReadingMode.fromPreference(manga?.readingMode?.toInt()) }
    DropdownItem(
        label = stringResource(MR.strings.pref_category_reading_mode),
        options = ReadingMode.entries.map { stringResource(it.stringRes) },
        selectedIndex = ReadingMode.entries.indexOf(readingMode),
        onSelect = { viewModel.setReadingMode(ReadingMode.entries[it]) },
    )

    val orientation = remember(manga) { ReaderOrientation.fromPreference(manga?.readerOrientation?.toInt()) }
    DropdownItem(
        label = stringResource(MR.strings.rotation_type),
        options = ReaderOrientation.entries.map { stringResource(it.stringRes) },
        selectedIndex = ReaderOrientation.entries.indexOf(orientation),
        onSelect = { viewModel.setOrientation(ReaderOrientation.entries[it]) },
    )

    val resolvedReadingMode by viewModel.readingMode.collectAsState()
    if (resolvedReadingMode.type is ReadingMode.ViewerType.Webtoon) {
        WebtoonViewerSettings(viewModel)
    } else {
        PagerViewerSettings(viewModel)
    }
}

@Composable
private fun ColumnScope.PagerViewerSettings(viewModel: ReaderSettingsViewModel) {
    HeadingItem(MR.strings.pager_viewer)

    val navigationModePager by viewModel.preferences.navigationModePager.collectAsState()
    val pagerNavInverted by viewModel.preferences.pagerNavInverted.collectAsState()
    TapZonesItems(
        selected = navigationModePager,
        onSelect = viewModel.preferences.navigationModePager::set,
        invertMode = pagerNavInverted,
        onSelectInvertMode = viewModel.preferences.pagerNavInverted::set,
    )

    val imageScaleType by viewModel.preferences.imageScaleType.collectAsState()
    DropdownItem(
        label = stringResource(MR.strings.pref_image_scale_type),
        options = ReaderPreferences.ImageScaleType.map { stringResource(it) },
        selectedIndex = imageScaleType - 1,
        onSelect = { viewModel.preferences.imageScaleType.set(it + 1) },
    )

    val zoomStart by viewModel.preferences.zoomStart.collectAsState()
    DropdownItem(
        label = stringResource(MR.strings.pref_zoom_start),
        options = ReaderPreferences.ZoomStart.map { stringResource(it) },
        selectedIndex = zoomStart - 1,
        onSelect = { viewModel.preferences.zoomStart.set(it + 1) },
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_crop_borders),
        pref = viewModel.preferences.cropBorders,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_landscape_zoom),
        pref = viewModel.preferences.landscapeZoom,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_navigate_pan),
        pref = viewModel.preferences.navigateToPan,
    )

    val dualPageSplitPaged by viewModel.preferences.dualPageSplitPaged.collectAsState()
    CheckboxItem(
        label = stringResource(MR.strings.pref_dual_page_split),
        pref = viewModel.preferences.dualPageSplitPaged,
    )

    if (dualPageSplitPaged) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_dual_page_invert),
            pref = viewModel.preferences.dualPageInvertPaged,
        )
    }

    val dualPageRotateToFit by viewModel.preferences.dualPageRotateToFit.collectAsState()
    CheckboxItem(
        label = stringResource(MR.strings.pref_page_rotate),
        pref = viewModel.preferences.dualPageRotateToFit,
    )

    if (dualPageRotateToFit) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_page_rotate_invert),
            pref = viewModel.preferences.dualPageRotateToFitInvert,
        )
    }
}

@Composable
private fun ColumnScope.WebtoonViewerSettings(viewModel: ReaderSettingsViewModel) {
    val numberFormat = remember { NumberFormat.getPercentInstance() }

    HeadingItem(MR.strings.webtoon_viewer)

    val navigationModeWebtoon by viewModel.preferences.navigationModeWebtoon.collectAsState()
    val webtoonNavInverted by viewModel.preferences.webtoonNavInverted.collectAsState()
    TapZonesItems(
        selected = navigationModeWebtoon,
        onSelect = viewModel.preferences.navigationModeWebtoon::set,
        invertMode = webtoonNavInverted,
        onSelectInvertMode = viewModel.preferences.webtoonNavInverted::set,
    )

    val webtoonSidePadding by viewModel.preferences.webtoonSidePadding.collectAsState()
    SliderItem(
        value = webtoonSidePadding,
        valueRange = ReaderPreferences.let { it.WEBTOON_PADDING_MIN..it.WEBTOON_PADDING_MAX },
        label = stringResource(MR.strings.pref_webtoon_side_padding),
        valueString = numberFormat.format(webtoonSidePadding / 100f),
        onChange = {
            viewModel.preferences.webtoonSidePadding.set(it)
        },
        pillColor = LocalBokuhonColors.current.surfaceContainerHighest,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_crop_borders),
        pref = viewModel.preferences.cropBordersWebtoon,
    )

    val dualPageSplitWebtoon by viewModel.preferences.dualPageSplitWebtoon.collectAsState()
    CheckboxItem(
        label = stringResource(MR.strings.pref_dual_page_split),
        pref = viewModel.preferences.dualPageSplitWebtoon,
    )

    if (dualPageSplitWebtoon) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_dual_page_invert),
            pref = viewModel.preferences.dualPageInvertWebtoon,
        )
    }

    val dualPageRotateToFitWebtoon by viewModel.preferences.dualPageRotateToFitWebtoon.collectAsState()
    CheckboxItem(
        label = stringResource(MR.strings.pref_page_rotate),
        pref = viewModel.preferences.dualPageRotateToFitWebtoon,
    )

    if (dualPageRotateToFitWebtoon) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_page_rotate_invert),
            pref = viewModel.preferences.dualPageRotateToFitInvertWebtoon,
        )
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_double_tap_zoom),
        pref = viewModel.preferences.webtoonDoubleTapZoomEnabled,
    )
    CheckboxItem(
        label = stringResource(MR.strings.pref_webtoon_disable_zoom_out),
        pref = viewModel.preferences.webtoonDisableZoomOut,
    )
}

@Composable
internal fun TapZonesItems(
    selected: Int,
    onSelect: (Int) -> Unit,
    invertMode: ReaderPreferences.TappingInvertMode,
    onSelectInvertMode: (ReaderPreferences.TappingInvertMode) -> Unit,
) {
    DropdownItem(
        label = stringResource(MR.strings.pref_viewer_nav),
        options = ReaderPreferences.TapZones.map { stringResource(it) },
        selectedIndex = selected,
        onSelect = onSelect,
    )

    if (selected != 5) {
        DropdownItem(
            label = stringResource(MR.strings.pref_read_with_tapping_inverted),
            options = ReaderPreferences.TappingInvertMode.entries.map { stringResource(it.titleRes) },
            selectedIndex = ReaderPreferences.TappingInvertMode.entries.indexOf(invertMode),
            onSelect = { onSelectInvertMode(ReaderPreferences.TappingInvertMode.entries[it]) },
        )
    }
}
