package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun SmallExtendedFloatingActionButton(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    containerColor: Color = LocalBokuhonColors.current.primary,
    contentColor: Color = LocalBokuhonColors.current.onPrimary,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .then(if (expanded) Modifier else Modifier.width(56.dp))
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) {
            icon()
            if (expanded) {
                CompositionLocalProvider(LocalBokuhonTextStyle provides LocalBokuhonTypography.current.labelLarge) {
                    text()
                }
            }
        }
    }
}

/**
 * Fades the button instead of animating it: e-ink panels smear scale and slide motion.
 */
fun Modifier.animateFloatingActionButton(
    visible: Boolean,
    alignment: Alignment = Alignment.BottomEnd,
): Modifier = this.alpha(if (visible) 1f else 0f)
