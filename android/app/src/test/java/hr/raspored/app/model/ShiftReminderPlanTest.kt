package hr.raspored.app.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ShiftReminderPlanTest {
    @Test fun dayAndNightNotificationsHaveIndependentTimes() {
        val events = ShiftReminderPlan.upcoming(
            mapOf(LocalDate.of(2026,10,12) to "D", LocalDate.of(2026,10,13) to "N"),
            LocalDateTime.of(2026,10,11,19,0)
        )
        assertEquals(4, events.size)
        assertEquals("2026-10-11T20:00", events[0].at.toString())
        assertEquals("2026-10-12T06:00", events[1].at.toString())
        assertEquals("2026-10-12T20:00", events[2].at.toString())
        assertEquals("2026-10-13T18:00", events[3].at.toString())
        assertEquals(4, events.map { it.identifier }.toSet().size)
    }

    @Test fun afterTheReminderTimePastEventsAreNeverScheduled() {
        val date = LocalDate.of(2026,10,12)
        val events = ShiftReminderPlan.upcoming(
            mapOf(date to "D", date.plusDays(1) to "GO"),
            LocalDateTime.of(2026,10,12,7,0)
        )
        assertTrue(events.isEmpty())
    }

    @Test fun togglesAndRemovedShiftsCannotCreateStaleReminders() {
        val date = LocalDate.of(2026,10,12)
        val now = LocalDateTime.of(2026,10,11,19,0)
        assertEquals(1, ShiftReminderPlan.upcoming(
            mapOf(date to "N"), now, eveningEnabled = false
        ).size)
        assertEquals(0, ShiftReminderPlan.upcoming(
            mapOf(date to "BO"), now
        ).size)
        assertEquals(0, ShiftReminderPlan.upcoming(
            emptyMap(), now
        ).size)
    }

    @Test fun noRemindersGeneratedForFarFutureUntilRollingRefresh() {
        val now = LocalDateTime.of(2026,10,11,19,0)
        val events = ShiftReminderPlan.upcoming(
            mapOf(LocalDate.of(2027,3,2) to "D"), now
        )
        assertTrue(events.isEmpty())
    }
    @Test fun todayNightDepartureIsKeptAfterPreviousEveningPassed() {
        val date = LocalDate.of(2026, 10, 12)
        val events = ShiftReminderPlan.upcoming(
            mapOf(date to "N"), LocalDateTime.of(2026, 10, 12, 17, 0)
        )
        assertEquals(1, events.size)
        assertEquals(ShiftReminderPlan.Kind.DEPARTURE, events.single().kind)
        assertEquals("2026-10-12T18:00", events.single().at.toString())
    }

    @Test fun sixtyDayHorizonIsInclusiveAndPastDatesAreExcluded() {
        val now = LocalDateTime.of(2026, 10, 11, 19, 0)
        val today = now.toLocalDate()
        val events = ShiftReminderPlan.upcoming(
            mapOf(
                today.minusDays(1) to "D",
                today.plusDays(60) to "D",
                today.plusDays(61) to "N"
            ), now
        )
        assertEquals(2, events.size)
        assertTrue(events.all { it.shiftDate == today.plusDays(60) })
    }

}
