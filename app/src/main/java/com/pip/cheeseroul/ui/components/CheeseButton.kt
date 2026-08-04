// ui/components/CheeseButton.kt
package com.pip.cheeseroul.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pip.cheeseroul.ui.theme.CheeseOrange
import com.pip.cheeseroul.ui.theme.CheeseYellow

@Composable
fun CheeseButton(
    isSpinning: Boolean,
    onNormalClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Состояние "вдавливания" пальцем
    var isPressed by remember { mutableStateOf(false) }

    // Анимация пульсации и вдавливания
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpinning) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Приоритет: если нажато - вдавливаем сильно, иначе применяем пульсацию
    val currentScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else pulseScale,
        animationSpec = tween(150),
        label = "press_scale"
    )

    Box(
        modifier = modifier
            .size(90.dp)
            .scale(currentScale)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease() // Ждём, пока отпустят палец
                        isPressed = false
                    },
                    onTap = { onNormalClick() },
                    onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = this.center
            val radius = size.minDimension / 2f

            drawCircle(color = CheeseYellow, radius = radius)
            drawCircle(color = CheeseOrange, radius = radius * 0.88f)

            drawCircle(color = CheeseYellow, radius = radius * 0.18f, center = center.copy(x = center.x - radius * 0.35f, y = center.y - radius * 0.2f))
            drawCircle(color = CheeseYellow, radius = radius * 0.25f, center = center.copy(x = center.x + radius * 0.3f, y = center.y + radius * 0.25f))
            drawCircle(color = CheeseYellow, radius = radius * 0.15f, center = center.copy(x = center.x + radius * 0.1f, y = center.y - radius * 0.4f))
        }
    }
}