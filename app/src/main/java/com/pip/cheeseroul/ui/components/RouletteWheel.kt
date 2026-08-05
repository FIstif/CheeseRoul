// ui/components/RouletteWheel.kt
package com.pip.cheeseroul.ui.components

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
import com.pip.cheeseroul.model.EliminationEffect
import com.pip.cheeseroul.model.Player
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun RouletteWheel(
    players: List<Player>,
    displayMode: DisplayMode,
    highlightedIndex: Int,
    animatingPlayer: Player? = null, // НОВОЕ: Игрок, которого мы прямо сейчас удаляем
    currentEffect: EliminationEffect = EliminationEffect.NONE, // НОВОЕ: Текущий эффект
    eliminationProgress: Float = 0f, // НОВОЕ: Прогресс анимации (от 0f до 1f)
    onSectorLongClick: (Player) -> Unit,
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
                .pointerInput(players.size) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            if (players.isEmpty()) return@detectTapGestures

                            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                            val radius = min(size.width, size.height) / 2f
                            val buttonRadius = 45.dp.toPx()

                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val distance = sqrt(dx * dx + dy * dy)

                            if (distance in buttonRadius..radius) {
                                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                val normalizedAngle = (angle + 90f + 360f) % 360f
                                val sweepAngle = 360f / players.size

                                val index = (normalizedAngle / sweepAngle).toInt().coerceIn(0, players.size - 1)
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
            val canvasCenter = this.center

            players.forEachIndexed { index, player ->
                val startAngle = index * sweepAngle - 90f
                val isAnimating = player == animatingPlayer
                val medianAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())

                var alpha = 1f
                var scale = 1f
                var translateX = 0f
                var translateY = 0f

                // Применяем математику эффекта, если этот сектор удаляется
                if (isAnimating) {
                    when (currentEffect) {
                        EliminationEffect.EXPLOSION -> {
                            val flyDistance = eliminationProgress * radius * 0.6f
                            translateX = flyDistance * cos(medianAngleRad).toFloat()
                            translateY = flyDistance * sin(medianAngleRad).toFloat()
                            scale = 1f + (eliminationProgress * 1.5f)
                            alpha = 1f - (eliminationProgress * eliminationProgress)
                        }
                        EliminationEffect.FADE -> {
                            alpha = 1f - eliminationProgress
                        }
                        EliminationEffect.SHRINK -> {
                            scale = 1f - eliminationProgress
                        }
                        else -> {}
                    }
                }

                drawContext.canvas.save()

                // Сдвигаем, увеличиваем и поворачиваем сам холст для анимации сектора
                if (isAnimating) {
                    drawContext.canvas.translate(canvasCenter.x + translateX, canvasCenter.y + translateY)
                    drawContext.canvas.scale(scale, scale)
                    drawContext.canvas.translate(-canvasCenter.x, -canvasCenter.y)
                }

                drawArc(
                    color = player.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                if (index == highlightedIndex && !isAnimating) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.4f * alpha),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    drawArc(
                        color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = Stroke(width = 12.dp.toPx())
                    )
                }

                drawArc(
                    color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)),
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
                    val textRadius = radius * 0.68f
                    val textX = (canvasCenter.x + textRadius * cos(medianAngleRad)).toFloat()
                    val textY = (canvasCenter.y + textRadius * sin(medianAngleRad)).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.alpha = (255 * alpha).toInt().coerceIn(0, 255) // Прозрачность текста
                            this.textSize = if (displayMode == DisplayMode.COLOR_AND_NAME) 36f else 54f
                            this.isFakeBoldText = true
                            this.textAlign = Paint.Align.CENTER
                            setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK)
                        }
                        val bounds = Rect()
                        paint.getTextBounds(textToDraw, 0, textToDraw.length, bounds)
                        drawText(textToDraw, textX, textY + bounds.height() / 2f, paint)
                    }
                }

                drawContext.canvas.restore()
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