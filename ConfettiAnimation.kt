package your.package.name

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import kotlin.math.sin

@Composable
fun ConfettiAnimation() {

    val infinite = rememberInfiniteTransition()

    val dropProgress by infinite.animateFloat(
        -0.2f, 1.2f,
        infiniteRepeatable(
            animation = tween(3500),
            repeatMode = RepeatMode.Restart
        )
    )

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFFFC107),
        Color(0xFFE91E63),
        Color(0xFF4CAF50),
        Color(0xFF2196F3)
    )

    val particles = remember {
        List(120) {
            Triple(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextInt(colors.size)
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {

        val width = size.width
        val height = size.height

        particles.forEach { (xFactor, depth, colorIndex) ->

            val radius = 6f + depth * 14f
            val drift = sin(dropProgress * 10f + depth * 5f) * 30f

            drawCircle(
                color = colors[colorIndex],
                radius = radius,
                center = Offset(
                    x = xFactor * width + drift,
                    y = dropProgress * height + (depth * -600f)
                )
            )
        }
    }
}
