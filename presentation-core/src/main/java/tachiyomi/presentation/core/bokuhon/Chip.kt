package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA

object FilterChipDefaults {
    val Height = 32.dp
    val IconSize = 18.dp
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Any? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: BorderStroke? = null,
    interactionSource: Any? = null,
) {
    val c = LocalBokuhonColors.current
    // Material keeps a 48dp touch target around the 32dp chip; do the same.
    Box(
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        ChipSurface(
            onClick = onClick,
            enabled = enabled,
            container = if (selected) c.surfaceVariant else Color.Transparent,
            contentColor = c.onSurface,
            border = border ?: if (selected) null else BorderStroke(1.dp, c.outline),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            label = label,
        )
    }
}

@Composable
fun SuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    shape: Any? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: BorderStroke? = null,
    interactionSource: Any? = null,
) {
    val c = LocalBokuhonColors.current
    ChipSurface(
        onClick = onClick,
        enabled = enabled,
        container = Color.Transparent,
        contentColor = c.onSurface,
        border = border ?: BorderStroke(1.dp, c.outline),
        leadingIcon = icon,
        trailingIcon = null,
        label = label,
    )
}

@Composable
private fun ChipSurface(
    onClick: () -> Unit,
    enabled: Boolean,
    container: Color,
    contentColor: Color,
    border: BorderStroke?,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    label: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(FilterChipDefaults.Height)
            .then(if (border != null) Modifier.border(border) else Modifier)
            .background(container)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor.copy(alpha = if (enabled) 1f else DISABLED_ALPHA),
            LocalBokuhonTextStyle provides LocalBokuhonTypography.current.labelLarge,
        ) {
            leadingIcon?.invoke()
            label()
            trailingIcon?.invoke()
        }
    }
}
