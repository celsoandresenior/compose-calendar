package dev.alejo.compose_calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.alejo.compose_calendar.component.CalendarBody
import dev.alejo.compose_calendar.component.CalendarHeader
import dev.alejo.compose_calendar.component.CompactCalendarContent
import dev.alejo.compose_calendar.util.CalendarColors
import dev.alejo.compose_calendar.util.CalendarDefaults
import dev.alejo.compose_calendar.util.buildMonthDays
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

internal val CalendarDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = { LocalDate.parse(it) }
)

/**
 * Main composable function to render a fully featured calendar with event indicators.
 *
 * Displays a monthly calendar view starting from [initDate], showing days, events, and
 * navigation controls for switching between months. Supports customization of event indicators,
 * click interactions on days, and styling via [calendarColors].
 *
 * @param T The generic type of events associated with calendar dates.
 * @param modifier Modifier to be applied to the calendar container.
 * @param initDate The initial date from which the calendar will display the month. Defaults to the first day of the current month.
 * @param events List of [CalendarEvent] objects representing events to be shown on the calendar.
 * @param onDayClick Lambda invoked when a calendar day is clicked, providing the date and associated events.
 * @param calendarColors Object specifying colors used throughout the calendar UI.
 * @param firstDayOfWeek Specifies the day to be treated as the first day of the week (e.g., Monday, Sunday).
 * @param eventIndicator Optional composable lambda to customize the rendering of individual event indicators.
 * @param maxIndicators The maximum number of event indicators to display per day cell.
 * @param indicatorLayout The layout style used to arrange event indicators within each day cell.
 * @param isContentClickable Flag that enables or disables clicking on individual day cells.
 * @param onPreviousMonthClick Lambda invoked when the user navigates to the previous month.
 * @param onNextMonthClick Lambda invoked when the user navigates to the next month.
 * @param layout Compact month picker or the original Classic event-cell layout.
 * @param initialSelectedDate Initially highlighted date; subsequent clicks update and save the selection.
 * @param locale Locale used by the compact month and weekday labels.
 */

