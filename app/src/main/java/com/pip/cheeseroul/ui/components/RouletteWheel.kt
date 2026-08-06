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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import com.pip.cheeseroul.model.SpinAnimationMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun RouletteWheel(
    players: List<Player>, displayMode: DisplayMode, spinAnimationMode: SpinAnimationMode,
    currentAnimatedAngle: Float, highlightedIndex: Int,
    animatingPlayer: Player? = null, currentEffect: EliminationEffect = EliminationEffect.NONE,
    eliminationProgress: Float = 0f, onSectorLongClick: (Player) -> Unit,
    modifier: Modifier = Modifier, centerContent: @Composable () -> Unit
) {
    val baseRotation = if (spinAnimationMode == SpinAnimationMode.SPINNING_WHEEL) -currentAnimatedAngle + 180f else 0f
    val currentBaseRotation by rememberUpdatedState(baseRotation)

    Box(
        modifier = modifier.fillMaxWidth().aspectRatio(1f).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.matchParentSize().pointerInput(players.size) {
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
                            val touchAngle = (angle - currentBaseRotation + 90f + 3600f) % 360f
                            val sweepAngle = 360f / players.size
                            val index = (touchAngle / sweepAngle).toInt().coerceIn(0, players.size - 1)
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

            drawContext.canvas.save()
            drawContext.canvas.nativeCanvas.rotate(baseRotation, canvasCenter.x, canvasCenter.y)

            players.forEachIndexed { index, player ->
                val startAngle = index * sweepAngle - 90f - (sweepAngle / 2f)
                val isAnimating = player == animatingPlayer
                val medianAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())

                var alpha = 1f
                var scale = 1f
                var translateX = 0f
                var translateY = 0f
                var animRotation = 0f

                // НОВОЕ: Продвинутая математика эффектов выбывания
                if (isAnimating) {
                    when (currentEffect) {
                        EliminationEffect.EXPLOSION -> {
                            val phase1 = (eliminationProgress / 0.3f).coerceIn(0f, 1f)
                            val phase2 = ((eliminationProgress - 0.3f) / 0.7f).coerceIn(0f, 1f)

                            val shake = if (phase1 < 1f && phase1 > 0f) sin(phase1 * 40f) * 15f else 0f
                            scale = if (phase1 < 1f) 1f + (phase1 * 0.3f) else 1.3f - (phase2 * 0.3f)

                            if (phase2 > 0f) {
                                val flyDist = phase2 * radius * 5f // Резко улетает за экран
                                translateX = flyDist * cos(medianAngleRad).toFloat()
                                translateY = flyDist * sin(medianAngleRad).toFloat() + shake
                                alpha = 1f - (phase2 * phase2)
                            } else {
                                // Тряска перед взрывом
                                translateX = shake * cos(medianAngleRad + Math.PI/2).toFloat()
                                translateY = shake * sin(medianAngleRad + Math.PI/2).toFloat()
                                alpha = 1f
                            }
                        }
                        EliminationEffect.FLY_AWAY -> {
                            val targetX = canvasCenter.x * 1.5f
                            val targetY = -canvasCenter.y * 1.5f
                            val cpX = canvasCenter.x * 2.5f * cos(medianAngleRad).toFloat() // Контрольная точка для выноса
                            val cpY = canvasCenter.y * 2.5f * sin(medianAngleRad).toFloat()

                            val t = eliminationProgress
                            translateX = 2 * (1 - t) * t * cpX + t * t * targetX
                            translateY = 2 * (1 - t) * t * cpY + t * t * targetY

                            scale = 1f - (t * 0.8f)
                            animRotation = t * 1440f // 4 полных оборота
                            alpha = 1f - (t * t * t)
                        }
                        EliminationEffect.FADE -> {
                            translateY = -eliminationProgress * radius * 1.5f
                            scale = 1f + (eliminationProgress * 0.8f)
                            translateX = sin(eliminationProgress * 15f) * 25f // Призрачное покачивание
                            alpha = 1f - (eliminationProgress * eliminationProgress)
                        }
                        EliminationEffect.SHRINK -> {
                            scale = (1f - eliminationProgress).coerceAtLeast(0f)
                            animRotation = eliminationProgress * 2160f // 6 оборотов (черная дыра)
                            val suckDist = eliminationProgress * radius * 0.5f
                            translateX = -suckDist * cos(medianAngleRad).toFloat()
                            translateY = -suckDist * sin(medianAngleRad).toFloat()
                            alpha = 1f - (eliminationProgress * eliminationProgress)
                        }
                        else -> {}
                    }
                }

                drawContext.canvas.save()

                if (isAnimating) {
                    drawContext.canvas.translate(canvasCenter.x + translateX, canvasCenter.y + translateY)
                    drawContext.canvas.scale(scale, scale)
                    if (animRotation != 0f) drawContext.canvas.rotate(animRotation)
                    drawContext.canvas.translate(-canvasCenter.x, -canvasCenter.y)
                }

                drawArc(
                    color = player.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true
                )

                if (index == highlightedIndex && !isAnimating && spinAnimationMode == SpinAnimationMode.HIGHLIGHT) {
                    drawArc(color = Color.White.copy(alpha = 0.4f * alpha), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true)
                    drawArc(color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true, style = Stroke(width = 12.dp.toPx()))
                }

                drawArc(color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true, style = Stroke(width = 4.dp.toPx()))

                val textToDraw = when (displayMode) {
                    DisplayMode.COLOR_ONLY -> ""
                    DisplayMode.COLOR_AND_NUMBER -> "${player.id}"
                    DisplayMode.COLOR_AND_NAME -> player.name
                }

                if (textToDraw.isNotEmpty()) {
                    val textRadiusStart = radius * 0.35f
                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        translate(canvasCenter.x, canvasCenter.y)
                        rotate(Math.toDegrees(medianAngleRad).toFloat())

                        val paint = Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.alpha = (255 * alpha).toInt().coerceIn(0, 255)
                            this.textSize = if (displayMode == DisplayMode.COLOR_AND_NAME) 48f else 64f
                            this.isFakeBoldText = true
                            this.textAlign = Paint.Align.LEFT
                            setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK)
                        }

                        val bounds = Rect()
                        paint.getTextBounds(textToDraw, 0, textToDraw.length, bounds)
                        drawText(textToDraw, textRadiusStart, bounds.height() / 2f, paint)
                        restore()
                    }
                }
                drawContext.canvas.restore()
            }

            drawCircle(color = Color(0xFF4E342E), radius = radius, style = Stroke(width = 8.dp.toPx()))
            drawContext.canvas.restore()
        }
        centerContent()
    }
}