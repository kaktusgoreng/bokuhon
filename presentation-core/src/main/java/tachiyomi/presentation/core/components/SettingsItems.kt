package tachiyomi.presentation.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.icerock.moko.resources.StringResource
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ArrowDownward
import mihon.icons.materialsymbols.rounded.ArrowUpward
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.CheckBoxOutlineBlank
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import mihon.icons.materialsymbols.rounded.KeyboardArrowUp
import mihon.icons.materialsymbols.roundedfilled.CheckBox
import mihon.icons.materialsymbols.roundedfilled.DisabledByDefault
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.TriState
import tachiyomi.core.common.preference.toggle
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.Checkbox
import tachiyomi.presentation.core.bokuhon.DropdownMenuItem
import tachiyomi.presentation.core.bokuhon.ExposedDropdownMenuAnchorType
import tachiyomi.presentation.core.bokuhon.ExposedDropdownMenuBox
import tachiyomi.presentation.core.bokuhon.ExposedDropdownMenuDefaults
import tachiyomi.presentation.core.bokuhon.Icon
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonContentColor
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.OutlinedTextField
import tachiyomi.presentation.core.bokuhon.ProvideBokuhonTheme
import tachiyomi.presentation.core.bokuhon.RadioButton
import tachiyomi.presentation.core.bokuhon.Surface
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA
import tachiyomi.presentation.core.components.material.Slider
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.header
import tachiyomi.presentation.core.util.collectAsState
import tachiyomi.presentation.core.util.secondaryItemAlpha
import kotlin.math.roundToInt

object SettingsItemsPaddings {
    val Horizontal = 24.dp
    val Vertical = 6.dp
}

/** The box of [CheckboxItem] and [RadioItem], which the whole row is padded around. */
private val CheckboxSize = 32.dp

/** A comfortable target for a fingertip on a slow panel, and tall enough to read at arm's length. */
private val DropdownOptionHeight = 48.dp

@Composable
fun HeadingItem(labelRes: StringResource) {
    HeadingItem(stringResource(labelRes))
}

@Composable
fun HeadingItem(text: String) {
    Text(
        text = text,
        style = LocalBokuhonTypography.current.header,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsItemsPaddings.Horizontal,
                vertical = SettingsItemsPaddings.Vertical,
            ),
    )
}

@Composable
fun SortItem(label: String, sortDescending: Boolean?, onClick: () -> Unit) {
    val arrowIcon = when (sortDescending) {
        true -> MaterialSymbols.Rounded.ArrowDownward
        false -> MaterialSymbols.Rounded.ArrowUpward
        null -> null
    }

    BaseSortItem(
        label = label,
        icon = arrowIcon,
        onClick = onClick,
    )
}

@Composable
fun BaseSortItem(label: String, icon: ImageVector?, onClick: () -> Unit) {
    BaseSettingsItem(
        label = label,
        widget = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LocalBokuhonColors.current.primary,
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
        onClick = onClick,
    )
}

@Composable
fun CheckboxItem(label: String, pref: Preference<Boolean>) {
    val checked by pref.collectAsState()
    CheckboxItem(
        label = label,
        checked = checked,
        onClick = { pref.toggle() },
    )
}

@Composable
fun CheckboxItem(label: String, checked: Boolean, onClick: () -> Unit) {
    BaseSettingsItem(
        label = label,
        widget = {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                // The row is the touch target; the box itself does not need to be a 48dp target as
                // well, which is what made these rows read as a column of gaps.
                modifier = Modifier.size(CheckboxSize),
            )
        },
        onClick = onClick,
    )
}

@Composable
fun RadioItem(label: String, selected: Boolean, onClick: () -> Unit) {
    BaseSettingsItem(
        label = label,
        widget = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
        onClick = onClick,
    )
}

@Composable
fun SliderItem(
    value: Int,
    valueRange: IntProgression,
    label: String,
    onChange: (Int) -> Unit,
    steps: Int = with(valueRange) { (last - first) - 1 },
    valueString: String = value.toString(),
    labelStyle: TextStyle = LocalBokuhonTypography.current.bodyMedium,
    pillColor: Color = LocalBokuhonColors.current.surfaceContainerHigh,
) {
    BaseSliderItem(
        value = value,
        valueRange = valueRange,
        steps = steps,
        title = label,
        valueString = valueString,
        onChange = onChange,
        titleStyle = labelStyle,
        pillColor = pillColor,
        modifier = Modifier.padding(
            horizontal = SettingsItemsPaddings.Horizontal,
            vertical = SettingsItemsPaddings.Vertical,
        ),
    )
}

