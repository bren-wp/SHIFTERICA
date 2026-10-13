package hr.raspored.kalendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class InfoPage(val title: String, val body: String) {
    PRIVACY(
        "Politika privatnosti",
        "Raspored_app čuva unose i odabir prikaza na ovom uređaju. " +
            "Aplikacija ne zahtijeva korisnički račun niti sadrži oglase ili analitičko praćenje. " +
            "Samo kada izaberete Izvezi ili Uvezi, sustav otvara odabir datoteke. " +
            "Izvezena JSON datoteka nije šifrirana, pa je čuvajte na sigurnom mjestu. " +
            "Brisanjem aplikacije mogu se izgubiti njezini podaci; prethodno izvezite kopiju."
    ),
    TERMS(
        "Uvjeti korištenja",
        "Raspored_app namijenjen je osobnom planiranju smjena, događaja i bilješki. " +
            "Prije važnih obveza provjerite unesene datume i vremena te napravite sigurnosnu kopiju. " +
            "Sustavske mogućnosti, poput dostupnosti datoteka, ovise o postavkama uređaja. " +
            "Aplikacija ne pruža službenu evidenciju radnog vremena niti obračun plaće."
    )
}

@Composable
fun CalendarSettings(
    weekView: Boolean,
    onWeekViewChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var page by remember { mutableStateOf<InfoPage?>(null) }
    val selected = page
    if (selected != null) {
        AlertDialog(
            onDismissRequest = { page = null },
            title = { Text(selected.title) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(selected.body, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Brendigo",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { page = null }) { Text("Natrag") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Postavke") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tjedni prikaz", modifier = Modifier.weight(1f))
                        Switch(checked = weekView, onCheckedChange = onWeekViewChange)
                    }
                    HorizontalDivider()
                    TextButton(onClick = { page = InfoPage.PRIVACY }) {
                        Text("Politika privatnosti")
                    }
                    TextButton(onClick = { page = InfoPage.TERMS }) {
                        Text("Uvjeti korištenja")
                    }
                    Text(
                        "Autor: Brendigo",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Gotovo") }
            }
        )
    }
}
