// model/GameModels.kt
package com.pip.cheeseroul.model

import androidx.compose.ui.graphics.Color

enum class DisplayMode {
    COLOR_ONLY,
    COLOR_AND_NUMBER,
    COLOR_AND_NAME
}

data class Player(
    val id: Int,
    val name: String = "",
    val color: Color,
    val isActive: Boolean = true
)

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

enum class EliminationEffect(val title: String) {
    EXPLOSION("Взрыв"),
    FADE("Угасание"),
    SHRINK("Сжатие"),
    FLY_AWAY("Улёт в корзину"), // НОВОЕ: Крутой эффект с вращением
    RANDOM("Случайно"),
    NONE("Без эффекта")
}

enum class SpinAnimationMode(val title: String) {
    HIGHLIGHT("Подсветка секторов"),
    SPINNING_ARROW("Крутится стрелка"),
    SPINNING_WHEEL("Крутится колесо")
}