package hr.raspored.app.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import hr.raspored.app.MainActivity
import hr.raspored.app.R
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.data.UiSettingsStore
import hr.raspored.app.model.ShiftReminderPlan
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/** Local-only reminders, no analytics, server, background network or schedule deletion. */
object ShiftReminders {
    const val ACTION_NOTIFY = "hr.raspored.app.REMINDER"
    const val ACTION_REFRESH = "hr.raspored.app.REFRESH_REMINDERS"
    private const val PREFS = "raspored.reminder.alarm.ids"
    private const val CHANNEL_EVENING = "raspored_evening_v1"
    private const val CHANNEL_SHIFT = "raspored_shift_v1"

    fun refresh(context: Context) {
        val schedule = ScheduleStore(context).snapshot()
        val settings = UiSettingsStore(context)
        refresh(context, schedule, settings.remindersEnabled,
            settings.eveningReminderEnabled, settings.shiftTimeReminderEnabled,
            ShiftLibraryStore(context).all.mapNotNull { shift ->
                shift.start?.let { shift.code to it }
            }.toMap())
    }

    fun refresh(
        context: Context,
        entries: Map<LocalDate, String>,
        enabled: Boolean,
        eveningEnabled: Boolean,
        shiftTimeEnabled: Boolean,
        startTimes: Map<String, String> = emptyMap()
    ) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // Cancel every previously managed alarm before applying edited schedules.
        val previous = prefs.getString("ids", "").orEmpty().split(",")
            .mapNotNull { it.toIntOrNull() }
        previous.forEach { id ->
            manager.cancel(alarmIntent(context, id, null))
        }
        val plan = if (enabled) ShiftReminderPlan.upcoming(
            entries, LocalDateTime.now(), eveningEnabled, shiftTimeEnabled,
            startTimes = startTimes
        ) else emptyList()
        val future = plan.filter { reminder ->
            reminder.at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >
                System.currentTimeMillis()
        }
        future.forEach { reminder ->
            val intent = alarmIntent(context, reminder.identifier, reminder)
            val at = reminder.at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // Inexact by design: no restricted exact-alarm permission or Play policy abuse.
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
        prefs.edit().putString("ids", future.joinToString(",") { it.identifier.toString() }).apply()

        // Daily rolling refresh keeps shifts scheduled beyond the 60-day window.
        val maintenance = PendingIntent.getBroadcast(
            context, -4096, Intent(context, ShiftReminderReceiver::class.java)
                .setAction(ACTION_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (enabled) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 3)
                set(Calendar.MINUTE, 15)
                set(Calendar.SECOND, 0)
            }
            manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, cal.timeInMillis,
                AlarmManager.INTERVAL_DAY, maintenance
            )
        } else manager.cancel(maintenance)
    }

    private fun alarmIntent(
        context: Context, id: Int, event: ShiftReminderPlan.Event?
    ): PendingIntent {
        val i = Intent(context, ShiftReminderReceiver::class.java).setAction(ACTION_NOTIFY)
        if (event != null) {
            i.putExtra("date", event.shiftDate.toString())
            i.putExtra("code", event.code)
            i.putExtra("kind", event.kind.name)
        }
        return PendingIntent.getBroadcast(
            context, id, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun notifyShift(context: Context, date: LocalDate, code: String, kind: String) {
        val settings = UiSettingsStore(context)
        if (!settings.remindersEnabled ||
            (kind == "EVENING" && !settings.eveningReminderEnabled) ||
            (kind == "DEPARTURE" && !settings.shiftTimeReminderEnabled)) return
        // The receiver is allowed to deliver only for a currently assigned shift.
        if (ScheduleStore(context).code(date) != code || code !in listOf("D", "N")) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        createChannels(context)
        val shift = ShiftLibraryStore(context).byCode(code)
        val start = shift?.start ?: if (code == "D") "07:00" else "19:00"
        val end = shift?.end ?: if (code == "D") "19:00" else "07:00"
        val title = if (kind == "EVENING") "Sutra imate smjenu $code" else
            "Smjena $code danas u $start"
        val detail = if (kind == "EVENING") {
            "Sutra radite $code od $start do $end."
        } else "Pripremite se za smjenu $code koja počinje u $start."
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            context, if (kind == "EVENING") CHANNEL_EVENING else CHANNEL_SHIFT
        )
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setPriority(if (kind == "EVENING") NotificationCompat.PRIORITY_DEFAULT
                else NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (kind == "EVENING") NotificationCompat.CATEGORY_REMINDER
                else NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(
            (date.toEpochDay() * 4 + if (kind == "EVENING") 0 else 1).toInt(),
            notification
        )
    }

    private fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_EVENING, "Raspored · večernji podsjetnik",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Podsjetnik u 20:00 za smjenu sljedeći dan" })
        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_SHIFT, "Raspored · početak smjene",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Zvučna obavijest u 06:00 za D i 18:00 za N" })
    }
}

class ShiftReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ShiftReminders.ACTION_REFRESH, Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED ->
                ShiftReminders.refresh(context)
            ShiftReminders.ACTION_NOTIFY -> {
                val date = intent.getStringExtra("date")
                    ?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return
                val code = intent.getStringExtra("code") ?: return
                val kind = intent.getStringExtra("kind") ?: return
                ShiftReminders.notifyShift(context, date, code, kind)
            }
        }
    }
}
