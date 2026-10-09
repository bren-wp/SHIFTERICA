package hr.raspored.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object RasporedColors {
    val Bg = Color(0xFF090F1B)
    val Bg2 = Color(0xFF102139)
    val Card = Color(0xF015243B)
    val Card2 = Color(0xFF1B304C)
    val Stroke = Color(0xFF345273)
    val StrokeSoft = Color(0x803E5D7A)
    val Text = Color(0xFFF4F7FD)
    val Muted = Color(0xFFB8C7DB)
    val Accent = Color(0xFF7CEBD6)
    val Accent2 = Color(0xFF91A7FF)
    val Weekend = Color(0xFFFFA0B5)
    val Danger = Color(0xFFFF8C9C)
    val Night = Color(0xFFF8CC7B)
    val Day = Color(0xFF78C6FF)
    val Annual = Color(0xFF82DFAD)
    val Morning = Color(0xFF87DDD5)
    val Afternoon = Color(0xFFFFB47D)
    val Sick = Color(0xFFD3B3FF)
    val Empty = Color(0xFF192B42)
    val WeekendEmpty = Color(0xFF312438)

    val AppGradient = Brush.verticalGradient(listOf(Bg, Bg2, Color(0xFF070C16)))
}

private val Scheme = darkColorScheme(
    primary = RasporedColors.Accent,
    secondary = RasporedColors.Accent2,
    background = RasporedColors.Bg,
    surface = RasporedColors.Card,
    onPrimary = Color(0xFF081A25),
    onBackground = RasporedColors.Text,
    onSurface = RasporedColors.Text,
    error = RasporedColors.Danger
)

@Composable
fun RasporedTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
