package mihon.feature.upcoming.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toJavaYearMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.KeyboardArrowLeft
import mihon.icons.materialsymbols.rounded.KeyboardArrowRight
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.Icon
import tachiyomi.presentation.core.bokuhon.IconButton
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.components.motion.AnimatedContent
import tachiyomi.presentation.core.i18n.stringResource
import java.time.format.DateTimeFormatter
import kotlin.time.Clock

@Composable
fun CalenderHeader(
    yearMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = getTitleText(yearMonth),
            style = MaterialTheme.typography.titleLarge,
        )
        Row {
            IconButton(onClick = onPreviousClick) {
                @Suppress("DEPRECATION")
                Icon(MaterialSymbols.Rounded.KeyboardArrowLeft, stringResource(MR.strings.upcoming_calendar_prev))
            }
            IconButton(onClick = onNextClick) {
                @Suppress("DEPRECATION")
                Icon(MaterialSymbols.Rounded.KeyboardArrowRight, stringResource(MR.strings.upcoming_calendar_next))
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun getTitleText(monthYear: YearMonth): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", LocalLocale.current.platformLocale)
    return formatter.format(monthYear.toJavaYearMonth())
}

@Preview
@Composable
private fun CalenderHeaderPreview() {
    CalenderHeader(
        yearMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.yearMonth,
        onNextClick = {},
        onPreviousClick = {},
    )
}
