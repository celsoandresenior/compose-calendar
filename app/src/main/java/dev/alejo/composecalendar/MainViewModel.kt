package dev.alejo.composecalendar

import androidx.lifecycle.ViewModel
import dev.alejo.compose_calendar.CalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state = _state.asStateFlow()

    init {
        val events = sampleEvents()
        _state.value = CalendarState(events = events, simpleEvents = events.map { it.date })
    }
}

internal fun sampleEvents(today: LocalDate = LocalDate.now()): List<CalendarEvent<MyData>> = listOf(
    0L to "Event 1",
    -20L to "Event 1",
    -20L to "Event 2",
    1L to "Event 1",
    1L to "Event 2",
    1L to "Event 3",
    1L to "Event 4",
    4L to "Event 3",
    4L to "Event 4",
    4L to "Event 4"
).map { (offset, name) -> CalendarEvent(data = MyData(name), date = today.plusDays(offset)) }
