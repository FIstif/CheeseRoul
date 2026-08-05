// ui/screens/GameScreen.kt
package com.pip.cheeseroul.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pip.cheeseroul.model.Player
import com.pip.cheeseroul.model.SpinState
import com.pip.cheeseroul.model.TutorialStep
import com.pip.cheeseroul.ui.components.CheeseButton
import com.pip.cheeseroul.ui.components.ResultBanner
import com.pip.cheeseroul.ui.components.RouletteWheel
import com.pip.cheeseroul.ui.components.TutorialOverlay
import com.pip.cheeseroul.ui.theme.CheeseBackground
import com.pip.cheeseroul.ui.theme.CheeseBrown
import com.pip.cheeseroul.utils.SoundManager
import com.pip.cheeseroul.utils.VibrationManager
import com.pip.cheeseroul.viewmodel.RouletteViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: RouletteViewModel,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val players by viewModel.players.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val isFakeStopEnabled by viewModel.isFakeStopEnabled.collectAsState()
    val fakeStopChance by viewModel.fakeStopChance.collectAsState()
    val tutorialStep by viewModel.tutorialStep.collectAsState()

    var highlightedIndex by remember { mutableIntStateOf(0) }
    var spinState by remember { mutableStateOf(SpinState.IDLE) }
    var showSettings by remember { mutableStateOf(false) }
    var showEliminateDialog by remember { mutableStateOf(false) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }

    val soundManager = remember { SoundManager(context) }
    val vibrationManager = remember { VibrationManager(context) }

    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    DisposableEffect(playerToDelete) {
        if (playerToDelete != null) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            val listener = object : SensorEventListener {
                private var lastShakeTime = 0L
                override fun onSensorChanged(event: SensorEvent) {
                    val gForce = sqrt(
                        Math.pow((event.values[0] / SensorManager.GRAVITY_EARTH).toDouble(), 2.0) +
                                Math.pow((event.values[1] / SensorManager.GRAVITY_EARTH).toDouble(), 2.0) +
                                Math.pow((event.values[2] / SensorManager.GRAVITY_EARTH).toDouble(), 2.0)
                    ).toFloat()

                    if (gForce > 2.5f) {
                        val now = System.currentTimeMillis()
                        if (now - lastShakeTime > 1000) {
                            lastShakeTime = now
                            viewModel.removePlayer(playerToDelete!!)
                            highlightedIndex = 0
                            spinState = SpinState.IDLE

                            if (isSoundEnabled) soundManager.playJump()
                            if (isVibrationEnabled) vibrationManager.vibrateWin()
                            playerToDelete = null
                        }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        } else {
            onDispose { }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var spinJob by remember { mutableStateOf<Job?>(null) }
    var isHardSpinning by remember { mutableStateOf(false) }

    fun startSpinning(isHard: Boolean) {
        if (isHardSpinning) return
        spinJob?.cancel()
        isHardSpinning = isHard

        spinJob = coroutineScope.launch {
            spinState = SpinState.SPINNING
            val totalDurationMs = if (isHard) Random.nextLong(6000, 10000) else Random.nextLong(3000, 6000)
            val startTime = System.currentTimeMillis()
            var currentInterval = if (isHard) 10L else 50L

            // ИСПРАВЛЕНИЕ 1: Ждем, пока интервал не станет 800L (создаем долгую интригу)
            while (currentInterval < 800L) {
                highlightedIndex = (highlightedIndex + 1) % players.size
                if (isSoundEnabled) soundManager.playTick()
                if (isVibrationEnabled) vibrationManager.vibrateTick()
                delay(currentInterval)

                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f)

                // Используем кривую ease-out. Она быстро делает рулетку медленной,
                // и она долго и тягуче докручивается в самом конце.
                val easeOut = 1f - (1f - progress) * (1f - progress)
                currentInterval = (50 + (easeOut * 800)).toLong()
            }

            if (isFakeStopEnabled && Random.nextFloat() <= fakeStopChance) {
                spinState = SpinState.FAKE_STOP
                delay(600L) // Пауза перед прыжком
                val jumpOffset = listOf(-2, -1, 1, 2).random()
                highlightedIndex = (highlightedIndex + jumpOffset).mod(players.size)
                if (isSoundEnabled) soundManager.playJump()
                if (isVibrationEnabled) vibrationManager.vibrateJump()
                delay(600L) // Пауза после прыжка, чтобы осознать подставу
            }

            spinState = SpinState.STOPPED
            isHardSpinning = false
            val winner = players[highlightedIndex]
            viewModel.recordSpinResult(winner.id)

            if (isSoundEnabled) soundManager.playWin()
            if (isVibrationEnabled) vibrationManager.vibrateWin()
        }
    }

    // Главный контейнер (Box на весь экран)
    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = CheeseBrown)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEliminateDialog = true }) {
                            Icon(Icons.Default.Flag, contentDescription = "Выбыл", tint = CheeseBrown)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CheeseBackground)
                )
            },
            containerColor = CheeseBackground
        ) { innerPadding ->

            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    RouletteWheel(
                        players = players,
                        displayMode = displayMode,
                        highlightedIndex = highlightedIndex,
                        onSectorLongClick = { player ->
                            if (spinState != SpinState.SPINNING && spinState != SpinState.FAKE_STOP && players.size > 1) {
                                playerToDelete = player
                            }
                        }
                    ) {
                        CheeseButton(
                            isSpinning = spinState == SpinState.SPINNING || spinState == SpinState.FAKE_STOP,
                            onNormalClick = { startSpinning(isHard = false) },
                            onLongClick = { startSpinning(isHard = true) }
                        )
                    }

                    Box(modifier = Modifier.height(100.dp), contentAlignment = Alignment.Center) {
                        this@Column.AnimatedVisibility(
                            visible = spinState == SpinState.STOPPED && highlightedIndex in players.indices && players.size > 1,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            ResultBanner(player = players[highlightedIndex], displayMode = displayMode)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = playerToDelete != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f))
                            .clickable { playerToDelete = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(text = "Удаление", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = playerToDelete?.name?.ifBlank { "Игрок ${playerToDelete!!.id}" } ?: "",
                                color = playerToDelete?.color ?: Color.White,
                                fontSize = 32.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.White, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(text = "Потрясите телефон 📳", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "(или нажмите на экран для отмены)", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        } // Конец Scaffold

        // ИСПРАВЛЕНИЕ 2 и 3: Экран победителя ВЫНЕСЕН ИЗ Scaffold!
        // Теперь он рисуется поверх всего экрана (включая верхнее меню).
        AnimatedVisibility(
            visible = players.size == 1,
            enter = scaleIn(initialScale = 0.8f) + fadeIn(tween(500)),
            exit = fadeOut(tween(300))
        ) {
            // ИСПРАВЛЕНИЕ 3: Запоминаем победителя (remember).
            // Это предотвращает баг с "Игрок 1", когда ViewModel сбрасывает данные перед выходом!
            val finalWinner = remember { players.firstOrNull() } ?: return@AnimatedVisibility

            val infiniteTransition = rememberInfiniteTransition(label = "winPulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "winScale"
            )

            LaunchedEffect(Unit) {
                if (isSoundEnabled) soundManager.playWin()
                if (isVibrationEnabled) vibrationManager.vibrateWin()
                delay(5000L)
                viewModel.restorePlayersAfterGame()
                onBackToMenu()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(finalWinner.color)
                    // Блокируем клики по экрану! Никто ничего не нажмет, пока идет праздник.
                    .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉 ПОБЕДИТЕЛЬ 🎉", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = finalWinner.name.ifBlank { "Игрок ${finalWinner.id}" },
                        fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier.scale(scale)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Игра окончена", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // Модалки настроек и туториала (остаются поверх всего)
        if (showSettings) {
            SettingsModal(
                isSoundEnabled = isSoundEnabled, isVibrationEnabled = isVibrationEnabled,
                displayMode = displayMode, isFakeStopEnabled = isFakeStopEnabled, fakeStopChance = fakeStopChance,
                onSoundToggle = { viewModel.setSoundEnabled(it) }, onVibrationToggle = { viewModel.setVibrationEnabled(it) },
                onFakeStopToggle = { viewModel.setFakeStopEnabled(it) }, onChanceChange = { viewModel.setFakeStopChance(it) },
                onModeSelect = { viewModel.setDisplayMode(it) },
                onExitToMenu = { showSettings = false; viewModel.restorePlayersAfterGame(); onBackToMenu() },
                onDismiss = { showSettings = false }
            )
        }

        if (showEliminateDialog) {
            EliminateDialog(
                players = players,
                onPlayerEliminated = { player ->
                    viewModel.removePlayer(player)
                    highlightedIndex = 0
                    spinState = SpinState.IDLE
                    showEliminateDialog = false
                },
                onDismiss = { showEliminateDialog = false }
            )
        }

        if (tutorialStep == TutorialStep.SPIN_HINT) {
            TutorialOverlay(
                text = "Кликните на сыр в центре, чтобы запустить рулетку.\n\nУдерживайте кнопку сыра нажатой для мощного и долгого вращения!",
                onNext = { viewModel.nextTutorialStep() },
                onSkip = { viewModel.skipTutorial() }
            )
        } else if (tutorialStep == TutorialStep.DELETE_HINT) {
            TutorialOverlay(
                text = "Если кто-то выбыл — удерживайте палец на его секторе или нажмите на иконку флажка сверху, чтобы удалить его из игры.",
                onNext = { viewModel.nextTutorialStep() },
                onSkip = { viewModel.skipTutorial() }
            )
        }
    }
}