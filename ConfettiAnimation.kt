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

data class Particle(
    val xFactor: Float,
    val depth: Float,
    val colorIndex: Int,
    val offset: Float
)

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier
) {

    /* -------------------------------------------------------
       Infinite Animations
       ------------------------------------------------------- */

    // Drives all repeating animations inside this composable.
    val infinite = rememberInfiniteTransition(label = "confetti")

    // Controls vertical falling progress.
    // Starts slightly above screen (-0.2f) and ends below screen (1.2f)
    // so particles fully enter and exit the viewport.
    val dropProgress by infinite.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiDrop"
    )

    // Rotational spin applied to each particle.
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    /* -------------------------------------------------------
       Color Palette
       ------------------------------------------------------- */

    // Mix of theme-based colors and fixed celebratory colors.
    val themeColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFFFC107),
        Color(0xFFE91E63),
        Color(0xFF4CAF50),
        Color(0xFF2196F3)
    )

    /* -------------------------------------------------------
       Particle Generation (Stable Across Recompositions)
       ------------------------------------------------------- */

    // Generated once per composition.
    // Using remember prevents particles from regenerating every frame.
    val particles = remember {
        List(100) {
            Particle(
                xFactor = Random.nextFloat(),                 // Horizontal placement
                depth = Random.nextFloat(),                   // Motion & size variance
                colorIndex = Random.nextInt(themeColors.size),
                offset = Random.nextFloat()                   // Vertical stagger
            )
        }
    }

    /* -------------------------------------------------------
       Rendering Layer
       ------------------------------------------------------- */

    Canvas(modifier = modifier) {

        val width = size.width
        val height = size.height

        particles.forEach { particle ->

            // Larger depth → larger particle size (fake perspective)
            val sizeFactor = 6f + (particle.depth * 12f)

            // Adds side-to-side floating motion using sine wave.
            // Depth influences drift intensity.
            val horizontalDrift =
                kotlin.math.sin(dropProgress * 10f + particle.depth * 5f) *
                        (20f + particle.depth * 40f)

            // Calculates vertical position.
            // offset staggers particles so they don't all start at top simultaneously.
            val yPos =
                ((dropProgress + particle.offset) % 1.2f) * height +
                        (particle.depth * -600f)

            // Rotate each particle individually around its own center.
            rotate(
                degrees = rotation + particle.depth * 180f,
                pivot = Offset(
                    x = particle.xFactor * width + horizontalDrift,
                    y = yPos
                )
            ) {
                // Draw rectangle-shaped confetti piece.
                drawRect(
                    color = themeColors[particle.colorIndex],
                    topLeft = Offset(
                        x = particle.xFactor * width + horizontalDrift,
                        y = yPos
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = sizeFactor,
                        height = sizeFactor * 1.5f   // Slightly taller than wide
                    )
                )
            }
        }
    }
}
