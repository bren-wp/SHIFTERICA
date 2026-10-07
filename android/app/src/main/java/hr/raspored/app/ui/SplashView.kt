package hr.raspored.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.data.UiSettingsStore
import java.time.YearMonth


@Composable
internal fun SplashView() {
    Box(
        Modifier
            .fillMaxSize()
            .background(RasporedColors.AppGradient)
            .systemBarsPadding()
    ) {
        SplashBackdrop()
        SplashTile("D", RasporedColors.Day, Modifier.align(Alignment.TopStart).offset(x = 42.dp, y = 170.dp).rotate(-12f), 76.dp)
        SplashTile("N", RasporedColors.Night, Modifier.align(Alignment.TopEnd).offset(x = (-38).dp, y = 220.dp).rotate(13f), 72.dp)
        SplashTile("GO", RasporedColors.Annual, Modifier.align(Alignment.CenterStart).offset(x = (-20).dp, y = 120.dp).rotate(-11f), 70.dp)
        SplashTile("J", RasporedColors.Morning, Modifier.align(Alignment.CenterEnd).offset(x = 18.dp, y = 150.dp).rotate(10f), 70.dp)
        SplashTile("BO", RasporedColors.Sick, Modifier.align(Alignment.BottomEnd).offset(x = (-45).dp, y = (-125).dp).rotate(9f), 74.dp)

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AppMark(Modifier.size(126.dp))
            Text("Raspored", color = RasporedColors.Text, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text("Pametni planer smjena", color = RasporedColors.Muted, fontSize = 17.sp)
            Spacer(Modifier.height(78.dp))
            LinearProgressIndicator(
                progress = { .68f },
                modifier = Modifier.width(250.dp).height(5.dp).clip(RoundedCornerShape(4.dp)),
                color = RasporedColors.Accent,
                trackColor = RasporedColors.Card2
            )
        }
    }
}

@Composable
private fun SplashTile(code: String, color: Color, modifier: Modifier, size: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = modifier.size(size),
        color = color,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .52f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(code, color = Color(0xFF06131F), fontSize = if (code.length == 1) 27.sp else 20.sp, fontWeight = FontWeight.Black)
        }
    }
}


@Composable
private fun BoxScope.SplashBackdrop() {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = 72.dp)
            .rotate(-5f)
            .width(320.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            YearMonth.now().month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("hr", "HR")).uppercase() + " " + YearMonth.now().year,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = RasporedColors.Text.copy(alpha = .18f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        repeat(5) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(7) { column ->
                    val accent = when {
                        (row + column) % 5 == 0 -> RasporedColors.Day.copy(alpha = .20f)
                        (row + column) % 4 == 0 -> RasporedColors.Night.copy(alpha = .17f)
                        else -> RasporedColors.Card2.copy(alpha = .30f)
                    }
                    Box(
                        Modifier
                            .size(width = 39.dp, height = 42.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(accent)
                    )
                }
            }
        }
    }
}
