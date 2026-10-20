package dev.alejo.composecalendar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SampleEventsTest {
    @Test
    fun sampleEventsUseTheSuppliedDateConsistently() {
        val today = LocalDate.of(2026, 1, 1)
        val dates = sampleEvents(today).groupingBy { it.date }.eachCount()
        assertEquals(mapOf(today to 1, today.minusDays(20) to 2,
            today.plusDays(1) to 4, today.plusDays(4) to 3), dates)
    }
}
