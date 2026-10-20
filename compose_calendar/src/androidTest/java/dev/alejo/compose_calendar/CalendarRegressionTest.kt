package dev.alejo.compose_calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.alejo.compose_calendar.component.EventIcon
import dev.alejo.compose_calendar.util.CalendarDefaults
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class CalendarRegressionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun gridPassesDistinctPositionsForTwoEvents() {
        val events = listOf(CalendarEvent("first", LocalDate.of(2026, 10, 1)),
            CalendarEvent("second", LocalDate.of(2026, 10, 1)))
        compose.setContent {
            EventIcon(events, { event, position, count -> Text("${event.data}:$position/$count") },
                CalendarDefaults.IndicatorLimit.Four, CalendarDefaults.IndicatorLayout.Grid)
        }
        compose.onNodeWithText("first:0/2").assertExists()
        compose.onNodeWithText("second:1/2").assertExists()
    }

    @Test
    fun selectedMonthSurvivesStateRestoration() {
        val restoration = StateRestorationTester(compose)
        val navigatedMonths = mutableListOf<LocalDate>()
        restoration.setContent {
            MaterialTheme {
                SimpleComposeCalendar(initDate = LocalDate.of(2026, 1, 1),
                    onNextMonthClick = { navigatedMonths += it })
            }
        }
        compose.onNodeWithContentDescription(nextMonthLabel()).performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithContentDescription(nextMonthLabel()).performClick()
        compose.runOnIdle {
            assertEquals(listOf(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)), navigatedMonths)
        }
    }

    @Test
    fun historicalMonthRendersDaysAndEvents() {
        val date = LocalDate.of(1800, 1, 1)
        var clickedDate: LocalDate? = null
        var clickedEvents: List<LocalDate> = emptyList()
        compose.setContent {
            MaterialTheme {
                SimpleComposeCalendar(initDate = date, events = listOf(date),
                    onDayClick = { day, events -> clickedDate = day; clickedEvents = events })
            }
        }
        compose.onNodeWithText("1").performClick()
        compose.runOnIdle {
            assertEquals(date, clickedDate)
            assertEquals(listOf(date), clickedEvents)
        }
    }

    @Test
    fun addingEventsToSnapshotStateListUpdatesIndicators() {
        val date = LocalDate.of(2026, 10, 1)
        val events = mutableStateListOf<LocalDate>()
        compose.setContent {
            MaterialTheme {
                SimpleComposeCalendar(initDate = date, events = events,
                    eventIndicator = { _, position, count -> Text("event:$position/$count") })
            }
        }
        compose.onNodeWithText("event:0/1").assertDoesNotExist()
        compose.runOnIdle { events += date }
        compose.onNodeWithText("event:0/1").assertExists()
    }

    @Test
    fun rendersInsideVerticallyScrollingContainer() {
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    SimpleComposeCalendar(initDate = LocalDate.of(2026, 10, 1))
                }
            }
        }
        compose.onNodeWithText("31").assertExists()
    }

    @Test
    fun navigationStopsAtSupportedDateExtremes() {
        compose.setContent {
            MaterialTheme {
                Column {
                    SimpleComposeCalendar(initDate = LocalDate.MIN)
                    SimpleComposeCalendar(initDate = LocalDate.MAX)
                }
            }
        }
        compose.onAllNodesWithContentDescription(previousMonthLabel())[0].assertIsNotEnabled()
        compose.onAllNodesWithContentDescription(nextMonthLabel())[1].assertIsNotEnabled()
    }

    private fun previousMonthLabel() = if (Locale.getDefault().language == "pt") "Mês anterior" else "Previous month"
    private fun nextMonthLabel() = if (Locale.getDefault().language == "pt") "Próximo mês" else "Next month"
}
