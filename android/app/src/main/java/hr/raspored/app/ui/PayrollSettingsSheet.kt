package hr.raspored.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hr.raspored.app.data.PayrollSettingsStore
import hr.raspored.app.model.payroll.CroatianPayrollRules
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PayrollSettingsSheet(
    store: PayrollSettingsStore,
    onDismiss: () -> Unit
) {
    var coefficientText by remember(store.coefficient) {
        mutableStateOf(String.format(Locale.US, "%.2f", store.coefficient))
    }
    var birthYearText by remember(store.birthYear) {
        mutableStateOf(if (store.birthYear > 0) store.birthYear.toString() else "")
    }
    var lowerTaxText by remember(store.lowerTaxPercent) {
        mutableStateOf(String.format(Locale.US, "%.1f", store.lowerTaxPercent))
    }
    var higherTaxText by remember(store.higherTaxPercent) {
        mutableStateOf(String.format(Locale.US, "%.1f", store.higherTaxPercent))
    }

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
                Text(
                    "Procjena plaće",
                    color = RasporedColors.Text,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Lokalni orijentacijski obračun iz rasporeda i službenih pravila.",
                    color = RasporedColors.Muted
                )
            }

            item {
                SettingsGroup("Parametri obračuna", Icons.Rounded.Calculate) {
                    SettingsToggle(
                        "Prikaži procjenu",
                        "Procjena se računa isključivo na uređaju",
                        store.enabled,
                        store::updateEnabled
                    )

                    val presets = CroatianPayrollRules.coefficientPresets
                    SettingsMenu(
                        title = "Radno mjesto / koeficijent",
                        value = "${store.coefficientLabel} — ${String.format(Locale.US, "%.2f", store.coefficient)}",
                        values = presets.map { "${it.label} — ${String.format(Locale.US, "%.2f", it.coefficient)}" },
                        onSelect = { selected ->
                            presets.firstOrNull {
                                selected.startsWith(it.label + " —")
                            }?.let { store.selectCoefficientPreset(it.id) }
                            coefficientText = String.format(Locale.US, "%.2f", store.coefficient)
                        }
                    )

                    OutlinedTextField(
                        value = coefficientText,
                        onValueChange = { coefficientText = it },
                        label = { Text("Ručni koeficijent") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = payrollFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    PayrollCounterRow(
                        title = "Navršene godine staža",
                        value = store.completedYearsService,
                        onMinus = { store.updateCompletedYearsService(store.completedYearsService - 1) },
                        onPlus = { store.updateCompletedYearsService(store.completedYearsService + 1) }
                    )
                    PayrollCounterRow(
                        title = "Djeca na poreznoj kartici",
                        value = store.children,
                        onMinus = { store.updateChildren(store.children - 1) },
                        onPlus = { store.updateChildren(store.children + 1) }
                    )
                    PayrollCounterRow(
                        title = "Ostali uzdržavani članovi",
                        value = store.dependents,
                        onMinus = { store.updateDependents(store.dependents - 1) },
                        onPlus = { store.updateDependents(store.dependents + 1) }
                    )

                    OutlinedTextField(
                        value = birthYearText,
                        onValueChange = { birthYearText = it.filter(Char::isDigit).take(4) },
                        label = { Text("Godina rođenja — opcionalno") },
                        supportingText = { Text("Služi samo za procjenu godišnje olakšice za mlade.") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RasporedColors.Accent,
                            unfocusedBorderColor = RasporedColors.Stroke,
                            focusedTextColor = RasporedColors.Text,
                            unfocusedTextColor = RasporedColors.Text,
                            focusedLabelColor = RasporedColors.Accent,
                            unfocusedLabelColor = RasporedColors.Muted,
                            focusedSupportingTextColor = RasporedColors.Muted,
                            unfocusedSupportingTextColor = RasporedColors.Muted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SettingsToggle(
                        "Rad u turnusu",
                        "Primijeni 5% turnus; dodatak za drugu smjenu tada se ne kumulira",
                        store.turnusEnabled,
                        store::updateTurnusEnabled
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = lowerTaxText,
                            onValueChange = { lowerTaxText = it },
                            label = { Text("Niža %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = payrollFieldColors()
                        )
                        OutlinedTextField(
                            value = higherTaxText,
                            onValueChange = { higherTaxText = it },
                            label = { Text("Viša %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = payrollFieldColors()
                        )
                    }
                    Text(
                        "Zadano je 20% / 25% za Rijeku u 2026. Stope ovise o prebivalištu pa ih je moguće promijeniti.",
                        color = RasporedColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Button(
                        onClick = {
                            coefficientText.replace(',', '.').toDoubleOrNull()?.let(store::updateCoefficient)
                            store.updateBirthYear(birthYearText.toIntOrNull() ?: 0)
                            val lower = lowerTaxText.replace(',', '.').toDoubleOrNull() ?: store.lowerTaxPercent
                            val higher = higherTaxText.replace(',', '.').toDoubleOrNull() ?: store.higherTaxPercent
                            store.updateTaxRates(lower, higher)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RasporedColors.Accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Spremi", color = RasporedColors.Text)
                    }
                }
            }

            item {
                Text(
                    "Izvori ugrađenih pravila: NN 11/2026, NN 29/2024, NN 22/2024, NN 4/2025, NN 152/2024 i službena tumačenja TKU-a. Procjena nije službena obračunska isprava.",
                    color = RasporedColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun PayrollCounterRow(
    title: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            title,
            color = RasporedColors.Text,
            modifier = Modifier.weight(1f).padding(top = 10.dp)
        )
        OutlinedButton(onClick = onMinus) { Text("−") }
        Text(
            value.toString(),
            color = RasporedColors.Text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
        )
        OutlinedButton(onClick = onPlus) { Text("+") }
    }
}

@Composable
private fun payrollFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RasporedColors.Accent,
    unfocusedBorderColor = RasporedColors.Stroke,
    focusedTextColor = RasporedColors.Text,
    unfocusedTextColor = RasporedColors.Text,
    focusedLabelColor = RasporedColors.Accent,
    unfocusedLabelColor = RasporedColors.Muted
)
