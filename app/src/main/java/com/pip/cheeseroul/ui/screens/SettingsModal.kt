// ui/screens/SettingsModal.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    isCheeseBackgroundEnabled: Boolean,
    spinAnimationMode: SpinAnimationMode,
    eliminationEffect: EliminationEffect,

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
    onCheeseBackgroundToggle: (Boolean) -> Unit,
    onSpinModeSelect: (SpinAnimationMode) -> Unit,
    onEffectSelect: (EliminationEffect) -> Unit,
    onResetTutorial: () -> Unit,

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
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
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

                // Скроллируемая область
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- ОСНОВНЫЕ НАСТРОЙКИ (Тумблеры) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Звук", fontSize = 18.sp, color = CheeseBrown)
                        Switch(
                            checked = isSoundEnabled, onCheckedChange = onSoundToggle,
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
                            checked = isVibrationEnabled, onCheckedChange = onVibrationToggle,
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
                            checked = isFakeStopEnabled, onCheckedChange = onFakeStopToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = CheeseYellow, checkedTrackColor = CheeseOrange)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Падающий сыр (Фон)", fontSize = 18.sp, color = CheeseBrown)
                        Switch(
                            checked = isCheeseBackgroundEnabled, onCheckedChange = onCheeseBackgroundToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = CheeseYellow, checkedTrackColor = CheeseOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- КОМПАКТНЫЕ ВЫПАДАЮЩИЕ СПИСКИ ---
                    DropdownSelector(
                        label = "Что вращается?",
                        options = listOf(SpinAnimationMode.SPINNING_ARROW to "Стрелка", SpinAnimationMode.SPINNING_WHEEL to "Колесо", SpinAnimationMode.HIGHLIGHT to "Подсветка"),
                        selectedOption = spinAnimationMode,
                        onOptionSelected = onSpinModeSelect
                    )

                    DropdownSelector(
                        label = "Отображение секторов",
                        options = listOf(DisplayMode.COLOR_ONLY to "Только цвета", DisplayMode.COLOR_AND_NUMBER to "Цвета и номера", DisplayMode.COLOR_AND_NAME to "Цвета и имена"),
                        selectedOption = displayMode,
                        onOptionSelected = onModeSelect
                    )

                    DropdownSelector(
                        label = "Эффект выбывания",
                        options = listOf(EliminationEffect.RANDOM to "Рандом", EliminationEffect.EXPLOSION to "Взрыв", EliminationEffect.FADE to "Угасание", EliminationEffect.SHRINK to "Сжатие", EliminationEffect.FLY_AWAY to "Улет"),
                        selectedOption = eliminationEffect,
                        onOptionSelected = onEffectSelect
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопка сброса подсказок
                    OutlinedButton(
                        onClick = onResetTutorial,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CheeseBrown),
                        border = BorderStroke(1.dp, CheeseOrange)
                    ) {
                        Text("Сбросить обучение (Подсказки)")
                    }

                    // --- БЛОК ОТЛАДКИ АНИМАЦИИ ---
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = CheeseOrange.copy(alpha = 0.5f), thickness = 2.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🛠 ОТЛАДКА АНИМАЦИИ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
                    Text("Настрой физику колеса под себя", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    AdjustableSlider(label = "Длительность вращения (сек)", value = spinDuration, range = 2f..10f, step = 0.5f, onValueChange = onSpinDurationChange)
                    AdjustableSlider(label = "Пауза фейк-остановки (сек)", value = fakeStopDuration, range = 0f..2f, step = 0.1f, onValueChange = onFakeStopDurationChange)
                    AdjustableSlider(label = "Скорость прыжка (сек)", value = fakeJumpDuration, range = 0.1f..1.5f, step = 0.1f, onValueChange = onFakeJumpDurationChange)
                    AdjustableSlider(label = "Шанс фейк-остановки (%)", value = fakeStopChance * 100f, range = 0f..100f, step = 10f, onValueChange = { onChanceChange(it / 100f) }, displayFormat = { "${it.toInt()}%" })
                    AdjustableSlider(label = "Плавность замедления (Easing)", value = easingFactor, range = 0f..1f, step = 0.1f, onValueChange = onEasingFactorChange)
                    AdjustableSlider(label = "Кол-во фейк-остановок", value = fakeStopsCountDebug, range = 1f..3f, step = 1f, onValueChange = onFakeStopsCountDebugChange, displayFormat = { it.toInt().toString() })
                    AdjustableSlider(label = "Громкость звука", value = soundVolume, range = 0f..1f, step = 0.1f, onValueChange = onSoundVolumeChange)
                    AdjustableSlider(label = "Сила вибрации", value = vibrationStrength, range = 0f..1f, step = 0.1f, onValueChange = onVibrationStrengthChange)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Две кнопки внизу (В меню / Закрыть)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onExitToMenu,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("В меню", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CheeseOrange)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Закрыть", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// НОВЫЙ КАСТОМНЫЙ КОМПОНЕНТ: ВЫПАДАЮЩИЙ СПИСОК
@Composable
fun <T> DropdownSelector(
    label: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.find { it.first == selectedOption }?.second ?: ""

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CheeseBrown)
        Box {
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = CheeseCardBg, contentColor = CheeseBrown),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(selectedText)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CheeseSurface)
            ) {
                options.forEach { (value, text) ->
                    DropdownMenuItem(
                        text = { Text(text, color = CheeseBrown) },
                        onClick = {
                            onOptionSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Кастомный компонент ползунков
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
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { onValueChange((value - step).coerceIn(range)) },
                contentPadding = PaddingValues(0.dp), modifier = Modifier.width(40.dp)
            ) { Text("-", fontSize = 24.sp, color = CheeseOrange, fontWeight = FontWeight.Bold) }

            Slider(
                value = value, onValueChange = { onValueChange(it) }, valueRange = range, modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = CheeseYellow, activeTrackColor = CheeseOrange, inactiveTrackColor = CheeseCardBg)
            )

            TextButton(
                onClick = { onValueChange((value + step).coerceIn(range)) },
                contentPadding = PaddingValues(0.dp), modifier = Modifier.width(40.dp)
            ) { Text("+", fontSize = 24.sp, color = CheeseOrange, fontWeight = FontWeight.Bold) }
        }
    }
}