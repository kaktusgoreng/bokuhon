package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTextStyle
import tachiyomi.presentation.core.bokuhon.Surface
import tachiyomi.presentation.core.bokuhon.Text

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalBokuhonColors.current.surfaceContainerHigh,
    contentColor: Color = LocalBokuhonColors.current.onSurface,
    style: TextStyle = LocalBokuhonTextStyle.current,
) {
    Surface(
        modifier = modifier
            .padding(start = 4.dp),
        shape = RectangleShape,
        color = color,
        contentColor = contentColor,
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp, 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                maxLines = 1,
                style = style,
            )
        }
    }
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalBokuhonColors.current.surfaceContainerHigh,
    contentColor: Color = LocalBokuhonColors.current.onSurface,
    fontSize: TextUnit = LocalBokuhonTextStyle.current.fontSize,
) {
    Pill(
        text = text,
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        style = LocalBokuhonTextStyle.current.merge(fontSize = fontSize),
    )
}
