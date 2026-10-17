package hr.raspored.kalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import hr.raspored.kalendar.ui.RasporedApp
import hr.raspored.kalendar.ui.RasporedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RasporedTheme { RasporedApp() }
        }
    }
}
