package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.data.ScheduleBackup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.UiSettingsStore
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import hr.raspored.app.reminders.ShiftReminders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSheet(
    store: UiSettingsStore,
    schedule: ScheduleStore,
    library: ShiftLibraryStore,
    onDismiss: () -> Unit
) {
    var showSupport by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var notificationsAllowed by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAllowed = granted
        if (granted) ShiftReminders.refresh(context)
    }


    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            backupMessage = runCatching {
                val payload = ScheduleBackup.encode(schedule, library)
                check(payload.toByteArray(Charsets.UTF_8).size <= ScheduleBackup.MAX_BYTES) {
                    "Sigurnosna kopija prelazi dopuštenu veličinu."
                }
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(payload.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: error("Nije moguće zapisati odabranu datoteku.")
                "Sigurnosna kopija rasporeda je spremljena."
            }.getOrElse { "Izvoz nije uspio: " + (it.message ?: "Nepoznata pogreška.") }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            backupMessage = runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var count: Int
                    while (stream.read(buffer).also { count = it } != -1) {
                        check(output.size() + count <= ScheduleBackup.MAX_BYTES) {
                            "Sigurnosna kopija je prevelika."
                        }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                } ?: error("Nije moguće otvoriti odabranu datoteku.")
                val imported = ScheduleBackup.restore(
                    bytes.toString(Charsets.UTF_8), schedule, library
                )
                "Dodano je " + imported.addedDates +
                    " nedostajućih datuma. Postojeći raspored nije prepisan."
            }.getOrElse { "Uvoz nije uspio: " + (it.message ?: "Neispravna datoteka.") }
        }
    }

    val todayColors = listOf(
        RasporedColors.Night,
        RasporedColors.Day,
        RasporedColors.Annual,
        RasporedColors.Morning,
        Color(0xFFB16CE4),
        Color(0xFFFF5BAA),
        Color(0xFFFF853A)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Postavke", color = RasporedColors.Text, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("Prilagodite Raspored svojim potrebama", color = RasporedColors.Muted)
            }
            item {
                SettingsGroup("Podsjetnici za smjene", Icons.Rounded.NotificationsActive) {
                    SettingsToggle(
                        "Podsjetnici uključeni",
                        "Zadano uključeni; slanje ovisi o dopuštenju sustava",
                        store.remindersEnabled, store::updateRemindersEnabled
                    )
                    if (store.remindersEnabled) {
                        SettingsToggle(
                            "Večer prije · 20:00",
                            "Sutra dnevna ili noćna smjena",
                            store.eveningReminderEnabled, store::updateEveningReminderEnabled
                        )
                        SettingsToggle(
                            "Prije početka smjene",
                            "D u 06:00 · N u 18:00",
                            store.shiftTimeReminderEnabled, store::updateShiftTimeReminderEnabled
                        )
                        if (!notificationsAllowed) {
                            Text(
                                "Obavijesti nisu dopuštene. Uključite ih kako biste primali podsjetnike.",
                                color = Color(0xFFFFC66B), fontSize = 12.sp
                            )
                            Button(onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) { Text("Dopusti obavijesti") }
                            TextButton(onClick = {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                ).apply {
                                    data = android.net.Uri.parse("package:" + context.packageName)
                                }
                                context.startActivity(intent)
                            }) { Text("Postavke obavijesti na uređaju") }
                        }
                        Text(
                            "Dolazi zvučna obavijest, ne alarm koji zvoni bez prekida. " +
                                "Android može malo odgoditi dostavu radi štednje baterije.",
                            color = RasporedColors.Muted, fontSize = 11.sp
                        )
                    }
                }
            }
            item {
                SettingsGroup("Vizualno", Icons.Rounded.Palette) {
                    SettingsSegmented("Tamni način rada", "Odaberite izgled aplikacije", listOf("Automatski", "Uključen", "Isključen"), store.themeMode, store::updateThemeMode)
                    SettingsToggle("Prikaz praznih dana", "Prikaži dane izvan odabranog mjeseca", store.showOutsideDays, store::updateShowOutsideDays)
                    SettingsSegmented("Veličina brojeva dana u mjesecu", "Odaberite veličinu brojeva u kalendaru", listOf("XS", "S", "M", "L", "XL"), store.dayNumberSize, store::updateDayNumberSize)
                    SettingsToggle("Istakni vikende", "Oboji subotu i nedjelju drugačijom bojom", store.highlightWeekends, store::updateHighlightWeekends)
                    SettingsToggle("Ikone alarma", "Prikaži ikonu za dane s alarmima", store.showAlarmIcons, store::updateShowAlarmIcons)
                    SettingsToggle("Ikone bilješki", "Prikaži ikonu za dane s bilješkama", store.showNoteIcons, store::updateShowNoteIcons)
                    SettingsToggle("Istakni današnji dan", "Prilagodite izgled današnjeg datuma", store.highlightToday, store::updateHighlightToday)
                    if (store.highlightToday) {
                        SettingsShapeSelector(store.todayShape, store::updateTodayShape)
                        Text("Boja", color = RasporedColors.Text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            todayColors.forEachIndexed { index, color ->
                                Surface(
                                    onClick = { store.updateTodayColorIndex(index) },
                                    color = color,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(40.dp),
                                    border = BorderStroke(if (store.todayColorIndex == index) 2.dp else 1.dp, if (store.todayColorIndex == index) RasporedColors.Accent else RasporedColors.StrokeSoft)
                                ) {}
                            }
                        }
                        SettingsSegmented("Prozirnost", "Postavite prozirnost isticanja", listOf("25%", "50%", "75%", "100%"), store.todayOpacity.toString() + "%", { store.updateTodayOpacity(it.removeSuffix("%").toInt()) }, compact = true)
                    }
                }
            }
            item {
                SettingsGroup("Jezik i vrijeme", Icons.Rounded.Language) {
                    SettingsMenu(
                        title = "Jezik",
                        value = store.language,
                        values = listOf("Automatski (Hrvatski)", "Hrvatski"),
                        onSelect = store::updateLanguage
                    )
                    SettingsSegmented("Prvi dan u tjednu", "Odaberite koji dan počinje tjedan", listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"), store.firstWeekday, store::updateFirstWeekday, compact = true)
                    SettingsSegmented("Format vremena", "Odaberite prikaz vremena", listOf("Automatski", "24 h", "AM/PM"), store.timeFormat, store::updateTimeFormat)
                    SettingsSegmented("Format datuma", "Odaberite format datuma", listOf("Automatski", "dd.MM.gggg", "MM/dd/gggg", "gggg/MM/dd"), store.dateFormat, store::updateDateFormat, compact = true)
                }
            }
            item {
                SettingsGroup("Bilješke", Icons.Rounded.Notes) {
                    SettingsToggle("Prikaži bilješke u dnevnoj ćeliji", "Prikaži tekst bilješki unutar ćelija kalendara", store.showNotesInCell, store::updateShowNotesInCell)
                    SettingsSegmented("Veličina teksta bilješke", "Odaberite veličinu teksta u dnevnim ćelijama", listOf("XS", "S", "M", "L", "XL"), store.noteTextSize, store::updateNoteTextSize)
                    SettingsSegmented("Prozirnost pozadine", "Postavite prozirnost pozadine bilješki", listOf("25%", "50%", "75%", "100%"), store.noteBackgroundOpacity.toString() + "%", { store.updateNoteBackgroundOpacity(it.removeSuffix("%").toInt()) }, compact = true)
                }
            }
            item {
                SettingsGroup("Sigurnost podataka", Icons.Rounded.Save) {
                    SettingsStatic("Izvezi raspored", "Spremi sigurnosnu kopiju na uređaj") {
                        exportLauncher.launch("Raspored-sigurnosna-kopija.json")
                    }
                    SettingsStatic("Uvezi raspored", "Dodaj nedostajuće datume bez brisanja postojećih") {
                        importLauncher.launch(arrayOf("application/json", "text/plain"))
                    }
                    Text(
                        "Ažuriranje aplikacije čuva lokalne zapise. Za oporavak nakon brisanja ili gubitka uređaja čuvajte vlastitu kopiju.",
                        color = RasporedColors.Muted, fontSize = 11.sp
                    )
                }
            }
            item {
                SettingsGroup("Podrška i privatnost", Icons.Rounded.Shield) {
                    SettingsStatic("Podržite nas!", "Dobrovoljna podrška bez otključavanja funkcija") {
                        showSupport = true
                    }
                    SettingsStatic("Uvjeti korištenja", "Pravila korištenja aplikacije") {
                        uriHandler.openUri("https://raspored.eu/uvjeti-koristenja")
                    }
                    SettingsStatic("Politika privatnosti", "Kako se štite vaši podaci") {
                        uriHandler.openUri("https://raspored.eu/politika-privatnosti")
                    }
                    SettingsStatic("Izrada aplikacije", "Brendigo") {
                        uriHandler.openUri("https://brendigo.com")
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showSupport) {
        SupportDialog(onDismiss = { showSupport = false })
    }
    backupMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { backupMessage = null },
            title = { Text("Sigurnosna kopija") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { backupMessage = null }) { Text("U redu") }
            }
        )
    }


}
