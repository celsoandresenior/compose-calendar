package dev.alejo.compose_calendar

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import dev.alejo.compose_calendar.util.CalendarDefaults
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class CompactCalendarScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun rendersReferenceMonthWithSelectedDay() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                MaterialTheme {
                    SimpleComposeCalendar(
                        modifier = Modifier.width(301.dp).testTag("reference-calendar"),
                        initDate = LocalDate.of(2023, 2, 1),
                        initialSelectedDate = LocalDate.of(2023, 2, 20),
                        firstDayOfWeek = DayOfWeek.SUNDAY,
                        locale = Locale.forLanguageTag("pt-BR"),
                        calendarColors = CalendarDefaults.calendarColors(
                            backgroundColor = Color.White, headerBackgroundColor = Color.White,
                            contentColor = Color.Black, headerContentColor = Color.Black
                        )
                    )
                }
            }
        }
        compose.onNodeWithText("Fevereiro").assertExists()
        compose.onNodeWithText("20").assertIsSelected()
        val image = compose.onNodeWithTag("reference-calendar").captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.cacheDir, "compact-calendar.png").outputStream().use {
            check(image.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        // Export a persistent preview only when explicitly requested by the test invocation.
        val exportScreenshot = InstrumentationRegistry.getArguments().getString("exportScreenshot") == "true"
        if (exportScreenshot && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "compact-calendar-final.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ComposeCalendar")
            }
            val uri = checkNotNull(context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            checkNotNull(context.contentResolver.openOutputStream(uri)).use {
                check(image.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        }
    }
}
