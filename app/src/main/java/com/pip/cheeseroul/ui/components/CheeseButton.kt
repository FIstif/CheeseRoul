// ui/components/CheeseButton.kt
package com.pip.cheeseroul.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pip.cheeseroul.ui.theme.CheeseBrown
import com.pip.cheeseroul.ui.theme.CheeseOrange
import com.pip.cheeseroul.ui.theme.CheeseYellow

@Composable
fun CheeseButton(
    isSpinning: Boolean,
    onNormalClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isSpinning) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
    )

    val currentScale by animateFloatAsState(targetValue = if (isPressed) 0.85f else pulseScale, animationSpec = tween(150), label = "press_scale")

    Box(
        modifier = modifier
            .size(110.dp)
            .scale(currentScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true; tryAwaitRelease(); isPressed = false },
                    onTap = { onNormalClick() }, onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = this.center
            val radius = size.minDimension / 2f

            val trianglePath = Path().apply {
                moveTo(center.x, center.y - radius * 0.9f)
                lineTo(center.x + radius * 0.8f, center.y + radius * 0.6f)
                lineTo(center.x - radius * 0.8f, center.y + radius * 0.6f)
                close()
            }

            drawPath(path = trianglePath, color = CheeseYellow)

            // Дырки в сыре для текстуры
            drawCircle(color = CheeseOrange, radius = radius * 0.16f, center = center.copy(x = center.x, y = center.y - radius * 0.35f))
            drawCircle(color = CheeseOrange, radius = radius * 0.12f, center = center.copy(x = center.x - radius * 0.35f, y = center.y + radius * 0.25f))
            drawCircle(color = CheeseOrange, radius = radius * 0.20f, center = center.copy(x = center.x + radius * 0.30f, y = center.y + radius * 0.20f))

            // НОВОЕ: Красный кончик (указатель)
            val tipPath = Path().apply {
                moveTo(center.x, center.y - radius * 0.9f) // Самая вершина
                lineTo(center.x + radius * 0.18f, center.y - radius * 0.65f) // Чуть ниже вправо
                lineTo(center.x - radius * 0.18f, center.y - radius * 0.65f) // Чуть ниже влево
                close()
            }
            drawPath(path = tipPath, color = Color(0xFFD32F2F))

            // Мягкая обводка
            drawPath(path = trianglePath, color = CheeseBrown, style = Stroke(width = 4.dp.toPx(), join = StrokeJoin.Round))
        }
    }
}