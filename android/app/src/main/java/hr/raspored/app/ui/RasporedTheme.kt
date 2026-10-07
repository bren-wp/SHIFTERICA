package hr.raspored.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object RasporedColors {
    val Bg = Color(0xFF051522)
    val Bg2 = Color(0xFF0A263B)
    val Card = Color(0xEA112B41)
    val Card2 = Color(0xFF102E46)
    val Stroke = Color(0xFF347291)
    val StrokeSoft = Color(0x80507F9B)
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
    val Afternoon = Color(0xFFFF8A3D)
    val Sick = Color(0xFFD991EE)
    val Empty = Color(0xFF122B40)
    val WeekendEmpty = Color(0xFF352436)

    val AppGradient = Brush.verticalGradient(listOf(Color(0xFF071B2D), Color(0xFF061A2A), Color(0xFF020B13)))
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
