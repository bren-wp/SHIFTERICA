package hr.raspored.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftDeletionPolicyTest {
    @Test
    fun unusedCustomShiftMayBeDeletedAfterConfirmation() {
        val schedule = listOf("D", "N", "GO", "BO", "AB")
        assertEquals(0, ShiftDeletionPolicy.assignedDates(schedule, "XY"))
        assertTrue(ShiftDeletionPolicy.canDelete(schedule, "XY"))
    }

    @Test
    fun usedCustomShiftIsProtectedAcrossEntireSchedule() {
        val schedule = listOf("XY", "D", "XY", "GO", "N", "XY")
        assertEquals(3, ShiftDeletionPolicy.assignedDates(schedule, "XY"))
        assertFalse(ShiftDeletionPolicy.canDelete(schedule, "XY"))
        // Codes are distinct: no prefix match and no implicit deletion.
        assertTrue(ShiftDeletionPolicy.canDelete(schedule, "X"))
    }

    @Test
    fun recheckingAfterDateRemovalEnablesSafeDeletion() {
        val before = listOf("AB", "D", "AB")
        val after = listOf("D")
        assertFalse(ShiftDeletionPolicy.canDelete(before, "AB"))
        assertTrue(ShiftDeletionPolicy.canDelete(after, "AB"))
    }
}
