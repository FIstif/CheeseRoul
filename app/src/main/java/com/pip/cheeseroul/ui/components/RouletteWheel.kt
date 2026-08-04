// ui/components/RouletteWheel.kt
package com.pip.cheeseroul.ui.components

import kotlin.math.min
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pip.cheeseroul.model.DisplayMode
import com.pip.cheeseroul.model.Player
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun RouletteWheel(
    players: List<Player>,
    displayMode: DisplayMode,
    highlightedIndex: Int,
    onSectorLongClick: (Player) -> Unit, // Новый колбек для долгого нажатия
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                // Добавляем обработчик нажатий прямо на Canvas
                .pointerInput(players.size) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            if (players.isEmpty()) return@detectTapGestures

                            val center = androidx.compose.ui.geometry.Offset(
                                size.width / 2f,
                                size.height / 2f
                            )
                            val radius = min(size.width, size.height) / 2f
                            val buttonRadius = 45.dp.toPx() // Исключаем зону центральной кнопки

                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val distance = sqrt(dx * dx + dy * dy)

                            // Проверяем, что нажатие было на секторах, а не на кнопке или за пределами колеса
                            if (distance in buttonRadius..radius) {
                                // Вычисляем угол нажатия (в градусах)
                                val angle =
                                    Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                // Приводим к нашей системе координат (0 градусов = 12 часов)
                                val normalizedAngle = (angle + 90f + 360f) % 360f
                                val sweepAngle = 360f / players.size

                                // Вычисляем индекс игрока
                                val index = (normalizedAngle / sweepAngle).toInt()
                                    .coerceIn(0, players.size - 1)
                                onSectorLongClick(players[index])
                            }
                        }
                    )
                }
        ) {
            if (players.isEmpty()) return@Canvas

            val count = players.size
            val sweepAngle = 360f / count
            val radius = size.minDimension / 2f
            val center = this.center

            players.forEachIndexed { index, player ->
                val startAngle = index * sweepAngle - 90f

                drawArc(
                    color = player.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                if (index == highlightedIndex) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.4f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = Stroke(width = 12.dp.toPx())
                    )
                }

                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    style = Stroke(width = 4.dp.toPx())
                )

                val textToDraw = when (displayMode) {
                    DisplayMode.COLOR_ONLY -> ""
                    DisplayMode.COLOR_AND_NUMBER -> "${player.id}"
                    DisplayMode.COLOR_AND_NAME -> player.name
                }

                if (textToDraw.isNotEmpty()) {
                    val medianAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                    val textRadius = radius * 0.68f
                    val textX = (center.x + textRadius * cos(medianAngleRad)).toFloat()
                    val textY = (center.y + textRadius * sin(medianAngleRad)).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = if (displayMode == DisplayMode.COLOR_AND_NAME) 36f else 54f
                            isFakeBoldText = true
                            textAlign = Paint.Align.CENTER
                            setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK)
                        }
                        val bounds = Rect()
                        paint.getTextBounds(textToDraw, 0, textToDraw.length, bounds)
                        drawText(textToDraw, textX, textY + bounds.height() / 2f, paint)
                    }
                }
            }

            drawCircle(
                color = Color(0xFF4E342E),
                radius = radius,
                style = Stroke(width = 8.dp.toPx())
            )
        }

        centerContent()
    }
}