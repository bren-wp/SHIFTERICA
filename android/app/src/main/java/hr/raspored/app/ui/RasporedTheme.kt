package hr.raspored.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object RasporedColors {
    val Bg = Color(0xFF061624)
    val Bg2 = Color(0xFF0A2235)
    val Card = Color(0xE612293D)
    val Card2 = Color(0xFF102A40)
    val Stroke = Color(0xFF2A5D7D)
    val StrokeSoft = Color(0x66396F8F)
    val Text = Color(0xFFF6F8FB)
    val Muted = Color(0xFFAFC1D8)
    val Accent = Color(0xFF19DCE0)
    val Accent2 = Color(0xFF08A8F0)
    val Weekend = Color(0xFFFF7186)
    val Danger = Color(0xFFFF6778)
    val Night = Color(0xFFFFD21F)
    val Day = Color(0xFF13B7F3)
    val Annual = Color(0xFF6CEB82)
    val Morning = Color(0xFF77DED7)
    val Sick = Color(0xFFD991EE)
    val Empty = Color(0xFF122B40)
    val WeekendEmpty = Color(0xFF352436)

    val AppGradient = Brush.verticalGradient(listOf(Color(0xFF071726), Color(0xFF071D2E), Color(0xFF04111D)))
}

private val Scheme = darkColorScheme(
    primary = RasporedColors.Accent,
    secondary = RasporedColors.Accent2,
    background = RasporedColors.Bg,
    surface = RasporedColors.Card,
    onPrimary = Color(0xFF04131F),
    onBackground = RasporedColors.Text,
    onSurface = RasporedColors.Text,
    error = RasporedColors.Danger
)

@Composable
fun RasporedTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
