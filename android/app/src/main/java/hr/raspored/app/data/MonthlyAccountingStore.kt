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
    // Explicit historical seniority. Never infer prior years from today's figure.
    private val monthlySeniority = mutableStateMapOf<YearMonth, Int>()
    /** Per-work-month payment delay; default is the next calendar month. */
    private val monthlyPaymentDelay = mutableStateMapOf<YearMonth, Int>()
    /** Cent-exact GO hourly overrides for each work month; legacy global value is fallback. */
    private val monthlyLeaveRateCents = mutableStateMapOf<YearMonth, Int>()
    var profileConfirmed by mutableStateOf(prefs.getBoolean("profile:confirmed", false))
        private set

    fun confirmPayrollProfile() {
        profileConfirmed = true
        prefs.edit().putBoolean("profile:confirmed", true).commit()
    }

    private fun markProfileForReview() {
        if (!profileConfirmed) return
        profileConfirmed = false
        prefs.edit().putBoolean("profile:confirmed", false).commit()
    }

    var serviceYears by mutableStateOf(prefs.getInt("profile:serviceYears", 0).coerceIn(0, 60))
        private set
    var children by mutableStateOf(prefs.getInt("profile:children", 0).coerceIn(0, 9))
        private set
    var dependents by mutableStateOf(prefs.getInt("profile:dependents", 0).coerceIn(0, 10))
        private set
    var annualLeaveHourlyGross by mutableStateOf(
        prefs.getFloat("profile:annualLeaveHourlyGross", 0f).toDouble().coerceIn(0.0, 1000.0)
    )
        private set

    fun updateAnnualLeaveHourlyGross(value: Double) {
        markProfileForReview()
        annualLeaveHourlyGross = if (value.isFinite()) value.coerceIn(0.0, 1000.0) else 0.0
        prefs.edit().putFloat("profile:annualLeaveHourlyGross",
            annualLeaveHourlyGross.toFloat()).commit()
    }

    /** A month-specific GO rate must never silently rewrite older payslip estimates. */
    fun annualLeaveHourlyGrossForMonth(month: YearMonth): Double =
        monthlyLeaveRateCents[month]?.div(100.0) ?: annualLeaveHourlyGross

    fun annualLeaveHourlyGrossOverride(month: YearMonth): Double? =
        monthlyLeaveRateCents[month]?.div(100.0)

    fun setAnnualLeaveHourlyGrossForMonth(month: YearMonth, euros: Double?) {
        if (euros != null && (!euros.isFinite() || euros < 0.01 || euros > 1000.0)) return
        markProfileForReview()
        if (euros == null) {
            monthlyLeaveRateCents.remove(month)
            prefs.edit().remove("go-rate-cents:$month").commit()
        } else {
            val cents = kotlin.math.round(euros * 100.0).toInt().coerceIn(1, 100_000)
            monthlyLeaveRateCents[month] = cents
            prefs.edit().putInt("go-rate-cents:$month", cents).commit()
        }
    }

    fun updateServiceYears(value: Int) {
        markProfileForReview()
        serviceYears = value.coerceIn(0, 60)
        prefs.edit().putInt("profile:serviceYears", serviceYears).commit()
    }

    fun updateChildren(value: Int) {
        markProfileForReview()
        children = value.coerceIn(0, 9)
        prefs.edit().putInt("profile:children", children).commit()
    }

    fun updateDependents(value: Int) {
        markProfileForReview()
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
                "seniority" -> (raw as? Int)?.takeIf { it in 0..60 }?.let {
                    monthlySeniority[month] = it
                }
                "payment-delay" -> (raw as? Int)?.takeIf { it in 0..12 }?.let {
                    monthlyPaymentDelay[month] = it
                }
                "go-rate-cents" -> (raw as? Int)?.takeIf { it in 1..100_000 }?.let {
                    monthlyLeaveRateCents[month] = it
                }
            }
        }
    }

    /** The global profile applies unless an explicitly verified month differs. */
    fun serviceYearsForMonth(month: YearMonth): Int =
        monthlySeniority[month] ?: serviceYears

    fun serviceYearsOverride(month: YearMonth): Int? = monthlySeniority[month]

    fun setServiceYearsForMonth(month: YearMonth, years: Int?) {
        markProfileForReview()
        if (years == null) {
            monthlySeniority.remove(month)
            prefs.edit().remove("seniority:$month").commit()
        } else {
            val safe = years.coerceIn(0, 60)
            monthlySeniority[month] = safe
            prefs.edit().putInt("seniority:$month", safe).commit()
        }
    }

    fun paymentDelayMonths(month: YearMonth): Int = monthlyPaymentDelay[month] ?: 1

    fun paymentDelayOverride(month: YearMonth): Int? = monthlyPaymentDelay[month]

    fun setPaymentDelayMonths(month: YearMonth, delay: Int?) {
        markProfileForReview()
        if (delay == null) {
            monthlyPaymentDelay.remove(month)
            prefs.edit().remove("payment-delay:$month").commit()
        } else {
            val valid = delay.coerceIn(0, 12)
            monthlyPaymentDelay[month] = valid
            prefs.edit().putInt("payment-delay:$month", valid).commit()
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
