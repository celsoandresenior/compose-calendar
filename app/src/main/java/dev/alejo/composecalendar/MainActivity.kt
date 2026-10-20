package dev.alejo.composecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.alejo.compose_calendar.ComposeCalendar
import dev.alejo.compose_calendar.SimpleComposeCalendar
import dev.alejo.compose_calendar.util.CalendarDefaults
import dev.alejo.composecalendar.ui.theme.ComposeCalendarTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            ComposeCalendarTheme {
                Scaffold { innerPadding ->
                    Calendar(Modifier.padding(innerPadding), state)
                }
            }
        }
    }
}

@Composable
fun Calendar(modifier: Modifier = Modifier, state: CalendarState) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleComposeCalendar(
            modifier = Modifier.padding(16.dp),
            events = state.simpleEvents,
            onDayClick = { _, _ -> },
            eventIndicator = { _, _, _ ->
                IndicatorContent(Modifier.size(8.dp))
            }
        )
        ComposeCalendar(
            events = state.events,
            onDayClick = { _, _ -> },
            eventIndicator = { _, position, events ->
                if (position < 2) {
                    IndicatorContent(Modifier.fillMaxWidth().height(8.dp))
                }
                if (position == 2) {
                    Text(
                        text = "+${events - 2}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            },
            maxIndicators = CalendarDefaults.IndicatorLimit.Three,
            indicatorLayout = CalendarDefaults.IndicatorLayout.Column,
            isContentClickable = false,
        )
    }
}

data class MyData(val name: String)

@Composable
fun IndicatorContent(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Box(modifier.clip(CircleShape).background(color))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ComposeCalendarPreview() {
    val events = sampleEvents()
    Calendar(state = CalendarState(events = events, simpleEvents = events.map { it.date }))
}
