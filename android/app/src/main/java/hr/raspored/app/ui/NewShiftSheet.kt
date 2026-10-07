package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.model.ShiftType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewShiftSheet(
    library: ShiftLibraryStore,
    initialShift: ShiftType? = null,
    onDismiss: () -> Unit
) {
    val editing = initialShift != null
    var name by remember(initialShift?.code) {
        mutableStateOf(initialShift?.name ?: "Nova smjena")
    }
    var abbr by remember(initialShift?.code) {
        mutableStateOf(initialShift?.code ?: "Nova")
    }
    var size by remember(initialShift?.code) {
        mutableFloatStateOf(initialShift?.fontSize?.toFloat() ?: 12f)
    }
    var bg by remember(initialShift?.code) {
        mutableStateOf(initialShift?.color ?: RasporedColors.Night)
    }
    var fg by remember(initialShift?.code) {
        mutableStateOf(initialShift?.textColor ?: Color.Black)
    }
    var tab by remember { mutableIntStateOf(0) }
    var start by remember(initialShift?.code) {
        mutableStateOf(initialShift?.start.orEmpty())
    }
    var end by remember(initialShift?.code) {
        mutableStateOf(initialShift?.end.orEmpty())
    }
    var secondaryStart by remember(initialShift?.code) {
        mutableStateOf(initialShift?.secondaryStart.orEmpty())
    }
    var secondaryEnd by remember(initialShift?.code) {
        mutableStateOf(initialShift?.secondaryEnd.orEmpty())
    }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = RasporedColors.Card2
                        )
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, "Natrag")
                    }
                    Column {
                        Text(
                            if (editing) "Uredi smjenu" else "Nova smjena",
                            color = RasporedColors.Text,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            if (editing) "Prilagodite izgled i radno vrijeme" else "Kreirajte novu smjenu",
                            color = RasporedColors.Muted
                        )
                    }
                }
            }

            item {
                LabeledField("Naziv smjene", name) {
                    name = it
                    error = null
                }
            }

            item {
                SegmentedSettings(
                    listOf("Izgled", "Raspored"),
                    if (tab == 0) "Izgled" else "Raspored",
                    onSelect = { tab = if (it == "Izgled") 0 else 1 }
                )
            }

            if (tab == 0) {
                item {
                    LabeledFieldWithCounter(
                        label = "Skraćenica",
                        value = abbr,
                        counter = abbr.length.toString() + "/4",
                        enabled = !editing
                    ) {
                        abbr = it.take(4)
                        error = null
                    }
                }
                item {
                    ColorCard(
                        "Boja pozadine",
                        listOf(
                            RasporedColors.Night,
                            RasporedColors.Day,
                            RasporedColors.Afternoon,
                            RasporedColors.Annual,
                            RasporedColors.Morning,
                            RasporedColors.Sick,
                            Color(0xFFFF5F67),
                            Color(0xFF36485A)
                        ),
                        bg
                    ) { bg = it }
                }
                item {
                    ColorCard(
                        "Boja teksta",
                        listOf(
                            Color.White,
                            Color(0xFFD8D8D8),
                            Color(0xFFAAAAAA),
                            Color(0xFF777777),
                            Color(0xFF444444),
                            Color.Black
                        ),
                        fg
                    ) { fg = it }
                }
                item { FontSizeCard(size = size, onChange = { size = it }) }
            } else {
                item {
                    Surface(
                        color = RasporedColors.Card,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, RasporedColors.Stroke),
                        shadowElevation = 5.dp
                    ) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Vrijeme smjene",
                                color = RasporedColors.Text,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                "Koristite format HH:mm. Smjene preko ponoći podržane su automatski.",
                                color = RasporedColors.Muted,
                                fontSize = 11.sp
                            )
                            CustomTimePair(
                                "Prvi interval",
                                start,
                                end,
                                { start = it; error = null },
                                { end = it; error = null }
                            )
                            CustomTimePair(
                                "Drugi interval (neobavezno)",
                                secondaryStart,
                                secondaryEnd,
                                { secondaryStart = it; error = null },
                                { secondaryEnd = it; error = null }
                            )
                        }
                    }
                }
            }

            error?.let { message ->
                item {
                    Text(
                        message,
                        color = RasporedColors.Danger,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Odustani")
                    }
                    Button(
                        onClick = {
                            library.save(
                                name = name,
                                code = abbr,
                                background = bg,
                                textColor = fg,
                                fontSize = size.toInt(),
                                start = start,
                                end = end,
                                secondaryStart = secondaryStart,
                                secondaryEnd = secondaryEnd
                            )
                                .onSuccess { onDismiss() }
                                .onFailure {
                                    error = it.message ?: "Smjena nije spremljena."
                                }
                        },
                        modifier = Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RasporedColors.Accent
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 9.dp,
                            pressedElevation = 3.dp
                        )
                    ) {
                        Text(
                            "Spremi",
                            color = Color(0xFF04131F),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
