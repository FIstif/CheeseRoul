// ui/screens/HistoryScreen.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pip.cheeseroul.model.GameStat
import com.pip.cheeseroul.ui.theme.CheeseBackground
import com.pip.cheeseroul.ui.theme.CheeseBrown
import com.pip.cheeseroul.ui.theme.CheeseCardBg
import com.pip.cheeseroul.ui.theme.CheeseOrange
import com.pip.cheeseroul.viewmodel.RouletteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: RouletteViewModel,
    onBack: () -> Unit
) {
    val history by viewModel.gameHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История игр", fontWeight = FontWeight.Bold, color = CheeseBrown) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = CheeseBrown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CheeseBackground)
            )
        },
        containerColor = CheeseBackground
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Вы еще не сыграли ни одной партии 🧀", color = CheeseBrown, fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { stat ->
                    GameStatCard(stat)
                }
            }
        }
    }
}

@Composable
fun GameStatCard(stat: GameStat) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(stat.dateMs))

    val minutes = TimeUnit.MILLISECONDS.toMinutes(stat.durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(stat.durationMs) % 60
    val durationString = String.format("%02d:%02d", minutes, seconds)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CheeseCardBg),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок карточки (Дата и Время)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateString, color = CheeseBrown, fontWeight = FontWeight.Bold)
                Text("⏱ $durationString", color = CheeseBrown)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Победитель
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏆 Победитель:", fontWeight = FontWeight.SemiBold, color = CheeseBrown)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(stat.winner.color))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stat.winner.name.ifBlank { "Игрок ${stat.winner.id}" }, fontWeight = FontWeight.Bold, color = CheeseOrange)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Первый выбывший
            stat.firstEliminated?.let { first ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💀 Вылетел первым:", fontSize = 14.sp, color = CheeseBrown)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(first.color))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(first.name.ifBlank { "Игрок ${first.id}" }, fontSize = 14.sp, color = CheeseBrown)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CheeseBrown.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Статистика рулетки
            Text("📊 Выпадения рулетки:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
            val maxSpins = stat.spinCounts.values.maxOrNull() ?: 1
            stat.spinCounts.forEach { (playerId, count) ->
                // Находим игрока, чтобы взять его цвет
                val playerName = if (playerId == stat.winner.id) stat.winner.name else "Игрок $playerId"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(playerName.ifBlank { "Игрок $playerId" }.take(10), fontSize = 12.sp, color = CheeseBrown, modifier = Modifier.width(70.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(CheeseBrown.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (count == 0) 0.05f else count.toFloat() / maxSpins.toFloat())
                                .fillMaxHeight()
                                .background(CheeseOrange)
                        )
                    }
                    Text(" $count", fontSize = 12.sp, color = CheeseBrown, modifier = Modifier.width(24.dp))
                }
            }
        }
    }
}