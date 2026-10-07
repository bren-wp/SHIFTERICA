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
internal fun AppMark(modifier: Modifier = Modifier.size(46.dp)) {
    BoxWithConstraints(modifier = modifier) {
        val unit = maxWidth
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = RasporedColors.Card2,
            shape = RoundedCornerShape(unit * .32f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RasporedColors.Accent),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(unit * .065f)
                ) {
                    Box(
                        Modifier
                            .size(width = unit * .54f, height = unit * .15f)
                            .clip(RoundedCornerShape(unit * .07f))
                            .background(RasporedColors.Accent)
                    )
                    repeat(2) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(unit * .065f)) {
                            repeat(3) { column ->
                                val tint = if (row == 0 && column == 1) RasporedColors.Day else RasporedColors.Accent
                                Box(
                                    Modifier
                                        .size(unit * .13f)
                                        .clip(RoundedCornerShape(unit * .04f))
                                        .background(tint)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

