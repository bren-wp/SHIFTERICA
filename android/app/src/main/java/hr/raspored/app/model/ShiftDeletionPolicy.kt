package hr.raspored.app.model

/** Protects calendar references to custom shift definitions. Does not mutate dates. */
internal object ShiftDeletionPolicy {
    fun assignedDates(codes: Collection<String>, code: String): Int =
        codes.count { it == code }

    fun canDelete(codes: Collection<String>, code: String): Boolean =
        assignedDates(codes, code) == 0
}
