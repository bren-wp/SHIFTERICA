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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
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
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, animationSpec = tween(durationMillis = 1100)) }
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
            Surface(
                modifier = Modifier.size(148.dp),
                color = RasporedColors.Accent.copy(alpha = .07f),
                shape = RoundedCornerShape(46.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    RasporedColors.Accent.copy(alpha = .34f)
                ),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppMark(Modifier.size(126.dp))
                }
            }
            Text(
                "Raspored",
                color = RasporedColors.Text,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Pametni planer smjena",
                color = RasporedColors.Muted,
                fontSize = 17.sp,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(74.dp))
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .width(270.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = RasporedColors.Accent,
                trackColor = RasporedColors.Card2.copy(alpha = .86f)
            )
        }
    }
}

@Composable
private fun SplashTile(code: String, color: Color, modifier: Modifier, size: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = modifier.size(size),
        color = Color.Transparent,
        shape = RoundedCornerShape(19.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = .56f)
        ),
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(color, color.copy(alpha = .78f))
                    ),
                    RoundedCornerShape(19.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .fillMaxWidth(.58f)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = .30f))
            )
            Text(
                code,
                color = Color(0xFF06131F),
                fontSize = if (code.length == 1) 27.sp else 20.sp,
                fontWeight = FontWeight.Black
            )
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
