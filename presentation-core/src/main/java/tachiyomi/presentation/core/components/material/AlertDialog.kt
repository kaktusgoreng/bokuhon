package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonContentColor
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTextStyle
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography

@Composable
fun AlertDialogContent(
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: @Composable () -> Unit,
) {
    AlertDialogContent(
        modifier = modifier,
        icon = icon,
        title = title,
        content = {
            Column {
                CompositionLocalProvider(
                    LocalBokuhonContentColor provides LocalBokuhonColors.current.onSurfaceVariant,
                ) {
                    val textStyle = LocalBokuhonTypography.current.bodyMedium
                    CompositionLocalProvider(LocalBokuhonTextStyle provides textStyle) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(horizontal = DialogPadding)
                                .padding(TextPadding)
                                .align(Alignment.Start),
                        ) {
                            text()
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(
                            start = DialogPadding,
                            end = DialogPadding,
                            bottom = DialogPadding,
                        )
                        .align(Alignment.End),
                ) {
                    CompositionLocalProvider(
                        LocalBokuhonContentColor provides LocalBokuhonColors.current.primary,
                    ) {
                        val textStyle = LocalBokuhonTypography.current.labelLarge
                        CompositionLocalProvider(LocalBokuhonTextStyle provides textStyle, content = buttons)
                    }
                }
            }
        },
    )
}

@Composable
fun AlertDialogContent(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .sizeIn(minWidth = MinWidth, maxWidth = MaxWidth),
    ) {
        if (icon != null || title != null) {
            Column(
                modifier = Modifier
                    .padding(
                        start = DialogPadding,
                        top = DialogPadding,
                        end = DialogPadding,
                    )
                    .fillMaxWidth(),
            ) {
                icon?.let {
                    CompositionLocalProvider(
                        LocalBokuhonContentColor provides LocalBokuhonColors.current.secondary,
                    ) {
                        Box(
                            Modifier
                                .padding(IconPadding)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            icon()
                        }
                    }
                }
                title?.let {
                    CompositionLocalProvider(
                        LocalBokuhonContentColor provides LocalBokuhonColors.current.onSurface,
                    ) {
                        val textStyle = LocalBokuhonTypography.current.headlineSmall
                        CompositionLocalProvider(LocalBokuhonTextStyle provides textStyle) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier
                                    .padding(TitlePadding)
                                    .align(
                                        if (icon == null) {
                                            Alignment.Start
                                        } else {
                                            Alignment.CenterHorizontally
                                        },
                                    ),
                            ) {
                                title()
                            }
                        }
                    }
                }
            }
        }
        content?.invoke(this)
    }
}

// Paddings for each of the dialog's parts.
private val DialogPadding = 24.dp
private val IconPadding = PaddingValues(bottom = 16.dp)
private val TitlePadding = PaddingValues(bottom = 16.dp)
private val TextPadding = PaddingValues(bottom = 24.dp)

private val MinWidth = 280.dp
private val MaxWidth = 560.dp
