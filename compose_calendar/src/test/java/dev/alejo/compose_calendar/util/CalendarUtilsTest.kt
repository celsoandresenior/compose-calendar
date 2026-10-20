package dev.alejo.compose_calendar.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarUtilsTest {
    @Test
    fun alignsEveryMonthWithEachFirstDayOfWeek() {
        for (year in listOf(1900, 2000, 2024, 2026, 2100)) {
            for (monthNumber in 1..12) {
                val month = YearMonth.of(year, monthNumber)
                for (firstDay in DayOfWeek.entries) {
                    val days = buildMonthDays(month, firstDay)
                    val leading = days.takeWhile { it == null }.size
                    assertEquals((month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7, leading)
                    assertEquals((1..month.lengthOfMonth()).map { month.atDay(it) }, days.filterNotNull())
                    assertTrue(days.size <= 37)
                }
            }
        }
    }

    @Test
    fun compactGridIncludesOnlyLeadingDatesFromPreviousMonth() {
        val days = buildMonthDays(YearMonth.of(2023, 2), DayOfWeek.SUNDAY, showPreviousMonthDays = true)
        assertEquals(listOf(LocalDate.of(2023, 1, 29), LocalDate.of(2023, 1, 30),
            LocalDate.of(2023, 1, 31), LocalDate.of(2023, 2, 1)), days.take(4))
        assertEquals(LocalDate.of(2023, 2, 28), days.last())
    }

    @Test
    fun previousMonthPaddingDoesNotOverflowAtMinimumDate() {
        val days = buildMonthDays(YearMonth.from(LocalDate.MIN), DayOfWeek.SUNDAY, true)
        assertEquals(LocalDate.MIN, days.filterNotNull().first())
        assertEquals(31, days.filterNotNull().size)
    }

    @Test
    fun handlesLeapYearsAndCenturyExceptions() {
        assertEquals(29, buildMonthDays(YearMonth.of(2000, 2), DayOfWeek.MONDAY).filterNotNull().size)
        assertEquals(28, buildMonthDays(YearMonth.of(1900, 2), DayOfWeek.MONDAY).filterNotNull().size)
    }

    @Test
    fun supportsHistoricalFutureAndExtremeMonths() {
        for (month in listOf(YearMonth.of(1800, 1), YearMonth.of(2200, 12),
            YearMonth.from(LocalDate.MIN), YearMonth.from(LocalDate.MAX))) {
            val dates = buildMonthDays(month, DayOfWeek.SUNDAY).filterNotNull()
            assertEquals(month.atDay(1), dates.first())
            assertEquals(month.atEndOfMonth(), dates.last())
        }
    }
}
