package hr.raspored.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hr.raspored.app.model.payroll.CroatianPayrollRules

/**
 * Local-only payroll preferences. No value leaves the device and no account,
 * cloud service or analytics endpoint is required for the estimate.
 */
class PayrollSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("raspored.payroll.settings", Context.MODE_PRIVATE)

    var enabled by mutableStateOf(prefs.getBoolean("enabled", true))
        private set
    var coefficientPresetId by mutableStateOf(
        prefs.getString("coefficientPresetId", "worker-iii") ?: "worker-iii"
    )
        private set
    var coefficient by mutableStateOf(
        prefs.getString("coefficient", "1.25")?.toDoubleOrNull() ?: 1.25
    )
        private set
    var completedYearsService by mutableStateOf(prefs.getInt("completedYearsService", 0))
        private set
    var children by mutableStateOf(prefs.getInt("children", 0))
        private set
    var dependents by mutableStateOf(prefs.getInt("dependents", 0))
        private set
    var birthYear by mutableStateOf(prefs.getInt("birthYear", 0))
        private set
    var turnusEnabled by mutableStateOf(prefs.getBoolean("turnusEnabled", false))
        private set
    var lowerTaxPercent by mutableStateOf(
        prefs.getString("lowerTaxPercent", "20.0")?.toDoubleOrNull() ?: 20.0
    )
        private set
    var higherTaxPercent by mutableStateOf(
        prefs.getString("higherTaxPercent", "25.0")?.toDoubleOrNull() ?: 25.0
    )
        private set

    val coefficientLabel: String
        get() = CroatianPayrollRules.coefficientPresets
            .firstOrNull { it.id == coefficientPresetId }
            ?.label ?: "Ručni koeficijent"

    fun updateEnabled(value: Boolean) = saveBoolean("enabled", value) { enabled = value }

    fun selectCoefficientPreset(id: String) {
        val preset = CroatianPayrollRules.coefficientPresets.firstOrNull { it.id == id } ?: return
        prefs.edit()
            .putString("coefficientPresetId", preset.id)
            .putString("coefficient", preset.coefficient.toString())
            .apply()
        coefficientPresetId = preset.id
        coefficient = preset.coefficient
    }

    fun updateCoefficient(value: Double) {
        val safe = value.coerceIn(0.1, 10.0)
        prefs.edit()
            .putString("coefficientPresetId", "manual")
            .putString("coefficient", safe.toString())
            .apply()
        coefficientPresetId = "manual"
        coefficient = safe
    }

    fun updateCompletedYearsService(value: Int) =
        saveInt("completedYearsService", value.coerceIn(0, 60)) {
            completedYearsService = value.coerceIn(0, 60)
        }

    fun updateChildren(value: Int) =
        saveInt("children", value.coerceIn(0, 9)) { children = value.coerceIn(0, 9) }

    fun updateDependents(value: Int) =
        saveInt("dependents", value.coerceIn(0, 9)) { dependents = value.coerceIn(0, 9) }

    fun updateBirthYear(value: Int) =
        saveInt("birthYear", value.coerceIn(0, 2100)) { birthYear = value.coerceIn(0, 2100) }

    fun updateTurnusEnabled(value: Boolean) =
        saveBoolean("turnusEnabled", value) { turnusEnabled = value }

    fun updateTaxRates(lower: Double, higher: Double) {
        val safeLower = lower.coerceIn(0.0, 50.0)
        val safeHigher = higher.coerceIn(0.0, 50.0)
        prefs.edit()
            .putString("lowerTaxPercent", safeLower.toString())
            .putString("higherTaxPercent", safeHigher.toString())
            .apply()
        lowerTaxPercent = safeLower
        higherTaxPercent = safeHigher
    }

    private inline fun saveBoolean(key: String, value: Boolean, update: () -> Unit) {
        update()
        prefs.edit().putBoolean(key, value).apply()
    }

    private inline fun saveInt(key: String, value: Int, update: () -> Unit) {
        update()
        prefs.edit().putInt(key, value).apply()
    }
}
