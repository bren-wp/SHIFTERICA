package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            "Važne termine i unose provjerite prije njihove uporabe."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSettings(
    weekView: Boolean,
    onWeekViewChange: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    notice: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
        }.getOrDefault("—")
    }
    var page by remember { mutableStateOf<InfoPage?>(null) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ParityStyle.bg2,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = ParityStyle.muted) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Postavke", color = ParityStyle.text,
                fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Prilagodite Raspored svojim potrebama",
                color = ParityStyle.muted, fontSize = 13.sp)
            CalendarSettingsGroup("Vizualno", Icons.Rounded.Palette) {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tjedni prikaz", color = ParityStyle.text,
                            fontWeight = FontWeight.SemiBold)
                        Text("Prikaži sedam dana umjesto cijelog mjeseca",
                            color = ParityStyle.muted, fontSize = 11.sp)
                    }
                    Switch(checked = weekView, onCheckedChange = onWeekViewChange)
                }
            }
            CalendarSettingsGroup("Sigurnost podataka", Icons.Rounded.Security) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        "Izvezite vlastite smjene, datume i bilješke prije promjene uređaja.",
                        color = ParityStyle.muted, fontSize = 12.sp
                    )
                    OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 48.dp)) {
                        Text("Izvezi raspored", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 48.dp)) {
                        Text("Uvezi raspored", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "Uvoz spaja nedostajuće zapise bez prepisivanja postojećih.",
                        color = ParityStyle.muted, fontSize = 11.sp
                    )
                    if (notice != null) Text(notice,
                        color = ParityStyle.accent, fontSize = 12.sp)
                }
            }
            CalendarSettingsGroup("Podrška i privatnost", Icons.Rounded.Security) {
                TextButton(
                    onClick = { page = InfoPage.PRIVACY },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Politika privatnosti", color = ParityStyle.accent) }
                TextButton(
                    onClick = { page = InfoPage.TERMS },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Uvjeti korištenja", color = ParityStyle.accent) }
            }
            CalendarSettingsGroup("O aplikaciji", Icons.Rounded.Info) {
                Text("Autor: Brendigo", color = ParityStyle.text)
                Text("Verzija: $version", color = ParityStyle.muted, fontSize = 12.sp)
            }
            Button(onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("Gotovo", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
    page?.let { selected ->
        AlertDialog(
            onDismissRequest = { page = null },
            containerColor = ParityStyle.card2,
            title = { Text(selected.title, color = ParityStyle.text) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(selected.body, color = ParityStyle.text)
                    Text("Brendigo", modifier = Modifier.padding(top = 16.dp),
                        color = ParityStyle.muted)
                }
            },
            confirmButton = {
                TextButton(onClick = { page = null }) { Text("Natrag") }
            }
        )
    }
}

@Composable
private fun CalendarSettingsGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ParityStyle.card,
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.dp, ParityStyle.softStroke)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Icon(icon, null, tint = ParityStyle.accent)
                Text(title, color = ParityStyle.text,
                    fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
            content()
        }
    }
}
