package hr.raspored.app.model

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftTypeTest {
    @Test
    fun builtInShiftDurationsMatchProductionRules() {
        assertEquals(12 * 60, ShiftCatalog.night.durationMinutes)
        assertEquals(12 * 60, ShiftCatalog.day.durationMinutes)
        assertEquals(8 * 60, ShiftCatalog.morning.durationMinutes)
        assertEquals(8 * 60, ShiftCatalog.afternoon.durationMinutes)
        assertEquals(0, ShiftCatalog.sick.durationMinutes)

        val octoberMinutes = 9 * ShiftCatalog.night.durationMinutes +
            8 * ShiftCatalog.day.durationMinutes

        assertEquals(204 * 60, octoberMinutes)
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
            code = "X2",
            name = "Podijeljena smjena",
            shortName = "X2",
            start = "10:00",
            end = "14:00",
            secondaryStart = "16:00",
            secondaryEnd = "20:00",
            color = Color.Black
        )

        assertEquals(8 * 60, shift.durationMinutes)
    }
    @Test
    fun overlappingCustomIntervalsDisplayActualPlannedUnion() {
        val shift = ShiftCatalog.afternoon.copy(
            code = "XY", start = "08:00", end = "16:00",
            secondaryStart = "14:00", secondaryEnd = "20:00",
            custom = true
        )
        assertEquals(12 * 60, shift.durationMinutes)
    }

    @Test
    fun overnightOverlappingIntervalsDoNotInflateDisplayedDuration() {
        val shift = ShiftCatalog.night.copy(
            code = "XY", start = "20:00", end = "04:00",
            secondaryStart = "22:00", secondaryEnd = "02:00",
            custom = true
        )
        assertEquals(8 * 60, shift.durationMinutes)
        assertEquals(24 * 60, shift.copy(start = "08:00", end = "08:00",
            secondaryStart = null, secondaryEnd = null).durationMinutes)
        assertEquals(0, shift.copy(start = null, end = null,
            secondaryStart = null, secondaryEnd = null).durationMinutes)
    }

    @Test
    fun overnightSecondaryIntervalAfterMidnightUsesFollowingDay() {
        val base = ShiftCatalog.night.copy(
            code = "XY", start = "20:00", end = "06:00", custom = true
        )
        assertEquals(10 * 60, base.copy(
            secondaryStart = "01:00", secondaryEnd = "04:00"
        ).durationMinutes)
        assertEquals(12 * 60, base.copy(
            secondaryStart = "07:00", secondaryEnd = "09:00"
        ).durationMinutes)
        assertEquals(11 * 60, base.copy(
            secondaryStart = "18:00", secondaryEnd = "19:00"
        ).durationMinutes)
        assertEquals(0, ShiftIntervalMath.secondaryDayOffset(
            "08:00", "16:00", "02:00", "04:00"
        ))
        assertEquals(1L, ShiftIntervalMath.secondaryDayOffset(
            "20:00", "06:00", "01:00", "04:00"
        ))
    }

}
