package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.components.DropdownMenu
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import tachiyomi.domain.category.model.Category
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.DropdownMenuItem
import tachiyomi.presentation.core.bokuhon.HorizontalDivider
import tachiyomi.presentation.core.bokuhon.Icon
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.i18n.stringResource

/**
 * The library's category picker: the active category in an outlined box that opens the full list,
 * with a way to add a new one at the bottom.
 */
@Composable
internal fun LibraryCategoryMenu(
    categories: List<Category>,
    selectedIndex: Int,
    getItemCountForCategory: (Category) -> Int?,
    onSelectCategory: (Int) -> Unit,
    onCreateCategory: () -> Unit,
) {
    val colors = LocalBokuhonColors.current
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .border(1.dp, colors.onSurface)
            .clickable { expanded = true }
            .padding(horizontal = BokuhonPadding.small, vertical = BokuhonPadding.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = categories.getOrNull(selectedIndex)?.visualName.orEmpty(),
            style = LocalBokuhonTypography.current.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 160.dp),
        )
        Icon(
            imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
            contentDescription = null,
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            val contentColor = if (isSelected) colors.surface else colors.onSurface
            DropdownMenuItem(
                modifier = if (isSelected) Modifier.background(colors.onSurface) else Modifier,
                onClick = {
                    expanded = false
                    onSelectCategory(index)
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = category.visualName,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        getItemCountForCategory(category)?.let { count ->
                            Text(
                                text = "$count",
                                color = contentColor,
                                modifier = Modifier.padding(start = BokuhonPadding.small),
                            )
                        }
                    }
                },
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = BokuhonPadding.extraSmall),
            color = colors.onSurface,
        )

        DropdownMenuItem(
            onClick = {
                expanded = false
                onCreateCategory()
            },
            text = {
                Text(
                    text = stringResource(MR.strings.action_add_category),
                    color = colors.onSurface,
                )
            },
        )
    }
}
