package hr.raspored.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Reactive, persisted UI preferences used directly by the Compose screens. */
class UiSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("raspored.ui.settings", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(prefs.getString("themeMode", "Automatski") ?: "Automatski")
        private set
    var showOutsideDays by mutableStateOf(prefs.getBoolean("showOutsideDays", true))
        private set
    var dayNumberSize by mutableStateOf(prefs.getString("dayNumberSize", "M") ?: "M")
        private set
    var highlightWeekends by mutableStateOf(prefs.getBoolean("highlightWeekends", true))
        private set
    var showAlarmIcons by mutableStateOf(prefs.getBoolean("showAlarmIcons", true))
        private set
    var showNoteIcons by mutableStateOf(prefs.getBoolean("showNoteIcons", true))
        private set
    var highlightToday by mutableStateOf(prefs.getBoolean("highlightToday", true))
        private set
    var todayShape by mutableStateOf(prefs.getString("todayShape", "Zaobljeni kvadrat") ?: "Zaobljeni kvadrat")
        private set
    var todayColorIndex by mutableStateOf(prefs.getInt("todayColorIndex", 1))
        private set
    var todayOpacity by mutableStateOf(prefs.getInt("todayOpacity", 50))
        private set

    var workSector by mutableStateOf(prefs.getString("workSector", "Univerzalno") ?: "Univerzalno")
        private set

    var language by mutableStateOf(prefs.getString("language", "Automatski (Hrvatski)") ?: "Automatski (Hrvatski)")
        private set
    var firstWeekday by mutableStateOf(prefs.getString("firstWeekday", "PON") ?: "PON")
        private set
    var timeFormat by mutableStateOf(prefs.getString("timeFormat", "Automatski") ?: "Automatski")
        private set
    var dateFormat by mutableStateOf(prefs.getString("dateFormat", "Automatski") ?: "Automatski")
        private set

    var showNotesInCell by mutableStateOf(prefs.getBoolean("showNotesInCell", true))
        private set
    var noteTextSize by mutableStateOf(prefs.getString("noteTextSize", "M") ?: "M")
        private set
    var noteBackgroundOpacity by mutableStateOf(prefs.getInt("noteBackgroundOpacity", 50))
        private set

    fun updateThemeMode(value: String) = saveString("themeMode", value) { themeMode = value }
    fun updateShowOutsideDays(value: Boolean) = saveBoolean("showOutsideDays", value) { showOutsideDays = value }
    fun updateDayNumberSize(value: String) = saveString("dayNumberSize", value) { dayNumberSize = value }
    fun updateHighlightWeekends(value: Boolean) = saveBoolean("highlightWeekends", value) { highlightWeekends = value }
    fun updateShowAlarmIcons(value: Boolean) = saveBoolean("showAlarmIcons", value) { showAlarmIcons = value }
    fun updateShowNoteIcons(value: Boolean) = saveBoolean("showNoteIcons", value) { showNoteIcons = value }
    fun updateHighlightToday(value: Boolean) = saveBoolean("highlightToday", value) { highlightToday = value }
    fun updateTodayShape(value: String) = saveString("todayShape", value) { todayShape = value }
    fun updateTodayColorIndex(value: Int) = saveInt("todayColorIndex", value.coerceIn(0, 6)) { todayColorIndex = value.coerceIn(0, 6) }
    fun updateTodayOpacity(value: Int) = saveInt("todayOpacity", value.coerceIn(25, 100)) { todayOpacity = value.coerceIn(25, 100) }
    fun updateWorkSector(value: String) = saveString("workSector", value) { workSector = value }
    fun updateLanguage(value: String) = saveString("language", value) { language = value }
    fun updateFirstWeekday(value: String) = saveString("firstWeekday", value) { firstWeekday = value }
    fun updateTimeFormat(value: String) = saveString("timeFormat", value) { timeFormat = value }
    fun updateDateFormat(value: String) = saveString("dateFormat", value) { dateFormat = value }
    fun updateShowNotesInCell(value: Boolean) = saveBoolean("showNotesInCell", value) { showNotesInCell = value }
    fun updateNoteTextSize(value: String) = saveString("noteTextSize", value) { noteTextSize = value }
    fun updateNoteBackgroundOpacity(value: Int) = saveInt("noteBackgroundOpacity", value.coerceIn(25, 100)) { noteBackgroundOpacity = value.coerceIn(25, 100) }

    private inline fun saveBoolean(key: String, value: Boolean, update: () -> Unit) {
        update(); prefs.edit().putBoolean(key, value).apply()
    }
    private inline fun saveString(key: String, value: String, update: () -> Unit) {
        update(); prefs.edit().putString(key, value).apply()
    }
    private inline fun saveInt(key: String, value: Int, update: () -> Unit) {
        update(); prefs.edit().putInt(key, value).apply()
    }
}
