// ui/screens/EliminateDialog.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pip.cheeseroul.model.Player
import com.pip.cheeseroul.ui.theme.CheeseBackground
import com.pip.cheeseroul.ui.theme.CheeseBrown

@Composable
fun EliminateDialog(
    players: List<Player>,
    onPlayerEliminated: (Player) -> Unit,
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
                Text(
                    text = "🚩 Кто выбыл?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CheeseBrown
                )

                Spacer(modifier = Modifier.height(16.dp))

                players.forEach { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayerEliminated(player) }
                            .padding(vertical = 8.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = CheeseBrown)
                }
            }
        }
    }
}