package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.bokuhon.Icon
import tachiyomi.presentation.core.bokuhon.IconButton
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors

@Composable
fun LinkIcon(
    label: String,
    icon: ImageVector,
    url: String,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    IconButton(
        modifier = modifier.padding(4.dp),
        onClick = { uriHandler.openUri(url) },
    ) {
        Icon(
            imageVector = icon,
            tint = LocalBokuhonColors.current.primary,
            contentDescription = label,
        )
    }
}
