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
    eventDate: String,   // Event date in "dd-MM-yyyy" format
    isDark: Boolean      // Theme flag for styling adjustments
) {

    /* -------------------------------------------------------
       State
       ------------------------------------------------------- */

    // Holds remaining time in milliseconds.
    // Null means countdown hasn't initialized yet.
    var remainingMillis by remember { mutableStateOf<Long?>(null) }

    // Controls confetti visibility on event day.
    var showConfetti by remember { mutableStateOf(false) }

    /* -------------------------------------------------------
       Countdown Calculation Side-Effect
       ------------------------------------------------------- */

    // Re-runs whenever eventDate changes.
    // This safely cancels previous coroutine if date updates.
    LaunchedEffect(eventDate) {

        if (eventDate.isEmpty()) return@LaunchedEffect

        // Strict date parsing (prevents invalid rollover like 32-01-2025)
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        formatter.isLenient = false

        val parsedDate = try {
            formatter.parse(eventDate)
        } catch (e: Exception) {
            null
        } ?: return@LaunchedEffect

        // Normalize today's date to midnight
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Normalize event date to midnight
        val eventCalendar = Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val eventMillis = eventCalendar.timeInMillis
        val todayMillis = todayCalendar.timeInMillis

        // 🎉 If today equals event day, trigger celebration immediately
        if (todayMillis == eventMillis) {
            remainingMillis = 0
            showConfetti = true
            return@LaunchedEffect
        }

        /* -------------------------------------------------------
           Countdown Loop
           ------------------------------------------------------- */

        // Runs until composable leaves composition (isActive ensures safety)
        while (isActive) {

            val diff = eventMillis - System.currentTimeMillis()

            if (diff <= 0) {
                remainingMillis = 0
                break
            }

            // Prevent negative values due to scheduling jitter
            remainingMillis = diff.coerceAtLeast(0)

            // Align updates with real-world second boundaries
            // This avoids drift from naive delay(1000)
            val nextTick = 1000 - (System.currentTimeMillis() % 1000)
            delay(nextTick)
        }
    }

    /* -------------------------------------------------------
       Confetti Auto-Stop
       ------------------------------------------------------- */

    // Separate side effect tied only to showConfetti state.
    // Keeps logic clean and avoids mixing responsibilities.
    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(8000)          // Confetti duration (8 seconds)
            showConfetti = false
        }
    }

    /* -------------------------------------------------------
       UI Rendering
       ------------------------------------------------------- */

    remainingMillis?.let { millis ->

        // Convert milliseconds into human-readable components
        val totalSeconds = millis / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val oneHourMillis = 60 * 60 * 1000L
        val oneDayMillis = 24 * oneHourMillis

        val isEventHappening = millis <= 0
        val isLastHour = millis in 1..oneHourMillis
        val isLastDay = millis in (oneHourMillis + 1)..oneDayMillis

        // Dynamic color logic for urgency feedback
        val countdownColor = when {
            isEventHappening -> Color(0xFF4CAF50)    // Celebration green
            isLastHour -> Color(0xFFE53935)          // High urgency red
            isLastDay -> if (isDark)
                Color(0xFF621E09)
            else
                Color(0xFFFF3C00)                   // Orange warning
            else -> if (isDark)
                Color.White
            else
                Color.Black
        }

        /* -------------------------------------------------------
           Urgency Pulse Animation
           ------------------------------------------------------- */

        // Only animate scale during last hour
        val scale = if (isLastHour) {
            val transition = rememberInfiniteTransition(label = "pulse")

            transition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAnim"
            ).value
        } else 1f

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (isEventHappening) {

                // Celebration layout container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {

                    // Confetti renders behind text
                    if (showConfetti) {
                        ConfettiAnimation(
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    Text(
                        "🎉 It's Event Day!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = countdownColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {

                // Standard countdown display
                Text(
                    "$days d  $hours h  $minutes m  $seconds s",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color =
                        if (isDark)
                            countdownColor.copy(alpha = 0.5f)
                        else
                            countdownColor.copy(alpha = 0.6f),
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
            }
        }
    }
}
