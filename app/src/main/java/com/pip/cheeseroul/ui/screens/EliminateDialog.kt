// ui/screens/EliminateDialog.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pip.cheeseroul.model.Player
import com.pip.cheeseroul.ui.theme.CheeseBackground
import com.pip.cheeseroul.ui.theme.CheeseBrown
import com.pip.cheeseroul.ui.theme.CheeseOrange

@Composable
fun EliminateDialog(
    players: List<Player>,
    eliminatedPlayers: List<Player>, // Добавили список выбывших
    onPlayerEliminated: (Player) -> Unit,
    onPlayerRestored: (Player) -> Unit, // Колбэк на возврат
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CheeseBackground,
            modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f) // Чтобы списки помещались
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚩 Управление",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CheeseBrown
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {

                    // Блок активных игроков (можно выгнать)
                    if (players.isNotEmpty()) {
                        item {
                            Text("Активные игроки:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(players) { player ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onPlayerEliminated(player) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(player.color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (player.name.isNotBlank()) player.name else "Игрок ${player.id}",
                                    fontSize = 18.sp,
                                    color = CheeseBrown,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Блок выбывших игроков (можно вернуть)
                    if (eliminatedPlayers.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = CheeseBrown.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Выбывшие (нажмите, чтобы вернуть):", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(eliminatedPlayers) { player ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onPlayerRestored(player) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(player.color.copy(alpha = 0.5f)) // Слегка тусклый цвет
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (player.name.isNotBlank()) player.name else "Игрок ${player.id}",
                                    fontSize = 18.sp,
                                    color = CheeseBrown.copy(alpha = 0.6f),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Вернуть", tint = CheeseOrange)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Закрыть", color = CheeseBrown)
                }
            }
        }
    }
}