@Composable
fun BaseSliderItem(
    value: Int,
    valueRange: IntProgression,
    title: String,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    steps: Int = with(valueRange) { (last - first) - 1 },
    valueString: String = value.toString(),
    titleStyle: TextStyle = LocalBokuhonTypography.current.titleLarge,
    subtitleStyle: TextStyle = LocalBokuhonTypography.current.bodySmall,
    pillColor: Color = LocalBokuhonColors.current.surfaceContainerHigh,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BokuhonPadding.small),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = titleStyle,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = subtitleStyle,
                        modifier = Modifier.secondaryItemAlpha(),
                    )
                }
            }
            Pill(
                text = valueString,
                style = LocalBokuhonTypography.current.bodyMedium,
                color = pillColor,
            )
        }
        Slider(
            value = value,
            onValueChange = f@{
                if (it == value) return@f
                onChange(it)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
@PreviewLightDark
fun SliderItemPreview() {
    ProvideBokuhonTheme(isSystemInDarkTheme()) {
        var value by remember { mutableIntStateOf(0) }
        Surface {
            BaseSliderItem(
                value = value,
                valueRange = 0..10,
                title = "Item per row",
                valueString = if (value == 0) "Auto" else value.toString(),
                onChange = { value = it },
                modifier = Modifier.padding(
                    horizontal = SettingsItemsPaddings.Horizontal,
                    vertical = SettingsItemsPaddings.Vertical,
                ),
            )
        }
    }
}

/**
 * A setting shown as its current value, with the choices in a menu that drops under it when tapped:
 * shorter than a row of chips for anything with more than two or three options, which is most of the
 * reader's choices.
 */
@Composable
fun DropdownItem(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    DropdownItemFrame(
        label = label,
        value = options.getOrNull(selectedIndex).orEmpty(),
        expanded = expanded,
        onToggle = { expanded = !expanded },
        onDismiss = { expanded = false },
        options = options,
        isSelected = { it == selectedIndex },
        onOptionClick = { index ->
            onSelect(index)
            expanded = false
        },
    )
}

/**
 * The same row, for a setting that takes any number of choices: tapping one switches it and leaves
 * the menu open, so several can be picked in one go.
 */
@Composable
fun MultiDropdownItem(
    label: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val summary = options.filterIndexed { index, _ -> index in selectedIndices }
        .joinToString(", ")
        .ifEmpty { "None" }

    DropdownItemFrame(
        label = label,
        value = summary,
        expanded = expanded,
        onToggle = { expanded = !expanded },
        onDismiss = { expanded = false },
        options = options,
        isSelected = { it in selectedIndices },
        onOptionClick = onToggle,
    )
}

@Composable
private fun DropdownItemFrame(
    label: String,
    value: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    options: List<String>,
    isSelected: (Int) -> Boolean,
    onOptionClick: (Int) -> Unit,
) {
    val density = LocalDensity.current
    var valueBox by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var valueBoxWidth by remember { mutableStateOf(0.dp) }
    val contentColor = LocalBokuhonContentColor.current
    val colorScheme = LocalBokuhonColors.current
    val measurer = rememberTextMeasurer()
    val bodyMedium = LocalBokuhonTypography.current.bodyMedium
    // The box is as wide as its widest choice, so it holds its size as the value changes.
    // Measured rather than picked by length: a short label of wide glyphs can need more room
    // than a long one of narrow glyphs, and these labels are translated.
    val widestOption = remember(options, bodyMedium) {
        options.maxByOrNull { measurer.measure(it, bodyMedium, maxLines = 1).size.width }.orEmpty()
    }

    Column {
        Row(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsItemsPaddings.Horizontal,
                    vertical = SettingsItemsPaddings.Vertical,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                // Some of these names are long; wrapping beats cutting them short, and rows with a
                // short name still take a single line.
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = LocalBokuhonTypography.current.bodyMedium,
            )
            // Only the chosen value is boxed: that is the control, the label is not.
            Box(
                modifier = Modifier
                    .onGloballyPositioned {
                        valueBox = it
                        valueBoxWidth = with(density) { it.size.width.toDp() }
                    }
                    .border(1.dp, colorScheme.onSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = LocalBokuhonTypography.current.bodyMedium,
                )
                // Invisible, but it keeps the box the width of the longest choice instead of
                // resizing with whichever value happens to be picked.
                Text(
                    text = widestOption,
                    modifier = Modifier
                        .alpha(0f)
                        .clearAndSetSemantics {},
                    maxLines = 1,
                    style = bodyMedium,
                )
            }
        }

        if (expanded) {
            Popup(
                onDismissRequest = onDismiss,
                properties = PopupProperties(focusable = true),
                popupPositionProvider = remember {
                    object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize,
                        ): IntOffset {
                            // Placed from the value box's position in the window rather than from
                            // the bounds Compose hands a popup: inside the reader's sheet those
                            // arrive at the window's corner, so the list opened nowhere near its
                            // control. It hangs straight under the box, sharing its edges.
                            val bounds = valueBox?.boundsInWindow()
                            val top = bounds?.top?.roundToInt() ?: anchorBounds.top
                            val bottom = bounds?.bottom?.roundToInt() ?: anchorBounds.bottom
                            val left = bounds?.left?.roundToInt() ?: anchorBounds.left

                            val y = if (bottom + popupContentSize.height <= windowSize.height) {
                                bottom
                            } else {
                                (top - popupContentSize.height).coerceAtLeast(0)
                            }
                            val x = left.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
                            return IntOffset(x, y)
                        }
                    }
                },
            ) {
                Surface(
                    color = LocalBokuhonColors.current.surface,
                    border = BorderStroke(1.dp, LocalBokuhonColors.current.onSurface),
                ) {
                    // The same width as the box it drops from, so the two read as one control;
                    // the popup's window would otherwise stretch it across the screen.
                    Column(
                        modifier = Modifier
                            .width(valueBoxWidth)
                            .padding(2.dp),
                    ) {
                        options.forEachIndexed { index, option ->
                            val selected = isSelected(index)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = DropdownOptionHeight)
                                    // The chosen one is outlined rather than filled: a block of ink
                                    // in the list was heavier than the row it belongs to.
                                    .then(
                                        if (selected) {
                                            Modifier.border(1.dp, colorScheme.onSurface)
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .clickable { onOptionClick(index) }
                                    // Narrow enough that the longest choice still fits the box's
                                    // width, which is what keeps the two edges aligned.
                                    .padding(horizontal = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    color = contentColor,
                                    style = LocalBokuhonTypography.current.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectItem(
    label: String,
    options: Array<out Any?>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsItemsPaddings.Horizontal,
                    vertical = SettingsItemsPaddings.Vertical,
                ),
            label = { Text(text = label) },
            value = options.getOrNull(selectedIndex)?.toString().orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text.toString()) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
fun TriStateItem(
    label: String,
    state: TriState,
    enabled: Boolean = true,
    onClick: ((TriState) -> Unit)?,
) {
    Row(
        modifier = Modifier
            .clickable(
                enabled = enabled && onClick != null,
                onClick = {
                    when (state) {
                        TriState.DISABLED -> onClick?.invoke(TriState.ENABLED_IS)
                        TriState.ENABLED_IS -> onClick?.invoke(TriState.ENABLED_NOT)
                        TriState.ENABLED_NOT -> onClick?.invoke(TriState.DISABLED)
                    }
                },
            )
            .fillMaxWidth()
            .padding(
                horizontal = SettingsItemsPaddings.Horizontal,
                vertical = SettingsItemsPaddings.Vertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BokuhonPadding.large),
    ) {
        val stateAlpha = if (enabled && onClick != null) 1f else DISABLED_ALPHA

        Icon(
            imageVector = when (state) {
                TriState.DISABLED -> MaterialSymbols.Rounded.CheckBoxOutlineBlank
                TriState.ENABLED_IS -> MaterialSymbols.RoundedFilled.CheckBox
                TriState.ENABLED_NOT -> MaterialSymbols.RoundedFilled.DisabledByDefault
            },
            contentDescription = null,
            tint = if (!enabled || state == TriState.DISABLED) {
                LocalBokuhonColors.current.onSurfaceVariant.copy(alpha = stateAlpha)
            } else {
                when (onClick) {
                    null -> LocalBokuhonColors.current.onSurface.copy(alpha = DISABLED_ALPHA)
                    else -> LocalBokuhonColors.current.primary
                }
            },
        )
        Text(
            text = label,
            color = LocalBokuhonColors.current.onSurface.copy(alpha = stateAlpha),
            style = LocalBokuhonTypography.current.bodyMedium,
        )
    }
}

@Composable
fun TextItem(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsItemsPaddings.Horizontal, vertical = 4.dp),
        label = { Text(text = label) },
        value = value,
        onValueChange = onChange,
        singleLine = true,
    )
}

@Composable
fun SettingsChipRow(labelRes: StringResource, content: @Composable FlowRowScope.() -> Unit) {
    Column {
        HeadingItem(labelRes)
        FlowRow(
            modifier = Modifier.padding(
                start = SettingsItemsPaddings.Horizontal,
                top = 0.dp,
                end = SettingsItemsPaddings.Horizontal,
                bottom = SettingsItemsPaddings.Vertical,
            ),
            horizontalArrangement = Arrangement.spacedBy(BokuhonPadding.small),
            content = content,
        )
    }
}

@Composable
fun SettingsIconGrid(labelRes: StringResource, content: LazyGridScope.() -> Unit) {
    Column {
        HeadingItem(labelRes)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            modifier = Modifier.padding(
                start = SettingsItemsPaddings.Horizontal,
                end = SettingsItemsPaddings.Horizontal,
                bottom = SettingsItemsPaddings.Vertical,
            ),
            verticalArrangement = Arrangement.spacedBy(BokuhonPadding.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(BokuhonPadding.small),
            content = content,
        )
    }
}

@Composable
private fun BaseSettingsItem(
    label: String,
    widget: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(
                horizontal = SettingsItemsPaddings.Horizontal,
                vertical = SettingsItemsPaddings.Vertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        widget(this)
        Text(
            text = label,
            style = LocalBokuhonTypography.current.bodyMedium,
        )
    }
}
