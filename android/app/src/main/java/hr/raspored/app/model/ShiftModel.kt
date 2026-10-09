package hr.raspored.app.model

import androidx.compose.ui.graphics.Color
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Visual and scheduling definition for a shift code. */
data class ShiftType(
    val code: String,
    val name: String,
    val shortName: String,
    val start: String? = null,
    val end: String? = null,
    val secondaryStart: String? = null,
    val secondaryEnd: String? = null,
    val color: Color,
    val textColor: Color = Color(0xFF06131F),
    val fontSize: Int = 12,
    val custom: Boolean = false
) {
    /** Displayed local-clock union; real DST-aware minutes are calculated by CroatianWorkTime. */
    val durationMinutes: Int
        get() = ShiftIntervalMath.plannedDurationMinutes(
            start, end, secondaryStart, secondaryEnd
        )

    val timeText: String?
        get() = if (start == null || end == null) null
        else if (secondaryStart != null && secondaryEnd != null) "$start – $end / $secondaryStart – $secondaryEnd"
        else "$start – $end"
}

/** Both Android duration display and actual payroll use the SAME interval placement. */
internal object ShiftIntervalMath {
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    private fun bounds(from: String?, to: String?): Pair<Int, Int>? {
        if (from == null || to == null) return null
        return runCatching {
            val startMinute = LocalTime.parse(from, timeFormat).toSecondOfDay() / 60
            val endMinute = LocalTime.parse(to, timeFormat).toSecondOfDay() / 60
            startMinute to if (endMinute > startMinute) endMinute else endMinute + 1440
        }.getOrNull()
    }

    private fun gap(a: Pair<Int, Int>, b: Pair<Int, Int>): Int = when {
        b.second < a.first -> a.first - b.second
        b.first > a.second -> b.first - a.second
        else -> 0
    }

    /**
     * The second interval of an overnight primary shift may start after midnight.
     * Place it on the nearer civil date rather than treating 01:00 as the previous
     * morning. Equal-distance ties retain the original (shift) date for stability.
     */
    fun secondaryDayOffset(
        firstStart: String?, firstEnd: String?,
        secondStart: String?, secondEnd: String?
    ): Long {
        val first = bounds(firstStart, firstEnd) ?: return 0
        val second = bounds(secondStart, secondEnd) ?: return 0
        if (first.second < 1440 || first.first == 0 && first.second == 1440) return 0
        val tomorrow = (second.first + 1440) to (second.second + 1440)
        return if (gap(first, tomorrow) < gap(first, second)) 1 else 0
    }

    fun plannedDurationMinutes(
        firstStart: String?, firstEnd: String?,
        secondStart: String?, secondEnd: String?
    ): Int {
        val first = bounds(firstStart, firstEnd)
        val rawSecond = bounds(secondStart, secondEnd)
        if (first == null) return rawSecond?.let { it.second - it.first } ?: 0
        if (rawSecond == null) return first.second - first.first
        val offset = secondaryDayOffset(firstStart, firstEnd, secondStart, secondEnd).toInt() * 1440
        val second = (rawSecond.first + offset) to (rawSecond.second + offset)
        val overlap = (minOf(first.second, second.second) - maxOf(first.first, second.first))
            .coerceAtLeast(0)
        return first.second - first.first + second.second - second.first - overlap
    }
}


object ShiftCatalog {
    val night = ShiftType("N", "Noćna smjena", "Noćna", "19:00", "07:00", color = Color(0xFFFFD21F))
    val day = ShiftType("D", "Dnevna smjena", "Dnevna", "07:00", "19:00", color = Color(0xFF13B7F3))
    val annual = ShiftType("GO", "Godišnji odmor", "Godišnji", color = Color(0xFF6CEB82))
    val morning = ShiftType("J", "Jutarnja smjena", "Jutarnja", "07:00", "15:00", color = Color(0xFF77DED7))
    val afternoon = ShiftType("P", "Popodnevna smjena", "Popodnevna", "14:00", "22:00", color = Color(0xFFFF8A3D))
    val sick = ShiftType("BO", "Bolovanje", "Bolovanje", color = Color(0xFFD991EE))

    val all = listOf(night, day, afternoon, morning, annual, sick)
    fun byCode(code: String?): ShiftType? = all.firstOrNull { it.code == code }
}
