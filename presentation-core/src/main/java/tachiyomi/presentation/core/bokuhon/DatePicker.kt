package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.TextStyle

interface SelectableDates {
    fun isSelectableDate(utcTimeMillis: Long): Boolean = true

    fun isSelectableYear(year: Int): Boolean = true
}

@Stable
class DatePickerState(
    initialSelectedDateMillis: Long? = null,
    val yearRange: IntRange = 1900..2100,
    val selectableDates: SelectableDates = object : SelectableDates {},
) {
    var selectedDateMillis: Long? by mutableStateOf(initialSelectedDateMillis)
        private set

    var displayedMonth: YearMonth by mutableStateOf(
        initialSelectedDateMillis
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
            ?.let { YearMonth.from(it) }
            ?: YearMonth.now(ZoneOffset.UTC),
    )
        private set

    fun selectDate(millis: Long) {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
        if (!selectableDates.isSelectableYear(date.year)) return
        if (!selectableDates.isSelectableDate(millis)) return
        selectedDateMillis = millis
    }

    fun showPreviousMonth() {
        val previous = displayedMonth.minusMonths(1)
        if (previous.year >= yearRange.first) displayedMonth = previous
    }

    fun showNextMonth() {
        val next = displayedMonth.plusMonths(1)
        if (next.year <= yearRange.last) displayedMonth = next
    }
}

@Composable
fun rememberDatePickerState(
    initialSelectedDateMillis: Long? = null,
    yearRange: IntRange = 1900..2100,
    selectableDates: SelectableDates = object : SelectableDates {},
): DatePickerState = remember {
    DatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        yearRange = yearRange,
        selectableDates = selectableDates,
    )
}

@Composable
fun DatePicker(
    state: DatePickerState,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    headline: (@Composable () -> Unit)? = null,
    showModeToggle: Boolean = true,
) {
    val month = state.displayedMonth
    val locale = LocalConfiguration.current.locales[0]
    val today = LocalDate.now(ZoneOffset.UTC)
    val selected = state.selectedDateMillis
        ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }

    Column(modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        title?.invoke()
        headline?.invoke()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonthNavButton(label = "‹", onClick = state::showPreviousMonth)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = month.atDay(1).month.getDisplayName(TextStyle.FULL, locale) +
                        " " + month.year,
                    style = LocalBokuhonTypography.current.titleSmall,
                )
            }
            MonthNavButton(label = "›", onClick = state::showNextMonth)
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        color = LocalBokuhonColors.current.onSurfaceVariant,
                        style = LocalBokuhonTypography.current.labelSmall,
                    )
                }
            }
        }

        val firstOfMonth = month.atDay(1)
        val leadingBlanks = (firstOfMonth.dayOfWeek.value + 6) % 7
        val daysInMonth = month.lengthOfMonth()
        val cells = leadingBlanks + daysInMonth
        val rows = (cells + 6) / 7

        for (row in 0..<rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (column in 0..6) {
                    val day = row * 7 + column - leadingBlanks + 1
                    if (day in 1..daysInMonth) {
                        val date = month.atDay(day)
                        val millis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                        val selectable = state.selectableDates.isSelectableYear(date.year) &&
                            state.selectableDates.isSelectableDate(millis)
                        DayCell(
                            modifier = Modifier.weight(1f),
                            date = date,
                            isSelected = selected == date,
                            isToday = today == date,
                            selectable = selectable,
                            onClick = { state.selectDate(millis) },
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).size(width = 0.dp, height = 40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier,
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    selectable: Boolean,
    onClick: () -> Unit,
) {
    val c = LocalBokuhonColors.current
    val background = when {
        isSelected -> c.primary
        else -> Color.Transparent
    }
    val contentColor = when {
        isSelected -> c.onPrimary
        isToday -> c.primary
        else -> c.onSurface
    }
    Box(
        modifier = modifier
            .height(40.dp)
            .clickable(enabled = selectable, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(
                LocalBokuhonContentColor provides if (selectable) contentColor else contentColor.copy(alpha = 0.38f),
                content = {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = LocalBokuhonTypography.current.bodyMedium,
                    )
                },
            )
        }
    }
}

@Composable
private fun MonthNavButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = LocalBokuhonTypography.current.titleLarge)
    }
}
