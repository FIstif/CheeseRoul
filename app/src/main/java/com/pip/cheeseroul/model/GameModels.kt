// model/GameModels.kt
package com.pip.cheeseroul.model

import androidx.compose.ui.graphics.Color

// Режимы отображения
enum class DisplayMode {
    COLOR_ONLY,
    COLOR_AND_NUMBER,
    COLOR_AND_NAME
}

// Модель игрока
data class Player(
    val id: Int,
    val name: String = "",
    val color: Color,
    val isActive: Boolean = true
)

// Состояния вращения колеса
enum class SpinState {
    IDLE,
    SPINNING,
    FAKE_STOP,
    STOPPED
}

enum class TutorialStep {
    SETUP_HINT,
    SPIN_HINT,
    DELETE_HINT,
    DONE
}

// НОВОЕ: Эффекты выбывания
enum class EliminationEffect(val title: String) {
    EXPLOSION("Взрыв"),
    FADE("Угасание"),
    SHRINK("Сжатие"),
    RANDOM("Случайно"),
    NONE("Без эффекта")
}