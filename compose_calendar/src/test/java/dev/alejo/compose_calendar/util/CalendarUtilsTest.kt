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
