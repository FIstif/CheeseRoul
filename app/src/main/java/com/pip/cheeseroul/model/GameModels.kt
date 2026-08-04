// GameModels.kt
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
    val isActive: Boolean = true // Флаг для логики кнопки "Выбыл"
)

// Состояния вращения колеса
enum class SpinState {
    IDLE,       // Ожидание нажатия
    SPINNING,   // Быстрое вращение / Замедление
    FAKE_STOP,  // Момент ложной остановки (перескок)
    STOPPED     // Результат показан на экране
}

enum class TutorialStep {
    SETUP_HINT,   // Подсказка на экране настроек
    SPIN_HINT,    // Подсказка как крутить рулетку
    DELETE_HINT,  // Подсказка как удалить игрока
    DONE          // Обучение пройдено
}