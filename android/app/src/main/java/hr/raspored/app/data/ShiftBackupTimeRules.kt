package hr.raspored.app.data

/** Shared v1 backup time contract. Old backups omit both fields. */
internal object ShiftBackupTimeRules {
    private val timePattern = Regex("""^(?:[01]\d|2[0-3]):[0-5]\d$""")
    private val shiftWithHours = setOf("N", "D", "P", "J")
    private val absenceCodes = setOf("GO", "BO")

    fun valid(code: String, start: String?, end: String?): Boolean {
        if (code !in shiftWithHours && code !in absenceCodes) return false
        if (start == null && end == null) return true // 1.13.0 color-only backup
        if (code !in shiftWithHours || start == null || end == null) return false
        return timePattern.matches(start) && timePattern.matches(end) && start != end
    }
}
