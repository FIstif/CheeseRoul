// ui/screens/SetupScreen.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pip.cheeseroul.model.DisplayMode
import com.pip.cheeseroul.model.TutorialStep
import com.pip.cheeseroul.ui.components.TutorialOverlay
import com.pip.cheeseroul.ui.theme.*
import com.pip.cheeseroul.viewmodel.RouletteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: RouletteViewModel,
    onStartGame: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val displayMode by viewModel.displayMode.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val players by viewModel.players.collectAsState()
    val tutorialStep by viewModel.tutorialStep.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("🧀 Колесо судьбы", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
                    },
                    actions = {
                        IconButton(onClick = onOpenHistory) {
                            Icon(Icons.Default.MenuBook, contentDescription = "История", tint = CheeseBrown)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CheeseBackground)
                )
            },
            containerColor = CheeseBackground
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Режим отображения:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CheeseBrown,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ModeSelector(selectedMode = displayMode, onModeSelected = { viewModel.setDisplayMode(it) })

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Количество игроков:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CheeseBrown,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CheeseCardBg)
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.setPlayerCount(playerCount - 1) },
                        enabled = playerCount > 2,
                        colors = ButtonDefaults.buttonColors(containerColor = CheeseOrange)
                    ) { Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

                    AnimatedContent(
                        targetState = playerCount,
                        label = "playerCountAnimation",
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) { count ->
                        Text(text = "$count", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
                    }

                    Button(
                        onClick = { viewModel.setPlayerCount(playerCount + 1) },
                        enabled = playerCount < 7,
                        colors = ButtonDefaults.buttonColors(containerColor = CheeseOrange)
                    ) { Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = displayMode == DisplayMode.COLOR_AND_NAME,
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(players, key = { _, p -> p.id }) { index, player ->
                            OutlinedTextField(
                                value = player.name,
                                onValueChange = { viewModel.updatePlayerName(player.id, it) },
                                label = { Text("Игрок ${index + 1}") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = player.color, unfocusedBorderColor = CheeseOrange,
                                    focusedContainerColor = CheeseSurface, unfocusedContainerColor = CheeseSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (displayMode != DisplayMode.COLOR_AND_NAME) Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CheeseYellow)
                ) {
                    Text(text = "ИГРАТЬ 🎲", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CheeseBrown)
                }
            }
        }

        if (tutorialStep == TutorialStep.SETUP_HINT) {
            TutorialOverlay(
                text = "Добро пожаловать в Колесо судьбы!\n\nВыберите количество игроков, настройте отображение и впишите имена. Как будете готовы — жмите «ИГРАТЬ»!",
                onNext = { viewModel.nextTutorialStep() },
                onSkip = { viewModel.skipTutorial() }
            )
        }
    }
}

@Composable
private fun ModeSelector(selectedMode: DisplayMode, onModeSelected: (DisplayMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        val modes = listOf(
            DisplayMode.COLOR_ONLY to "Только цвет",
            DisplayMode.COLOR_AND_NUMBER to "Цвет + номер",
            DisplayMode.COLOR_AND_NAME to "Цвет + имя"
        )
        modes.forEach { (mode, label) ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(text = label, modifier = Modifier.fillMaxWidth(), fontWeight = if (selectedMode == mode) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CheeseOrange, selectedLabelColor = Color.White,
                    containerColor = CheeseCardBg, labelColor = CheeseBrown
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}