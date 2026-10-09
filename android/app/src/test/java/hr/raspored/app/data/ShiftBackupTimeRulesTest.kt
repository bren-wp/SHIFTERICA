package hr.raspored.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftBackupTimeRulesTest {
    @Test
    fun oldColorOnlyBackupRemainsValidForEveryBuiltinCode() {
        listOf("N", "D", "P", "J", "GO", "BO").forEach { code ->
            assertTrue(ShiftBackupTimeRules.valid(code, null, null))
        }
    }

    @Test
    fun customizedWorkHoursSupportOvernightAndMinutePrecision() {
        assertTrue(ShiftBackupTimeRules.valid("N", "20:30", "08:30"))
        assertTrue(ShiftBackupTimeRules.valid("D", "07:15", "19:00"))
        assertTrue(ShiftBackupTimeRules.valid("J", "06:00", "14:00"))
        assertTrue(ShiftBackupTimeRules.valid("P", "14:00", "22:00"))
    }

    @Test
    fun malformedOrIncompleteBackupCannotAlterExistingHours() {
        assertFalse(ShiftBackupTimeRules.valid("N", "19:00", null))
        assertFalse(ShiftBackupTimeRules.valid("D", null, "19:00"))
        assertFalse(ShiftBackupTimeRules.valid("N", "19:00", "19:00"))
        assertFalse(ShiftBackupTimeRules.valid("D", "25:00", "19:00"))
        assertFalse(ShiftBackupTimeRules.valid("P", "14:60", "22:00"))
        assertFalse(ShiftBackupTimeRules.valid("J", "7:00", "15:00"))
        assertFalse(ShiftBackupTimeRules.valid("GO", "07:00", "15:00"))
        assertFalse(ShiftBackupTimeRules.valid("BO", "07:00", null))
        assertFalse(ShiftBackupTimeRules.valid("UNKNOWN", null, null))
    }
}
