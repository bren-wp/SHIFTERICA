package hr.raspored.kalendar

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EntryRulesTest {
    private fun item(date: String = "2026-10-11") =
        CalendarEntry(id = "test", date = date, title = "Moja smjena")

    @Test fun universalCalendarDefaultsAndEventPresets() {
        val day = LocalDate.parse("2026-10-17")
        assertEquals("Događaj", item().category)
        assertEquals(14, EntryRules.categories.size)
        assertEquals(15, CalendarEventPresets.all.size)
        assertEquals(CalendarEventPresets.all.size,
            CalendarEventPresets.all.map { it.code }.toSet().size)
        CalendarEventPresets.all.forEach { preset ->
            val draft = preset.draft(day)
            assertTrue(EntryRules.valid(draft))
            assertEquals(preset.category, draft.category)
            assertEquals("", draft.start)
            assertEquals("", draft.end)
        }
    }

    @Test fun acceptsFreeformNotesAndOvernightShifts() {
        assertTrue(EntryRules.valid(item().copy(title = "", note = "Bilo kakva napomena")))
        assertTrue(EntryRules.valid(item().copy(start = "22:00", end = "06:00")))
        assertTrue(EntryRules.valid(item().copy(start = "08:00", end = "08:00")))
    }

    @Test fun rejectsBrokenTimesDatesAndEmptyContent() {
        assertFalse(EntryRules.valid(item("2026-02-30")))
        assertFalse(EntryRules.valid(item().copy(title = " ", note = " ")))
        assertFalse(EntryRules.valid(item().copy(start = "27:00", end = "06:00")))
        assertFalse(EntryRules.valid(item().copy(start = "07:00", end = "")))
        assertFalse(EntryRules.valid(item().copy(color = 22)))
    }

    @Test fun searchMatchesCroatianTitlesAndNotesWithoutAccents() {
        val entries = listOf(
            item().copy(id = "one", title = "Noćna smjena", note = "Đurđica čeka"),
            item().copy(id = "two", title = "Jutarnja")
        )
        assertEquals(listOf("one"), CalendarSearch.find(entries, " NOCNA ").map { it.id })
        assertEquals(listOf("one"), CalendarSearch.find(entries, "durdica").map { it.id })
        assertTrue(CalendarSearch.find(entries, "nepoznato").isEmpty())
    }

    @Test fun searchNeverSilentlyDropsResultsAfterTwoHundred() {
        val entries = (1..240).map { number ->
            item().copy(id = "entry-$number", title = "Pregled smjena $number")
        }
        val results = CalendarSearch.find(entries, "pregled smjena")
        assertEquals(240, results.size)
        assertEquals(entries.map { it.id }.toSet(), results.map { it.id }.toSet())
    }

    @Test fun allSixRasporedPresetsAreIndependentValidCalendarRecords() {
        val date = LocalDate.parse("2026-10-14")
        val records = CalendarShiftPresets.builtIn.map { it.forDate(date) }
        assertEquals(listOf("N", "D", "J", "P", "GO", "BO"),
            CalendarShiftPresets.builtIn.map { it.code })
        assertTrue(records.all(EntryRules::valid))
        assertEquals(6, records.map { it.id }.toSet().size)
        assertEquals("19:00", records[0].start)
        assertEquals("07:00", records[0].end)
        assertEquals("", records[4].start)
        assertEquals("Smjena", records[4].category)
    }

    @Test fun quickShiftSelectionPreservesNotesAndDoesNotDuplicate() {
        val day = LocalDate.parse("2026-10-14")
        val preset = CalendarShiftPresets.builtIn.first()
        val entry = preset.forDate(day).copy(
            id = "stable-user-id", note = "Sačuvaj bilješku", color = 3
        )
        assertTrue(CalendarShiftPresets.alreadyScheduled(listOf(entry), preset, day))
        assertFalse(CalendarShiftPresets.alreadyScheduled(
            listOf(entry), preset, day.plusDays(1)
        ))
        assertFalse(CalendarShiftPresets.alreadyScheduled(
            listOf(entry.copy(category = "Napomena")), preset, day
        ))
        assertEquals("Sačuvaj bilješku", entry.note)
    }

    @Test fun scheduledDurationHandlesNightAndUntimedShifts() {
        val day = LocalDate.parse("2026-10-14")
        val night = CalendarShiftPresets.builtIn.first { it.code == "N" }.forDate(day)
        val annual = CalendarShiftPresets.builtIn.first { it.code == "GO" }.forDate(day)
        assertEquals(720, CalendarScheduledTime.minutes(night))
        assertEquals(0, CalendarScheduledTime.minutes(annual))
        assertEquals("12 h 15 min", CalendarScheduledTime.display(735))
    }

    @Test fun genericEventsAreNotMisidentifiedAsShiftCodes() {
        val date = "2026-10-14"
        val event = CalendarEntry(date = date, title = "Noćni izlazak", category = "Događaj")
        assertEquals("NO", calendarTileCode(event))
        assertEquals("RO", calendarTileCode(
            CalendarEventPresets.all.first { it.code == "RO" }.draft(LocalDate.parse(date))
        ))
    }

    @Test fun originalShiftLabelsMatchAnnualAndMonthlyCalendar() {
        val date = java.time.LocalDate.parse("2026-10-14")
        assertEquals("N", calendarTileCode(
            CalendarShiftPresets.builtIn.first { it.code == "N" }.forDate(date)
        ))
        assertEquals("D", calendarTileCode(
            CalendarShiftPresets.builtIn.first { it.code == "D" }.forDate(date)
        ))
        assertEquals("GO", calendarTileCode(
            CalendarShiftPresets.builtIn.first { it.code == "GO" }.forDate(date)
        ))
        assertEquals("BO", calendarTileCode(
            CalendarShiftPresets.builtIn.first { it.code == "BO" }.forDate(date)
        ))
    }

    @Test fun multipleEntriesPerDaySortByStartAndKeepUntimedNotes() {
        val date = LocalDate.parse("2026-10-11")
        val entries = listOf(
            item().copy(id = "a", title = "Bilješka", start = ""),
            item().copy(id = "b", title = "Noćna", start = "22:00", end = "06:00"),
            item().copy(id = "c", title = "Jutro", start = "07:00", end = "15:00"),
            item("2026-10-12").copy(id = "d")
        )
        assertEquals(listOf("c", "b", "a"), EntryRules.forDay(entries, date).map { it.id })
    }

    @Test fun expandedShiftCatalogKeepsLegacyChoicesAndCoversDifferentRosters() {
        val day = LocalDate.parse("2026-10-14")
        val presets = CalendarShiftPresets.all
        assertEquals(6, CalendarShiftPresets.builtIn.size)
        assertTrue(CalendarShiftPresets.additional.size >= 30)
        assertEquals(presets.size, presets.map { it.code }.toSet().size)
        assertTrue(presets.map { it.forDate(day) }.all(EntryRules::valid))
        assertEquals("S3", CalendarShiftPresets.codeFor(
            presets.first { it.code == "S3" }.forDate(day)))
        assertEquals(null, CalendarShiftPresets.codeFor(
            presets.first { it.code == "S3" }.forDate(day).copy(category = "Napomena")))
        assertEquals(1440, CalendarScheduledTime.minutes(
            presets.first { it.code == "24" }.forDate(day)))
        assertEquals(1440, CalendarScheduledTime.minutes(
            presets.first { it.code == "DE" }.forDate(day)))
        assertTrue(CalendarShiftPresets.matches(presets.first { it.code == "6N" }, "nocna"))
        assertTrue(CalendarShiftPresets.matches(presets.first { it.code == "KL" }, "klizno"))
        assertFalse(CalendarShiftPresets.matches(presets.first { it.code == "KL" }, "bolovanje"))
        assertEquals(0, CalendarScheduledTime.minutes(
            presets.first { it.code == "DV" }.forDate(day)))
    }
}
