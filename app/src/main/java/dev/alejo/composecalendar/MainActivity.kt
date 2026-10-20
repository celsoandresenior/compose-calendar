package dev.alejo.composecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alejo.compose_calendar.ComposeCalendar
import dev.alejo.compose_calendar.util.CalendarDefaults
import dev.alejo.composecalendar.ui.theme.ComposeCalendarTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            ComposeCalendarTheme {
                Scaffold(containerColor = Color.White) { innerPadding ->
                    Calendar(Modifier.padding(innerPadding), state)
                }
            }
        }
    }
}

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    state: CalendarState,
    initialDate: LocalDate = LocalDate.now()
) {
    var selectedDate by rememberSaveable { mutableStateOf(initialDate.toString()) }
    var clickedEventsCount by rememberSaveable { mutableStateOf<Int?>(null) }
    val locale = Locale.forLanguageTag("pt-BR")
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ComposeCalendar(
            modifier = Modifier.widthIn(max = 320.dp),
            initDate = initialDate.withDayOfMonth(1),
            initialSelectedDate = initialDate,
            events = state.events,
            firstDayOfWeek = DayOfWeek.SUNDAY,
            locale = locale,
            calendarColors = CalendarDefaults.calendarColors(
                backgroundColor = Color.White,
                headerBackgroundColor = Color.White,
                contentColor = Color(0xFF111111),
                headerContentColor = Color.Black
            ),
            onDayClick = { date, events ->
                selectedDate = date.toString()
                clickedEventsCount = events.size
            }
        )
        if (clickedEventsCount != null) {
            Text(
                text = "${LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} · $clickedEventsCount evento(s)",
                color = Color(0xFF555555)
            )
        }
    }
}

data class MyData(val name: String)

@Preview(showBackground = true, widthDp = 301, heightDp = 300, locale = "pt-rBR")
@Composable
fun ComposeCalendarPreview() {
    val date = LocalDate.of(2023, 2, 20)
    ComposeCalendarTheme {
        Calendar(state = CalendarState(events = sampleEvents(date)), initialDate = date)
    }
}
