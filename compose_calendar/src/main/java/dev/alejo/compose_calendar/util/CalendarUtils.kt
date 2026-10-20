package dev.alejo.compose_calendar.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Builds only the visible month, including empty cells before its first day. */
internal fun buildMonthDays(month: YearMonth, firstDayOfWeek: DayOfWeek): List<LocalDate?> {
    val leadingEmpty = (month.atDay(1).dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    return List(leadingEmpty) { null } + List(month.lengthOfMonth()) { month.atDay(it + 1) }
}
