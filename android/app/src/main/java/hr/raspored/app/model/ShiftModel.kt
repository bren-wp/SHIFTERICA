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
    val durationMinutes: Int
        get() = intervalMinutes(start, end) + intervalMinutes(secondaryStart, secondaryEnd)

    val timeText: String?
        get() = if (start == null || end == null) null
        else if (secondaryStart != null && secondaryEnd != null) "$start – $end / $secondaryStart – $secondaryEnd"
        else "$start – $end"

    private fun intervalMinutes(from: String?, to: String?): Int {
        if (from == null || to == null) return 0
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val a = LocalTime.parse(from, formatter).toSecondOfDay() / 60
            val b = LocalTime.parse(to, formatter).toSecondOfDay() / 60
            val raw = b - a
            if (raw > 0) raw else raw + 24 * 60
        }.getOrDefault(0)
    }
}

object ShiftCatalog {
    val night = ShiftType("N", "Noćna smjena", "Noćna", "19:00", "07:00", color = Color(0xFFFFD21F))
    val day = ShiftType("D", "Dnevna smjena", "Dnevna", "07:00", "19:00", color = Color(0xFF13B7F3))
    val annual = ShiftType("GO", "Godišnji odmor", "Godišnji", color = Color(0xFF6CEB82))
    val morning = ShiftType("J", "Jutarnja smjena", "Jutarnja", "07:00", "15:00", color = Color(0xFF77DED7))
    val sick = ShiftType("BO", "Bolovanje", "Bolovanje", color = Color(0xFFD991EE))

    val all = listOf(night, day, annual, morning, sick)
    fun byCode(code: String?): ShiftType? = all.firstOrNull { it.code == code }
}
