package dev.alejo.compose_calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.alejo.compose_calendar.CalendarEvent
import dev.alejo.compose_calendar.R
import dev.alejo.compose_calendar.util.CalendarColors
import dev.alejo.compose_calendar.util.CalendarDefaults
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun <T> CompactCalendarContent(
    month: YearMonth,
    days: List<LocalDate?>,
    selectedDate: LocalDate?,
    events: Map<LocalDate, List<CalendarEvent<T>>>,
    firstDayOfWeek: DayOfWeek,
    locale: Locale,
    colors: CalendarColors,
    isContentClickable: Boolean,
    previousEnabled: Boolean,
    nextEnabled: Boolean,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDayClick: (LocalDate, List<CalendarEvent<T>>) -> Unit,
    eventIndicator: (@Composable (CalendarEvent<T>, Int, Int) -> Unit)?,
    maxIndicators: CalendarDefaults.IndicatorLimit,
    indicatorLayout: CalendarDefaults.IndicatorLayout
) {
    val monthName = month.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.titlecase(locale) }
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = monthName,
            modifier = Modifier.weight(1f).padding(start = 12.dp).semantics { contentDescription = "$monthName ${month.year}" },
            color = colors.headerContentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onPreviousMonthClick, enabled = previousEnabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.previous_month),
                modifier = Modifier.size(24.dp),
                tint = if (previousEnabled) colors.headerContentColor else colors.inactiveContentColor)
        }
        Spacer(Modifier.width(24.dp))
        IconButton(onClick = onNextMonthClick, enabled = nextEnabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.next_month),
                modifier = Modifier.size(24.dp),
                tint = if (nextEnabled) colors.headerContentColor else colors.inactiveContentColor)
        }
    }
    Row(Modifier.fillMaxWidth()) {
        repeat(7) { index ->
            val day = DayOfWeek.of((firstDayOfWeek.value - 1 + index) % 7 + 1)
            Box(Modifier.weight(1f).heightIn(min = 20.dp), contentAlignment = Alignment.Center) {
                Text(day.getDisplayName(TextStyle.SHORT, locale).removeSuffix(".")
                    .replaceFirstChar { it.titlecase(locale) },
                    color = colors.headerContentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { index ->
                    val day = week.getOrNull(index)
                    Box(Modifier.weight(1f).heightIn(min = 31.dp), contentAlignment = Alignment.TopCenter) {
                        key(day) {
                            if (day != null) {
                                val inMonth = YearMonth.from(day) == month
                                val isSelected = inMonth && day == selectedDate
                                val dayEvents = events[day].orEmpty()
                                val interaction = if (inMonth && isContentClickable) {
                                    Modifier.selectable(selected = isSelected, onClick = { onDayClick(day, dayEvents) })
                                } else Modifier
                                Column(
                                    modifier = Modifier.fillMaxWidth().then(interaction).semantics(mergeDescendants = true) {
                                        contentDescription = day.format(dateFormatter)
                                        selected = isSelected
                                        if (!inMonth || !isContentClickable) disabled()
                                    },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        Modifier.size(30.dp).then(
                                            if (isSelected) Modifier.background(colors.selectedBackgroundColor, CircleShape)
                                            else Modifier
                                        ), contentAlignment = Alignment.Center
                                    ) {
                                        Text(day.dayOfMonth.toString(), fontSize = 12.sp,
                                            color = when {
                                                !inMonth -> colors.inactiveContentColor
                                                isSelected -> colors.selectedContentColor
                                                else -> colors.contentColor
                                            })
                                    }
                                    if (inMonth && dayEvents.isNotEmpty() && eventIndicator != null) {
                                        EventIcon(dayEvents, eventIndicator, maxIndicators, indicatorLayout)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
