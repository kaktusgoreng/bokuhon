package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.components.motion.animateDpAsState

/**
 * Switch drawn square so it stays legible on e-ink panels.
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalBokuhonColors.current
    val trackColor = if (checked) colors.primary else colors.surface
    val borderColor = when {
        !enabled -> colors.onSurface.copy(alpha = 0.12f)
        checked -> colors.primary
        else -> colors.outline
    }
    val thumbColor = when {
        !enabled -> colors.onSurface.copy(alpha = 0.38f)
        checked -> colors.onPrimary
        else -> colors.outline
    }
    val thumbOffset by animateDpAsState(if (checked) 25.dp else 5.dp, label = "switchThumb")

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .background(trackColor)
            .border(2.dp, borderColor)
            .toggleable(
                value = checked,
                enabled = enabled && onCheckedChange != null,
                role = Role.Switch,
                onValueChange = { onCheckedChange?.invoke(it) },
            ),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 5.dp)
                .size(22.dp)
                .background(thumbColor),
        )
    }
}
