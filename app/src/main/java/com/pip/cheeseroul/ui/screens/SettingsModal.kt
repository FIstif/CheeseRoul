// ui/screens/SettingsModal.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pip.cheeseroul.model.DisplayMode
import com.pip.cheeseroul.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SettingsModal(
    isSoundEnabled: Boolean,
    isVibrationEnabled: Boolean,
    displayMode: DisplayMode,
    isFakeStopEnabled: Boolean,
    fakeStopChance: Float,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onFakeStopToggle: (Boolean) -> Unit,
    onChanceChange: (Float) -> Unit,
    onModeSelect: (DisplayMode) -> Unit,
    onExitToMenu: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CheeseBackground,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚙️ Настройки", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
                Spacer(modifier = Modifier.height(16.dp))

                // Звук и Вибрация
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Звук", fontSize = 18.sp, color = CheeseBrown)
                    Switch(checked = isSoundEnabled, onCheckedChange = onSoundToggle, colors = SwitchDefaults.colors(checkedThumbColor = CheeseOrange))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Вибрация", fontSize = 18.sp, color = CheeseBrown)
                    Switch(checked = isVibrationEnabled, onCheckedChange = onVibrationToggle, colors = SwitchDefaults.colors(checkedThumbColor = CheeseOrange))
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CheeseCardBg)
                Spacer(modifier = Modifier.height(8.dp))

                // Ложная остановка
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ложная остановка", fontSize = 18.sp, color = CheeseBrown, fontWeight = FontWeight.Medium)
                    Switch(checked = isFakeStopEnabled, onCheckedChange = onFakeStopToggle, colors = SwitchDefaults.colors(checkedThumbColor = CheeseOrange))
                }

                // Слайдер частоты ложной остановки (показывается только если включено)
                if (isFakeStopEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Шанс срабатывания: ${(fakeStopChance * 100).roundToInt()}%", fontSize = 14.sp, color = CheeseBrown)
                    Slider(
                        value = fakeStopChance,
                        onValueChange = onChanceChange,
                        valueRange = 0.1f..1f, // От 10% до 100%
                        colors = SliderDefaults.colors(thumbColor = CheeseOrange, activeTrackColor = CheeseYellow)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CheeseCardBg)
                Spacer(modifier = Modifier.height(8.dp))

                // Режим
                Text("Режим отображения:", fontWeight = FontWeight.SemiBold, color = CheeseBrown)
                DisplayMode.entries.forEach { mode ->
                    val title = when (mode) {
                        DisplayMode.COLOR_ONLY -> "Только цвет"
                        DisplayMode.COLOR_AND_NUMBER -> "Цвет + номер"
                        DisplayMode.COLOR_AND_NAME -> "Цвет + имя"
                    }
                    TextButton(onClick = { onModeSelect(mode) }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = title, fontWeight = if (mode == displayMode) FontWeight.Bold else FontWeight.Normal, color = if (mode == displayMode) CheeseOrange else CheeseBrown)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onExitToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = CheeseOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выйти в меню")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Закрыть", color = CheeseBrown)
                }

                // --- НОВОЕ: Указание авторства для лицензии CC BY 4.0 ---
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Иконка приложения: Lima Studio (Icon-Icons.com) по лицензии CC BY 4.0",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}