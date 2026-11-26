package tachiyomi.presentation.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
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
 *
 * The menu is placed from the row's position in the window rather than from the bounds Compose hands
 * a popup: inside the reader's sheet those arrive at the window's corner, so the menu opened nowhere
 * near its row.
 */
@Composable
fun DropdownItem(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val contentColor = LocalBokuhonContentColor.current

    Column {
        Row(
            modifier = Modifier
                .onGloballyPositioned { anchor = it }
                .clickable { expanded = !expanded }
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = LocalBokuhonTypography.current.bodyMedium,
            )
            Text(
                text = options.getOrNull(selectedIndex).orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = LocalBokuhonTypography.current.bodyMedium,
            )
            Icon(
                imageVector = if (expanded) {
                    MaterialSymbols.Rounded.KeyboardArrowUp
                } else {
                    MaterialSymbols.Rounded.KeyboardArrowDown
                },
                contentDescription = null,
                tint = contentColor,
            )
        }

        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
                popupPositionProvider = remember {
                    object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize,
                        ): IntOffset {
                            val bounds = anchor?.boundsInWindow()
                            val top = bounds?.top?.roundToInt() ?: anchorBounds.top
                            val bottom = bounds?.bottom?.roundToInt() ?: anchorBounds.bottom
                            val left = bounds?.left?.roundToInt() ?: anchorBounds.left

                            val below = bottom
                            val fitsBelow = below + popupContentSize.height <= windowSize.height
                            val y = if (fitsBelow) {
                                below
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
                    // Sized by its widest choice: the popup's window would otherwise stretch it
                    // across the screen.
                    Column(modifier = Modifier.width(IntrinsicSize.Max).widthIn(min = 220.dp)) {
                        options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        onSelect(index)
                                        expanded = false
                                    }
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = SettingsItemsPaddings.Horizontal,
                                        vertical = SettingsItemsPaddings.Vertical * 2,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.weight(1f),
                                    style = LocalBokuhonTypography.current.bodyMedium,
                                )
                                if (index == selectedIndex) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Check,
                                        contentDescription = null,
                                        tint = contentColor,
                                    )
                                }
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
