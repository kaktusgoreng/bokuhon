package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@NonRestartableComposable
fun Surface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = LocalBokuhonColors.current.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) {
        Box(
            modifier = modifier
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .background(color = color, shape = shape)
                .clip(shape),
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}

@Composable
@NonRestartableComposable
fun Surface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    color: Color = LocalBokuhonColors.current.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBokuhonContentColor provides contentColor) {
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .background(color = color, shape = shape)
                .clip(shape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onLongClick = onLongClick,
                    onClick = onClick,
                ),
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) = CardSurface(modifier, shape, colors, border) { content(this) }

@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    elevation: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit,
) = CardSurface(modifier, shape, colors, border = null) { content(this) }

@Composable
fun OutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    elevation: Dp = 0.dp,
    border: BorderStroke = BorderStroke(1.dp, LocalBokuhonColors.current.outline),
    content: @Composable ColumnScope.() -> Unit,
) = CardSurface(modifier, shape, colors, border) { content(this) }

@Composable
private fun CardSurface(
    modifier: Modifier,
    shape: Shape,
    colors: CardColors,
    border: BorderStroke?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        border = border,
    ) {
        Column(content = content)
    }
}

@Immutable
data class CardColors(
    val containerColor: Color,
    val contentColor: Color,
)

object CardDefaults {
    @Composable
    fun cardColors(
        containerColor: Color = LocalBokuhonColors.current.surfaceVariant,
        contentColor: Color = LocalBokuhonColors.current.onSurface,
    ) = CardColors(containerColor, contentColor)

    @Composable
    fun elevatedCardColors(
        containerColor: Color = LocalBokuhonColors.current.surface,
        contentColor: Color = LocalBokuhonColors.current.onSurface,
    ) = CardColors(containerColor, contentColor)

    @Composable
    fun outlinedCardColors(
        containerColor: Color = LocalBokuhonColors.current.surface,
        contentColor: Color = LocalBokuhonColors.current.onSurface,
    ) = CardColors(containerColor, contentColor)
}
