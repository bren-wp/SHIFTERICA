package hr.raspored.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.time.YearMonth

/**
 * Local accounting adjustments. No personal identifiers or pay slips are stored.
 * Explicit monthly overrides never alter or delete scheduled shifts.
 */
class MonthlyAccountingStore(context: Context) {
    private val prefs = context.getSharedPreferences("raspored.accounting.v1", Context.MODE_PRIVATE)
    private val fundHours = mutableStateMapOf<YearMonth, Int>()
    private val confirmedCents = mutableStateMapOf<YearMonth, Long>()
    var serviceYears by mutableStateOf(prefs.getInt("profile:serviceYears", 0).coerceIn(0, 60))
        private set
    var children by mutableStateOf(prefs.getInt("profile:children", 0).coerceIn(0, 9))
        private set
    var dependents by mutableStateOf(prefs.getInt("profile:dependents", 0).coerceIn(0, 10))
        private set

    fun setServiceYears(value: Int) {
        serviceYears = value.coerceIn(0, 60)
        prefs.edit().putInt("profile:serviceYears", serviceYears).commit()
    }

    fun setChildren(value: Int) {
        children = value.coerceIn(0, 9)
        prefs.edit().putInt("profile:children", children).commit()
    }

    fun setDependents(value: Int) {
        dependents = value.coerceIn(0, 10)
        prefs.edit().putInt("profile:dependents", dependents).commit()
    }


    init {
        prefs.all.forEach { (key, raw) ->
            val parts = key.split(':')
            if (parts.size != 2) return@forEach
            val month = runCatching { YearMonth.parse(parts[1]) }.getOrNull() ?: return@forEach
            when (parts[0]) {
                "fund" -> (raw as? Int)?.takeIf { it in 0..744 }?.let { fundHours[month] = it }
                "net" -> (raw as? Long)?.takeIf { it in 0..100_000_000L }?.let {
                    confirmedCents[month] = it
                }
            }
        }
    }

    fun fundOverrideMinutes(month: YearMonth): Int? = fundHours[month]?.times(60)

    fun setFundHours(month: YearMonth, hours: Int?) {
        if (hours == null) {
            fundHours.remove(month)
            prefs.edit().remove("fund:$month").commit()
        } else {
            val safe = hours.coerceIn(0, 744)
            fundHours[month] = safe
            prefs.edit().putInt("fund:$month", safe).commit()
        }
    }

    fun actualNet(month: YearMonth): Double? = confirmedCents[month]?.div(100.0)

    fun setActualNet(month: YearMonth, euros: Double?) {
        if (euros == null || !euros.isFinite() || euros < 0) {
            confirmedCents.remove(month)
            prefs.edit().remove("net:$month").commit()
        } else {
            val cents = kotlin.math.round(euros.coerceAtMost(1_000_000.0) * 100).toLong()
            confirmedCents[month] = cents
            prefs.edit().putLong("net:$month", cents).commit()
        }
    }

    fun actualForYear(year: Int): List<Pair<YearMonth, Double>> =
        confirmedCents.entries
            .asSequence()
            .filter { it.key.year == year }
            .sortedBy { it.key }
            .map { it.key to it.value / 100.0 }
            .toList()

    fun latestThreeActual(upTo: YearMonth): List<Double> =
        confirmedCents.entries
            .asSequence()
            .filter { !it.key.isAfter(upTo) }
            .sortedByDescending { it.key }
            .take(3)
            .map { it.value / 100.0 }
            .toList()
}
