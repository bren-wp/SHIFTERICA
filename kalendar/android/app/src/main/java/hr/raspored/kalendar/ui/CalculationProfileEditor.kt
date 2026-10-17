package hr.raspored.kalendar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hr.raspored.kalendar.data.MonthlyAccountingStore

/** Separate, deliberately empty calculation profile; no user's salary data is bundled. */
@Composable
internal fun CalculationProfileEditor(accounting: MonthlyAccountingStore) {
    var coefficient by remember(accounting.userCoefficient) {
        mutableStateOf(accounting.userCoefficient.takeIf { it > 0 }?.toString() ?: "")
    }
    var base by remember(accounting.userMonthlyBase) {
        mutableStateOf(accounting.userMonthlyBase.takeIf { it > 0 }?.toString() ?: "")
    }
    var invalid by remember { mutableStateOf(false) }

    Surface(
        color = RasporedColors.Card, shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Podaci za izračun", color = RasporedColors.Text,
                fontWeight = FontWeight.Black)
            Text("Bod i mjesečna osnovica nisu unaprijed uneseni. " +
                "Unesite vlastite vrijednosti kako biste uključili procjenu.",
                color = RasporedColors.Muted)
            OutlinedTextField(
                value = coefficient,
                onValueChange = { coefficient = it.take(18); invalid = false },
                label = { Text("Bod / koeficijent") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = base,
                onValueChange = { base = it.take(18); invalid = false },
                label = { Text("Mjesečna osnovica (€)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            if (invalid) Text("Unesite valjane pozitivne brojeve.",
                color = MaterialTheme.colorScheme.error)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val point = coefficient.trim().replace(',', '.').toDoubleOrNull()
                    val amount = base.trim().replace(',', '.').toDoubleOrNull()
                    if (point != null && amount != null && point in 0.0001..100.0 &&
                        amount in 0.01..100_000.0) {
                        accounting.updateCalculationInputs(point, amount)
                        invalid = false
                    } else invalid = true
                }
            ) { Text("Spremi parametre") }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    accounting.updateCalculationInputs(0.0, 0.0)
                    coefficient = ""
                    base = ""
                    invalid = false
                }
            ) { Text("Očisti parametre") }
        }
    }
}
