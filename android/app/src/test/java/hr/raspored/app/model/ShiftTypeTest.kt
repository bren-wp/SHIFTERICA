package hr.raspored.app.model

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftTypeTest {
    @Test
    fun referenceShiftsMatchExpectedMonthlyTotals() {
        assertEquals(6 * 60, ShiftCatalog.night.durationMinutes)
        assertEquals(7 * 60, ShiftCatalog.day.durationMinutes)
        assertEquals(10 * 60, ShiftCatalog.morning.durationMinutes)
        assertEquals(8 * 60, ShiftCatalog.sick.durationMinutes)

        val octoberMinutes = 9 * ShiftCatalog.night.durationMinutes +
            8 * ShiftCatalog.day.durationMinutes

        assertEquals(110 * 60, octoberMinutes)
    }

    @Test
    fun overnightIntervalsWrapAcrossMidnight() {
        val shift = ShiftType(
            code = "X",
            name = "Test",
            shortName = "Test",
            start = "21:00",
            end = "07:00",
            color = Color.Black
        )

        assertEquals(10 * 60, shift.durationMinutes)
    }

    @Test
    fun splitIntervalsAreAddedTogether() {
        val shift = ShiftType(
            code = "BO",
            name = "Bolovanje",
            shortName = "Bolovanje",
            start = "10:00",
            end = "14:00",
            secondaryStart = "16:00",
            secondaryEnd = "20:00",
            color = Color.Black
        )

        assertEquals(8 * 60, shift.durationMinutes)
    }
}