@Composable
fun <T> ComposeCalendar(
    modifier: Modifier = Modifier,
    initDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    events: List<CalendarEvent<T>> = emptyList(),
    onDayClick: (date: LocalDate, events: List<CalendarEvent<T>>) -> Unit = { _, _ -> },
    calendarColors: CalendarColors = CalendarDefaults.calendarColors(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
    eventIndicator: (@Composable (event: CalendarEvent<T>, position: Int, count: Int) -> Unit)? = null,
    maxIndicators: CalendarDefaults.IndicatorLimit = CalendarDefaults.IndicatorLimit.Four,
    indicatorLayout: CalendarDefaults.IndicatorLayout = CalendarDefaults.IndicatorLayout.Row,
    isContentClickable: Boolean = true,
    onPreviousMonthClick: (LocalDate) -> Unit = {},
    onNextMonthClick: (LocalDate) -> Unit = {},
    layout: CalendarDefaults.Layout = CalendarDefaults.Layout.Compact,
    initialSelectedDate: LocalDate? = LocalDate.now(),
    locale: Locale = Locale.getDefault()
) {
    var currentMonth by rememberSaveable(stateSaver = CalendarDateSaver) {
        mutableStateOf(initDate)
    }
    val currentYearMonth = YearMonth.from(currentMonth)
    val isPreviousButtonEnable = currentYearMonth > YearMonth.from(LocalDate.MIN)
    val isNextButtonEnable = currentYearMonth < YearMonth.from(LocalDate.MAX)
    var selectedEpochDay by rememberSaveable { mutableStateOf(initialSelectedDate?.toEpochDay()) }
    val selectDay: (LocalDate, List<CalendarEvent<T>>) -> Unit = { date, dayEvents ->
        selectedEpochDay = date.toEpochDay()
        onDayClick(date, dayEvents)
    }
    val days = remember(currentYearMonth, firstDayOfWeek, layout) {
        buildMonthDays(currentYearMonth, firstDayOfWeek, layout == CalendarDefaults.Layout.Compact)
    }
    val currentMonthEvents by remember(events, currentYearMonth) {
        derivedStateOf {
            events.filter { YearMonth.from(it.date) == currentYearMonth }.groupBy { it.date }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = calendarColors.backgroundColor,
                shape = RoundedCornerShape(CalendarDefaults.Dimens.Default)
            )
            .padding(
                start = CalendarDefaults.Dimens.Default,
                top = CalendarDefaults.Dimens.Default,
                end = CalendarDefaults.Dimens.Default,
                bottom = if (layout == CalendarDefaults.Layout.Compact) 4.dp else CalendarDefaults.Dimens.Default
            ),
        verticalArrangement = Arrangement.spacedBy(
            if (layout == CalendarDefaults.Layout.Compact) CalendarDefaults.Dimens.None else CalendarDefaults.Dimens.Default
        )
    ) {
        if (layout == CalendarDefaults.Layout.Compact) {
            CompactCalendarContent(
                month = currentYearMonth,
                days = days,
                selectedDate = selectedEpochDay?.let(LocalDate::ofEpochDay),
                events = currentMonthEvents,
                firstDayOfWeek = firstDayOfWeek,
                locale = locale,
                colors = calendarColors,
                isContentClickable = isContentClickable,
                previousEnabled = isPreviousButtonEnable,
                nextEnabled = isNextButtonEnable,
                onPreviousMonthClick = {
                    currentMonth = currentMonth.minusMonths(1)
                    onPreviousMonthClick(currentMonth)
                },
                onNextMonthClick = {
                    currentMonth = currentMonth.plusMonths(1)
                    onNextMonthClick(currentMonth)
                },
                onDayClick = selectDay,
                eventIndicator = eventIndicator,
                maxIndicators = maxIndicators,
                indicatorLayout = indicatorLayout
            )
        } else {
            CalendarHeader(
                currentMonth = currentMonth,
                calendarColors = calendarColors,
                firstDayOfWeek = firstDayOfWeek,
                isPreviousButtonEnable = isPreviousButtonEnable,
                isNextButtonEnable = isNextButtonEnable,
                onPreviousMonthClick = {
                    if (isPreviousButtonEnable) {
                        currentMonth = currentMonth.minusMonths(1)
                        onPreviousMonthClick(currentMonth)
                    }
                },
                onNextMonthClick = {
                    if (isNextButtonEnable) {
                        currentMonth = currentMonth.plusMonths(1)
                        onNextMonthClick(currentMonth)
                    }
                },
            )
            CalendarBody(
                events = currentMonthEvents,
                days = days,
                onDayClick = selectDay,
                eventIndicator = eventIndicator,
                maxIndicators = maxIndicators,
                indicatorLayout = indicatorLayout,
                calendarColors = calendarColors,
                isContentClickable = isContentClickable
            )
        }
    }
}

/**
 * Simplified composable calendar function for displaying dates with minimal event data.
 *
 * This wrapper around [ComposeCalendar] accepts a list of [LocalDate] events without additional
 * event details. It converts these dates into generic calendar events internally, allowing easy
 * usage when only event dates matter.
 *
 * @param modifier Modifier to be applied to the calendar container.
 * @param initDate The initial date from which the calendar will display the month. Defaults to the first day of the current month.
 * @param events List of dates representing event days to be shown on the calendar.
 * @param onDayClick Lambda invoked when a calendar day is clicked, providing the date and a list of event dates on that day.
 * @param calendarColors Object specifying colors used throughout the calendar UI.
 * @param firstDayOfWeek Specifies the day to be treated as the first day of the week (e.g., Monday, Sunday).
 * @param eventIndicator Optional composable lambda to customize the rendering of individual event indicators by date.
 * @param maxIndicators The maximum number of event indicators to display per day cell.
 * @param indicatorLayout The layout style used to arrange event indicators within each day cell.
 * @param isContentClickable Flag that enables or disables clicking on individual day cells.
 * @param onPreviousMonthClick Lambda invoked when the user navigates to the previous month.
 * @param onNextMonthClick Lambda invoked when the user navigates to the next month.
 * @param layout Compact month picker or the original Classic event-cell layout.
 * @param initialSelectedDate Initially highlighted date; subsequent clicks update and save the selection.
 * @param locale Locale used by the compact month and weekday labels.
 */

@Composable
fun SimpleComposeCalendar(
    modifier: Modifier = Modifier,
    initDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    events: List<LocalDate> = emptyList(),
    onDayClick: (date: LocalDate, events: List<LocalDate>) -> Unit = { _, _ -> },
    calendarColors: CalendarColors = CalendarDefaults.calendarColors(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
    eventIndicator: (@Composable (event: LocalDate, position: Int, count: Int) -> Unit)? = null,
    maxIndicators: CalendarDefaults.IndicatorLimit = CalendarDefaults.IndicatorLimit.Four,
    indicatorLayout: CalendarDefaults.IndicatorLayout = CalendarDefaults.IndicatorLayout.Row,
    isContentClickable: Boolean = true,
    onPreviousMonthClick: (LocalDate) -> Unit = {},
    onNextMonthClick: (LocalDate) -> Unit = {},
    layout: CalendarDefaults.Layout = CalendarDefaults.Layout.Compact,
    initialSelectedDate: LocalDate? = LocalDate.now(),
    locale: Locale = Locale.getDefault()
) {
    val calendarEvents by remember(events) {
        derivedStateOf { events.map { CalendarEvent<Unit>(date = it) } }
    }
    ComposeCalendar<Unit>(
        modifier = modifier,
        initDate = initDate,
        events = calendarEvents,
        onDayClick = { date, dayEvents -> onDayClick(date, dayEvents.map { it.date }) },
        calendarColors = calendarColors,
        firstDayOfWeek = firstDayOfWeek,
        eventIndicator = eventIndicator?.let {
            ({ event, indicatorPosition, eventsSize -> eventIndicator(event.date, indicatorPosition, eventsSize) })
        },
        indicatorLayout = indicatorLayout,
        maxIndicators = maxIndicators,
        isContentClickable = isContentClickable,
        onPreviousMonthClick = onPreviousMonthClick,
        onNextMonthClick = onNextMonthClick,
        layout = layout,
        initialSelectedDate = initialSelectedDate,
        locale = locale
    )
}
