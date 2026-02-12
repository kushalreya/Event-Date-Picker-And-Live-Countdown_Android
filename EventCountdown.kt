package your.package.name

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventCountdown(
    eventDate: String,
    modifier: Modifier = Modifier,
    celebrationText: String = "🎉 Event is happening!"
) {

    var remainingMillis by remember { mutableStateOf<Long?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(eventDate) {

        showConfetti = false
        if (eventDate.isEmpty()) return@LaunchedEffect

        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        formatter.isLenient = false

        val parsed = formatter.parse(eventDate) ?: return@LaunchedEffect

        val calendar = Calendar.getInstance().apply {
            time = parsed
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val eventMillis = calendar.timeInMillis

        while (true) {
            val diff = eventMillis - System.currentTimeMillis()

            if (diff <= 0) {
                remainingMillis = 0
                showConfetti = true
                break
            }

            remainingMillis = diff
            delay(1000)
        }
    }

    remainingMillis?.let { millis ->

        val oneDay = 86_400_000L
        val oneHour = 3_600_000L

        val baseColor = when {
            millis == 0L -> MaterialTheme.colorScheme.primary
            millis <= oneHour -> MaterialTheme.colorScheme.error
            millis <= oneDay -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        val animatedColor by animateColorAsState(baseColor)

        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            if (millis == 0L) {

                val infinite = rememberInfiniteTransition()

                val scale by infinite.animateFloat(
                    1f, 1.12f,
                    infiniteRepeatable(
                        animation = tween(900),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                val textColor = if (isDark) Color(0xFFFFD54F) else animatedColor

                Text(
                    text = celebrationText,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold,
                        shadow = Shadow(
                            color = if (isDark) Color.Black else Color.LightGray,
                            blurRadius = 12f
                        )
                    )
                )

                if (showConfetti) {
                    ConfettiAnimation()
                }

            } else {

                val totalSeconds = millis / 1000
                val days = totalSeconds / 86400
                val hours = (totalSeconds % 86400) / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                Text(
                    text = "%02d d  %02d h  %02d m  %02d s"
                        .format(days, hours, minutes, seconds),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = animatedColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
