package dev.alejo.compose_calendar.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Builds the visible month with leading blanks or muted previous-month dates. */
internal fun buildMonthDays(month: YearMonth, firstDayOfWeek: DayOfWeek, showPreviousMonthDays: Boolean = false): List<LocalDate?> {
    val leadingEmpty = (month.atDay(1).dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val first = month.atDay(1)
    val padding = List(leadingEmpty) { index ->
        val offset = (leadingEmpty - index).toLong()
        if (showPreviousMonthDays && first.toEpochDay() - offset >= LocalDate.MIN.toEpochDay()) {
            first.minusDays(offset)
        } else null
    }
    return padding + List(month.lengthOfMonth()) { month.atDay(it + 1) }
}
