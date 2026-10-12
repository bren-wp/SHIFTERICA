package hr.raspored.kalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = CalendarStore(applicationContext)
        var notice by mutableStateOf<String?>(null)
        var pendingExport: ByteArray? = null

        val export = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            val bytes = pendingExport
            pendingExport = null
            if (uri != null && bytes != null) {
                notice = runCatching {
                    checkNotNull(contentResolver.openOutputStream(uri)) {
                        "Nije moguće otvoriti odabranu datoteku."
                    }.use { it.write(bytes) }
                    "Sigurnosna kopija spremljena."
                }.getOrElse { "Izvoz nije uspio: " + (it.message ?: "Pogreška") }
            }
        }

        val importer = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                notice = runCatching {
                    val bytes = checkNotNull(contentResolver.openInputStream(uri)) {
                        "Datoteka nije dostupna."
                    }.use(CalendarBackup::readLimited)
                    val imported = CalendarBackup.decode(bytes)
                    val added = store.mergeMissing(imported)
                    "Uvezeno " + added + " novih zapisa. Postojeći su sačuvani."
                }.getOrElse { "Uvoz nije uspio: " + (it.message ?: "Neispravna datoteka") }
            }
        }

        setContent {
            CalendarScreen(
                store = store,
                onExport = {
                    runCatching { CalendarBackup.encode(store.entries.toList()) }
                        .onSuccess {
                            pendingExport = it
                            export.launch("Raspored_app-backup.json")
                        }
                        .onFailure {
                            notice = "Izvoz nije uspio: " + (it.message ?: "Pogreška")
                        }
                },
                onImport = {
                    importer.launch(arrayOf("application/json", "text/plain"))
                },
                notice = notice
            )
        }
    }
}
