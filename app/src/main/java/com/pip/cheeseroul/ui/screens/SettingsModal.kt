// ui/screens/SettingsModal.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pip.cheeseroul.model.DisplayMode
import com.pip.cheeseroul.model.EliminationEffect
import com.pip.cheeseroul.model.SpinAnimationMode
import com.pip.cheeseroul.ui.theme.*

@Composable
fun SettingsModal(
    isSoundEnabled: Boolean,
    isVibrationEnabled: Boolean,
    displayMode: DisplayMode,
    isFakeStopEnabled: Boolean,
    spinAnimationMode: SpinAnimationMode,
    eliminationEffect: EliminationEffect,

    // Новые параметры отладки
    fakeStopChance: Float,
    spinDuration: Float,
    fakeStopDuration: Float,
    fakeJumpDuration: Float,
    easingFactor: Float,
    fakeStopsCountDebug: Float,
    soundVolume: Float,
    vibrationStrength: Float,

    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onModeSelect: (DisplayMode) -> Unit,
    onFakeStopToggle: (Boolean) -> Unit,
    onSpinModeSelect: (SpinAnimationMode) -> Unit,
    onEffectSelect: (EliminationEffect) -> Unit,

    // Новые коллбэки отладки
    onChanceChange: (Float) -> Unit,
    onSpinDurationChange: (Float) -> Unit,
    onFakeStopDurationChange: (Float) -> Unit,
    onFakeJumpDurationChange: (Float) -> Unit,
    onEasingFactorChange: (Float) -> Unit,
    onFakeStopsCountDebugChange: (Float) -> Unit,
    onSoundVolumeChange: (Float) -> Unit,
    onVibrationStrengthChange: (Float) -> Unit,

    onExitToMenu: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f), // Сделали окно чуть больше, чтобы влезли настройки
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CheeseBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Заголовок и крестик
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Настройки", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = CheeseBrown)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Скроллируемая область для всех настроек
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- ОСНОВНЫЕ НАСТРОЙКИ ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Звук", fontSize = 18.sp, color = CheeseBrown)
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = onSoundToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = CheeseYellow, checkedTrackColor = CheeseOrange)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Вибрация", fontSize = 18.sp, color = CheeseBrown)
                        Switch(
                            checked = isVibrationEnabled,
                            onCheckedChange = onVibrationToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = CheeseYellow, checkedTrackColor = CheeseOrange)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ложная остановка", fontSize = 18.sp, color = CheeseBrown)
                        Switch(
                            checked = isFakeStopEnabled,
                            onCheckedChange = onFakeStopToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = CheeseYellow, checkedTrackColor = CheeseOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Что вращается?", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CheeseBrown)
                    SpinModeSelector(selectedMode = spinAnimationMode, onModeSelected = onSpinModeSelect)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Отображение секторов", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CheeseBrown)
                    ModeSelector(selectedMode = displayMode, onModeSelected = onModeSelect)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Эффект выбывания", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CheeseBrown)
                    EffectSelector(selectedEffect = eliminationEffect, onEffectSelected = onEffectSelect)

                    // --- БЛОК ОТЛАДКИ АНИМАЦИИ ---
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = CheeseOrange.copy(alpha = 0.5f), thickness = 2.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🛠 ОТЛАДКА АНИМАЦИИ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
                    Text("Настрой физику колеса под себя", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    AdjustableSlider(
                        label = "Длительность вращения (сек)", value = spinDuration,
                        range = 2f..10f, step = 0.5f,
                        onValueChange = onSpinDurationChange
                    )

                    AdjustableSlider(
                        label = "Пауза фейк-остановки (сек)", value = fakeStopDuration,
                        range = 0f..2f, step = 0.1f,
                        onValueChange = onFakeStopDurationChange
                    )

                    AdjustableSlider(
                        label = "Скорость прыжка (сек)", value = fakeJumpDuration,
                        range = 0.1f..1.5f, step = 0.1f,
                        onValueChange = onFakeJumpDurationChange
                    )

                    AdjustableSlider(
                        label = "Шанс фейк-остановки (%)", value = fakeStopChance * 100f,
                        range = 0f..100f, step = 10f,
                        onValueChange = { onChanceChange(it / 100f) },
                        displayFormat = { "${it.toInt()}%" }
                    )

                    AdjustableSlider(
                        label = "Плавность замедления (Easing)", value = easingFactor,
                        range = 0f..1f, step = 0.1f,
                        onValueChange = onEasingFactorChange
                    )

                    AdjustableSlider(
                        label = "Кол-во фейк-остановок", value = fakeStopsCountDebug,
                        range = 1f..3f, step = 1f,
                        onValueChange = onFakeStopsCountDebugChange,
                        displayFormat = { it.toInt().toString() }
                    )

                    AdjustableSlider(
                        label = "Громкость звука", value = soundVolume,
                        range = 0f..1f, step = 0.1f,
                        onValueChange = onSoundVolumeChange
                    )

                    AdjustableSlider(
                        label = "Сила вибрации", value = vibrationStrength,
                        range = 0f..1f, step = 0.1f,
                        onValueChange = onVibrationStrengthChange
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Кнопка выхода в меню
                Button(
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CheeseOrange)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выйти в меню", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// НОВЫЙ КАСТОМНЫЙ UI КОМПОНЕНТ ДЛЯ ПОЛЗУНКОВ
@Composable
fun AdjustableSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit,
    displayFormat: (Float) -> String = { String.format("%.1f", it) }
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, color = CheeseBrown, fontWeight = FontWeight.Medium)
            Text(displayFormat(value), fontSize = 14.sp, color = CheeseBrown, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка минус
            TextButton(
                onClick = {
                    val newValue = (value - step).coerceIn(range)
                    onValueChange(newValue)
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.width(40.dp)
            ) { Text("-", fontSize = 24.sp, color = CheeseOrange, fontWeight = FontWeight.Bold) }

            // Сам ползунок
            Slider(
                value = value,
                onValueChange = { onValueChange(it) },
                valueRange = range,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = CheeseYellow, activeTrackColor = CheeseOrange, inactiveTrackColor = CheeseCardBg)
            )

            // Кнопка плюс
            TextButton(
                onClick = {
                    val newValue = (value + step).coerceIn(range)
                    onValueChange(newValue)
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.width(40.dp)
            ) { Text("+", fontSize = 24.sp, color = CheeseOrange, fontWeight = FontWeight.Bold) }
        }
    }
}

// Вспомогательные селекторы
@Composable
private fun ModeSelector(selectedMode: DisplayMode, onModeSelected: (DisplayMode) -> Unit) {
    val modes = listOf(DisplayMode.COLOR_ONLY to "Только цвета", DisplayMode.COLOR_AND_NUMBER to "Цвета и номера", DisplayMode.COLOR_AND_NAME to "Цвета и имена")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        modes.forEach { (mode, label) ->
            FilterChip(
                selected = selectedMode == mode, onClick = { onModeSelected(mode) },
                label = { Text(text = label, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CheeseOrange, selectedLabelColor = Color.White, containerColor = CheeseCardBg, labelColor = CheeseBrown),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun SpinModeSelector(selectedMode: SpinAnimationMode, onModeSelected: (SpinAnimationMode) -> Unit) {
    val modes = listOf(SpinAnimationMode.SPINNING_ARROW to "Крутится сыр (Стрелка)", SpinAnimationMode.SPINNING_WHEEL to "Крутится колесо", SpinAnimationMode.HIGHLIGHT to "Бегущая подсветка")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        modes.forEach { (mode, label) ->
            FilterChip(
                selected = selectedMode == mode, onClick = { onModeSelected(mode) },
                label = { Text(text = label, modifier = Modifier.fillMaxWidth()) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CheeseOrange, selectedLabelColor = Color.White, containerColor = CheeseCardBg, labelColor = CheeseBrown),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EffectSelector(selectedEffect: EliminationEffect, onEffectSelected: (EliminationEffect) -> Unit) {
    val effects = listOf(EliminationEffect.RANDOM to "Рандом", EliminationEffect.EXPLOSION to "Взрыв", EliminationEffect.FADE to "Угасание", EliminationEffect.SHRINK to "Сжатие", EliminationEffect.FLY_AWAY to "Улет")
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            effects.take(3).forEach { (effect, label) ->
                FilterChip(
                    selected = selectedEffect == effect, onClick = { onEffectSelected(effect) },
                    label = { Text(text = label, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CheeseOrange, selectedLabelColor = Color.White, containerColor = CheeseCardBg, labelColor = CheeseBrown),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            effects.drop(3).forEach { (effect, label) ->
                FilterChip(
                    selected = selectedEffect == effect, onClick = { onEffectSelected(effect) },
                    label = { Text(text = label, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CheeseOrange, selectedLabelColor = Color.White, containerColor = CheeseCardBg, labelColor = CheeseBrown),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }
    }
}