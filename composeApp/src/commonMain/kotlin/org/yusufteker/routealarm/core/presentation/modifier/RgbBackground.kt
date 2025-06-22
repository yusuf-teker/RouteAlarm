package org.yusufteker.routealarm.core.presentation.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.yusufteker.routealarm.feature.location.domain.toRadians
import kotlin.math.cos
import kotlin.math.sin
val rainbowColors = listOf(
    Color.Red,
    Color(0xFFFF7F00), // Orange
    Color.Yellow,
    Color.Green,
    Color.Blue,
    Color(0xFF4B0082), // Indigo
    Color.Magenta,
    Color.Red
)
val activeAlarmBorderColors = listOf(
    Color(0xFF00FFAA), // canlı yeşil-mavi (neon yeşil)
    Color(0xFF00FFC8), // aqua
    Color(0xFF00FFD5), // açık mavi-cyan tonu
    Color(0xFF2EFFA3), // yumuşak yeşilimsi neon
    Color(0xFF3AFFA2), // biraz daha derin bir yeşil tonu
    Color(0xFF32CD32), // lime green, doğal ama parlak yeşil
    Color(0xFF2E7D32), // koyu yeşil (senin tanımınla uyumlu)
    Color(0xFF00FF00)  // tam RGB yeşil
)

fun Modifier.rgbBorder(
    strokeWidth: Dp,
    shape: Shape,
    colorList: List<Color> = rainbowColors,
    duration: Int = 3000
) = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepGradientAnimation"
    )

    this
        .drawBehind {
            drawAnimatedBorder(
                strokeWidth = strokeWidth,
                shape = shape,
                colorList = colorList,
                angle = angle
            )
        }
        .padding(strokeWidth)
        .clip(shape)
}

private fun DrawScope.drawAnimatedBorder(
    strokeWidth: Dp,
    shape: Shape,
    colorList: List<Color>,
    angle: Float
) {
    val strokeWidthPx = strokeWidth.toPx()

    // Create rotating gradient brush
    val brush = createRotatingBrush(colorList, angle, size.width / 2, size.height / 2)

    // Draw the border using the shape's outline
    val outline = shape.createOutline(size, layoutDirection, this)

    drawOutline(
        outline = outline,
        brush = brush,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = strokeWidthPx
        )
    )
}

private fun createRotatingBrush(
    colors: List<Color>,
    angle: Float,
    centerX: Float,
    centerY: Float
): Brush {
    // Convert angle to radians

    val angleRad = angle.toDouble().toRadians()

    // Calculate start and end points for linear gradient
    val radius = maxOf(centerX, centerY) * 1.5f
    val startX = centerX + cos(angleRad).toFloat() * radius
    val startY = centerY + sin(angleRad).toFloat() * radius
    val endX = centerX - cos(angleRad).toFloat() * radius
    val endY = centerY - sin(angleRad).toFloat() * radius

    return Brush.linearGradient(
        colors = colors,
        start = androidx.compose.ui.geometry.Offset(startX, startY),
        end = androidx.compose.ui.geometry.Offset(endX, endY)
    )
}

// Alternative implementation using sweep gradient (simpler but different effect)
fun Modifier.rgbBorderSweep(
    strokeWidth: Dp,
    shape: Shape,
    colorList: List<Color> = rainbowColors,
    duration: Int = 3000
) = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepGradientAnimation"
    )

    this
        .drawBehind {
            rotate(angle) {
                val outline = shape.createOutline(size, layoutDirection, this@drawBehind)
                drawOutline(
                    outline = outline,
                    brush = Brush.sweepGradient(colorList),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth.toPx()
                    )
                )
            }
        }
        .padding(strokeWidth)
        .clip(shape)
}
