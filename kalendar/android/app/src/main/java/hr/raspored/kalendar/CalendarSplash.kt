package hr.raspored.kalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.YearMonth

@Composable
internal fun CalendarSplash() {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, animationSpec = tween(durationMillis = 1100)) }
    Box(
        Modifier
            .fillMaxSize()
            .background(ParityStyle.gradient)
            .systemBarsPadding()
    ) {
        SplashBackdrop()
        SplashTile("D", ParityStyle.tiles[1], Modifier.align(Alignment.TopStart).offset(x = 42.dp, y = 170.dp).rotate(-12f), 76.dp)
        SplashTile("N", ParityStyle.tiles[5], Modifier.align(Alignment.TopEnd).offset(x = (-38).dp, y = 220.dp).rotate(13f), 72.dp)
        SplashTile("GO", ParityStyle.tiles[4], Modifier.align(Alignment.CenterStart).offset(x = (-20).dp, y = 120.dp).rotate(-11f), 70.dp)
        SplashTile("J", ParityStyle.tiles[0], Modifier.align(Alignment.CenterEnd).offset(x = 18.dp, y = 150.dp).rotate(10f), 70.dp)
        SplashTile("BO", ParityStyle.tiles[2], Modifier.align(Alignment.BottomEnd).offset(x = (-45).dp, y = (-125).dp).rotate(9f), 74.dp)

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier.size(148.dp),
                color = ParityStyle.accent.copy(alpha = .07f),
                shape = RoundedCornerShape(46.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ParityStyle.accent.copy(alpha = .34f)
                ),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CalendarSplashMark(Modifier.size(126.dp))
                }
            }
            Text(
                "Raspored",
                color = ParityStyle.text,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Pametni planer smjena",
                color = ParityStyle.muted,
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
                color = ParityStyle.accent,
                trackColor = ParityStyle.card2.copy(alpha = .86f)
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
            color = ParityStyle.text.copy(alpha = .18f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        repeat(5) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(7) { column ->
                    val accent = when {
                        (row + column) % 5 == 0 -> ParityStyle.tiles[1].copy(alpha = .20f)
                        (row + column) % 4 == 0 -> ParityStyle.tiles[5].copy(alpha = .17f)
                        else -> ParityStyle.card2.copy(alpha = .30f)
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

@Composable
private fun CalendarSplashMark(modifier: Modifier) {
    BoxWithConstraints(modifier) {
        val unit = maxWidth
        Surface(
            modifier = Modifier.fillMaxSize(), color = ParityStyle.card2,
            shape = RoundedCornerShape(unit * .32f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ParityStyle.accent)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(unit * .065f)
                ) {
                    Box(Modifier.size(unit * .54f, unit * .15f)
                        .background(ParityStyle.accent, RoundedCornerShape(unit * .07f)))
                    repeat(2) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(unit * .065f)) {
                            repeat(3) { column ->
                                Box(Modifier.size(unit * .13f).background(
                                    if (row == 0 && column == 1) ParityStyle.tiles[1]
                                    else ParityStyle.accent,
                                    RoundedCornerShape(unit * .04f)
                                ))
                            }
                        }
                    }
                }
            }
        }
    }
}
