// ui/screens/GameScreen.kt
package com.pip.cheeseroul.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pip.cheeseroul.model.*
import com.pip.cheeseroul.ui.components.CheeseButton
import com.pip.cheeseroul.ui.components.ResultBanner
import com.pip.cheeseroul.ui.components.RouletteWheel
import com.pip.cheeseroul.ui.theme.CheeseBackground
import com.pip.cheeseroul.ui.theme.CheeseBrown
import com.pip.cheeseroul.ui.theme.CheeseOrange
import com.pip.cheeseroul.ui.theme.CheeseYellow
import com.pip.cheeseroul.utils.SoundManager
import com.pip.cheeseroul.utils.VibrationManager
import com.pip.cheeseroul.viewmodel.RouletteViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// Класс для частиц сыра и искр
data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float, val maxLife: Float,
    val isCheese: Boolean, val color: Color,
    var rotation: Float, var rotSpeed: Float,
    var size: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: RouletteViewModel,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val players by viewModel.players.collectAsState()
    val eliminatedPlayers by viewModel.eliminatedPlayers.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val isFakeStopEnabled by viewModel.isFakeStopEnabled.collectAsState()
    val fakeStopChance by viewModel.fakeStopChance.collectAsState()
    val eliminationEffect by viewModel.eliminationEffect.collectAsState()
    val spinAnimationMode by viewModel.spinAnimationMode.collectAsState()

    // Читаем состояние сырного фона
    val isCheeseBackgroundEnabled by viewModel.isCheeseBackgroundEnabled.collectAsState()

    // Стейты статистики
    val totalSpins by viewModel.totalSpins.collectAsState()
    val fakeStopsCount by viewModel.fakeStopsCount.collectAsState()

    val rotationAngle = remember { Animatable(0f) }
    var highlightedIndex by remember { mutableIntStateOf(0) }
    var spinState by remember { mutableStateOf(SpinState.IDLE) }
    var showSettings by remember { mutableStateOf(false) }
    var showEliminateDialog by remember { mutableStateOf(false) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }
    var animatingPlayer by remember { mutableStateOf<Player?>(null) }
    var currentAnimEffect by remember { mutableStateOf(EliminationEffect.NONE) }
    val eliminationProgress = remember { Animatable(0f) }
    val soundManager = remember { SoundManager(context) }
    val vibrationManager = remember { VibrationManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val spinDuration by viewModel.spinDuration.collectAsState()
    val fakeStopDuration by viewModel.fakeStopDuration.collectAsState()
    val fakeJumpDuration by viewModel.fakeJumpDuration.collectAsState()
    val easingFactor by viewModel.easingFactor.collectAsState()
    val fakeStopsCountDebug by viewModel.fakeStopsCountDebug.collectAsState()
    val soundVolume by viewModel.soundVolume.collectAsState()
    val vibrationStrength by viewModel.vibrationStrength.collectAsState()

    // Для генератора частиц
    var screenWidth by remember { mutableFloatStateOf(1000f) }
    var screenHeight by remember { mutableFloatStateOf(2000f) }
    var frameTrigger by remember { mutableIntStateOf(0) }
    val particles = remember { mutableListOf<Particle>() }

    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    // Движок частиц
    LaunchedEffect(spinState, players.size) {
        var lastTime = withFrameNanos { it }
        while (true) {
            withFrameNanos { time ->
                val dt = (time - lastTime) / 1_000_000_000f
                lastTime = time
                var needsUpdate = false

                if (Random.nextFloat() < 0.015f) {
                    particles.add(
                        Particle(
                            x = Random.nextFloat() * screenWidth,
                            y = -100f,
                            vx = Random.nextFloat() * 100f - 50f,
                            vy = Random.nextFloat() * 100f + 100f,
                            life = 15f,
                            maxLife = 15f,
                            isCheese = true,
                            color = CheeseYellow,
                            rotation = Random.nextFloat() * 360f,
                            rotSpeed = Random.nextFloat() * 200f - 100f,
                            size = Random.nextFloat() * 25f + 15f
                        )
                    )
                    needsUpdate = true
                }

                if (spinState == SpinState.SPINNING || spinState == SpinState.FAKE_STOP) {
                    repeat(3) {
                        val angle = Random.nextFloat() * PI * 2
                        val speed = Random.nextFloat() * 600f + 200f
                        particles.add(
                            Particle(
                                x = screenWidth / 2f,
                                y = screenHeight / 2f,
                                vx = (cos(angle) * speed).toFloat(),
                                vy = (sin(angle) * speed).toFloat(),
                                life = Random.nextFloat() * 0.4f + 0.2f,
                                maxLife = 0.6f,
                                isCheese = false,
                                color = listOf(CheeseYellow, CheeseOrange, Color.White).random(),
                                rotation = 0f,
                                rotSpeed = 0f,
                                size = Random.nextFloat() * 12f + 4f
                            )
                        )
                    }
                    needsUpdate = true
                }

                if (particles.isNotEmpty()) {
                    val iter = particles.iterator()
                    while (iter.hasNext()) {
                        val p = iter.next()
                        p.life -= dt
                        if (p.life <= 0f || p.y > screenHeight + 100f) {
                            iter.remove()
                        } else {
                            p.x += p.vx * dt
                            p.y += p.vy * dt
                            p.vy += if (p.isCheese) 50f * dt else 600f * dt
                            p.rotation += p.rotSpeed * dt
                        }
                    }
                    needsUpdate = true
                }
                if (needsUpdate) frameTrigger++

                // Фоновый сырный дождь (падает только если включена настройка)
                if (isCheeseBackgroundEnabled && Random.nextFloat() < 0.015f) {
                    particles.add(
                        Particle(
                            x = Random.nextFloat() * screenWidth,
                            y = -100f,
                            vx = Random.nextFloat() * 100f - 50f,
                            vy = Random.nextFloat() * 100f + 100f,
                            life = 15f,
                            maxLife = 15f,
                            isCheese = true,
                            color = CheeseYellow,
                            rotation = Random.nextFloat() * 360f,
                            rotSpeed = Random.nextFloat() * 200f - 100f,
                            size = Random.nextFloat() * 25f + 15f
                        )
                    )
                    needsUpdate = true
                }
            }
        }
    }

    LaunchedEffect(players.size) {
        snapshotFlow { rotationAngle.value }.collect { angle ->
            if (players.isEmpty()) return@collect
            val sweep = 360f / players.size
            val index = (((angle + (sweep / 2f)) / sweep).toInt()) % players.size
            val safeIndex = index.coerceIn(0, players.size - 1)
            if (safeIndex != highlightedIndex) {
                highlightedIndex = safeIndex
                if (spinState == SpinState.SPINNING || spinState == SpinState.FAKE_STOP) {
                    if (isSoundEnabled) soundManager.playTick()
                    if (isVibrationEnabled) vibrationManager.vibrateTick()
                }
            }
        }
    }

    fun triggerElimination(targetPlayer: Player) {
        coroutineScope.launch {
            val resolvedEffect = if (eliminationEffect == EliminationEffect.RANDOM) {
                listOf(
                    EliminationEffect.EXPLOSION,
                    EliminationEffect.FADE,
                    EliminationEffect.SHRINK,
                    EliminationEffect.FLY_AWAY
                ).random()
            } else {
                eliminationEffect
            }

            if (resolvedEffect == EliminationEffect.NONE) {
                viewModel.removePlayer(targetPlayer)
                spinState = SpinState.IDLE
            } else {
                animatingPlayer = targetPlayer
                currentAnimEffect = resolvedEffect
                eliminationProgress.snapTo(0f)
                if (isSoundEnabled) soundManager.playJump()
                if (isVibrationEnabled) vibrationManager.vibrateWin()
                eliminationProgress.animateTo(
                    1f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing)
                )
                viewModel.removePlayer(targetPlayer)
                animatingPlayer = null
                spinState = SpinState.IDLE
            }
        }
    }

    var spinJob by remember { mutableStateOf<Job?>(null) }
    var isHardSpinning by remember { mutableStateOf(false) }

    fun startSpinning(isHard: Boolean) {
        if (isHardSpinning) return
        spinJob?.cancel()
        isHardSpinning = isHard
        spinJob = coroutineScope.launch {
            spinState = SpinState.SPINNING
            // ИСПОЛЬЗУЕМ НАСТРОЙКУ ПОЛЗУНКА (переводим секунды в миллисекунды)
            val duration = (spinDuration * 1000).toInt()
            val spins = if (isHard) Random.nextInt(20, 30) else Random.nextInt(10, 15)
            val sectorAngle = 360f / players.size
            val finalIndex = Random.nextInt(players.size)
            val currentBase = rotationAngle.value - (rotationAngle.value % 360f)
            val exactTargetAngle = currentBase + (spins * 360f) + (finalIndex * sectorAngle)
            val targetRotation =
                if (exactTargetAngle <= rotationAngle.value) exactTargetAngle + 360f else exactTargetAngle

            val isFake = isFakeStopEnabled && Random.nextFloat() <= fakeStopChance

            // ИСПОЛЬЗУЕМ НАСТРОЙКУ КРИВОЙ (Easing)
            // easingFactor от 0 до 1. 0 - плавно, 1 - очень резко в конце
            val customEasing = CubicBezierEasing(0.1f, 1f - easingFactor, 0.2f, 1f)

            if (isFake) {
                // ИСПОЛЬЗУЕМ НАСТРОЙКУ КОЛИЧЕСТВА ФЕЙКОВ (для простоты пока делаем 1 фейк, но учитываем скорость)
                val maxOffset = max(2, players.size)
                val randomOffset = Random.nextInt(1, maxOffset)
                val fakeIndex = (finalIndex + randomOffset) % players.size

                val fakeExactTarget = currentBase + (spins * 360f) + (fakeIndex * sectorAngle)
                val fakeTargetRotation =
                    if (fakeExactTarget <= rotationAngle.value) fakeExactTarget + 360f else fakeExactTarget

                rotationAngle.animateTo(
                    targetValue = fakeTargetRotation,
                    animationSpec = tween(duration, easing = customEasing)
                )
                spinState = SpinState.FAKE_STOP

                // ИСПОЛЬЗУЕМ НАСТРОЙКУ ПАУЗЫ ФЕЙК-ОСТАНОВКИ
                delay((fakeStopDuration * 1000).toLong())

                if (isSoundEnabled) soundManager.playJump()
                if (isVibrationEnabled) vibrationManager.vibrateJump()

                val distanceToCorrect =
                    ((finalIndex - fakeIndex + players.size) % players.size) * sectorAngle
                val finalJumpTarget = fakeTargetRotation + distanceToCorrect

                // ИСПОЛЬЗУЕМ НАСТРОЙКУ СКОРОСТИ ПРЫЖКА
                rotationAngle.animateTo(
                    targetValue = finalJumpTarget,
                    animationSpec = tween(
                        (fakeJumpDuration * 1000).toInt(),
                        easing = FastOutSlowInEasing
                    )
                )
            } else {
                rotationAngle.animateTo(
                    targetValue = targetRotation,
                    animationSpec = tween(duration, easing = customEasing)
                )
            }

            spinState = SpinState.STOPPED
            isHardSpinning = false
            val winner = players[highlightedIndex]
            viewModel.recordSpinResult(winner.id)
            viewModel.recordSpin(wasFakeStop = isFake)

            if (isSoundEnabled) soundManager.playWin()
            if (isVibrationEnabled) vibrationManager.vibrateWin()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        if ((spinState == SpinState.IDLE || spinState == SpinState.STOPPED) && animatingPlayer == null) {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = CheeseBrown
                                )
                            }
                        }
                    },
                    actions = {
                        if ((spinState == SpinState.IDLE || spinState == SpinState.STOPPED) && animatingPlayer == null) {
                            IconButton(onClick = { showEliminateDialog = true }) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = CheeseBrown
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CheeseBackground)
                )
            },
            containerColor = CheeseBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .onSizeChanged {
                        screenWidth = it.width.toFloat()
                        screenHeight = it.height.toFloat()
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    frameTrigger
                    particles.forEach { p ->
                        val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                        if (p.isCheese) {
                            withTransform({
                                translate(p.x, p.y)
                                rotate(p.rotation, pivot = Offset.Zero)
                            }) {
                                val path = Path().apply {
                                    moveTo(0f, -p.size)
                                    lineTo(p.size * 0.866f, p.size * 0.5f)
                                    lineTo(-p.size * 0.866f, p.size * 0.5f)
                                    close()
                                }
                                drawPath(path, p.color.copy(alpha = alpha * 0.7f))
                            }
                        } else {
                            drawCircle(
                                p.color.copy(alpha = alpha),
                                radius = p.size * alpha,
                                center = Offset(p.x, p.y)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    RouletteWheel(
                        players = players,
                        displayMode = displayMode,
                        spinAnimationMode = spinAnimationMode,
                        currentAnimatedAngle = rotationAngle.value,
                        highlightedIndex = highlightedIndex,
                        animatingPlayer = animatingPlayer,
                        currentEffect = currentAnimEffect,
                        eliminationProgress = eliminationProgress.value,
                        onSectorLongClick = { player ->
                            if ((spinState == SpinState.IDLE || spinState == SpinState.STOPPED) && animatingPlayer == null && players.size > 1) {
                                playerToDelete = player
                            }
                        }
                    ) {
                        val buttonRotation = when (spinAnimationMode) {
                            SpinAnimationMode.SPINNING_ARROW, SpinAnimationMode.HIGHLIGHT -> rotationAngle.value
                            SpinAnimationMode.SPINNING_WHEEL -> 180f
                        }
                        CheeseButton(
                            isSpinning = spinState == SpinState.SPINNING || spinState == SpinState.FAKE_STOP,
                            onNormalClick = { startSpinning(isHard = false) },
                            onLongClick = { startSpinning(isHard = true) },
                            modifier = Modifier.rotate(buttonRotation)
                        )
                    }

                    // НОВОЕ: Панель статистики
                    FakeStopStatsBanner(
                        totalSpins = totalSpins,
                        fakeStopsCount = fakeStopsCount,
                        onReset = { viewModel.resetFakeStopStats() }
                    )

                    Box(modifier = Modifier.height(100.dp), contentAlignment = Alignment.Center) {
                        this@Column.AnimatedVisibility(
                            visible = spinState == SpinState.STOPPED && highlightedIndex in players.indices && players.size > 1,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            ResultBanner(
                                player = players[highlightedIndex],
                                displayMode = displayMode
                            )
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Исключить из игры?",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        playerToDelete?.color ?: Color.Gray,
                                        androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(playerToDelete?.name?.ifBlank { "Игрок ${playerToDelete!!.id}" }
                                ?: "",
                                color = playerToDelete?.color ?: Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(48.dp))
                            Button(
                                onClick = {
                                    val target = playerToDelete!!; playerToDelete =
                                    null; triggerElimination(target)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFD32F2F
                                    )
                                ),
                                modifier = Modifier
                                    .height(56.dp)
                                    .padding(horizontal = 32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Да, выбыл!",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            TextButton(onClick = { playerToDelete = null }) {
                                Text(
                                    "Отмена",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = players.size == 1 && animatingPlayer == null,
            enter = scaleIn(initialScale = 0.8f) + fadeIn(tween(500)),
            exit = fadeOut(tween(300))
        ) {
            val finalWinner = remember { players.firstOrNull() } ?: return@AnimatedVisibility
            val infiniteTransition = rememberInfiniteTransition(label = "winPulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    tween(600, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ),
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
                    .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🎉 ПОБЕДИТЕЛЬ 🎉",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        finalWinner.name.ifBlank { "Игрок ${finalWinner.id}" },
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.scale(scale)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Игра окончена", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        if (showSettings) {
            SettingsModal(
                isSoundEnabled = isSoundEnabled,
                isVibrationEnabled = isVibrationEnabled,
                displayMode = displayMode,
                isFakeStopEnabled = isFakeStopEnabled,
                spinAnimationMode = spinAnimationMode,
                eliminationEffect = eliminationEffect,

                // Передаем состояние фона
                isCheeseBackgroundEnabled = isCheeseBackgroundEnabled,

                fakeStopChance = fakeStopChance,
                spinDuration = spinDuration,
                fakeStopDuration = fakeStopDuration,
                fakeJumpDuration = fakeJumpDuration,
                easingFactor = easingFactor,
                fakeStopsCountDebug = fakeStopsCountDebug,
                soundVolume = soundVolume,
                vibrationStrength = vibrationStrength,

                onSoundToggle = { viewModel.setSoundEnabled(it) },
                onVibrationToggle = { viewModel.setVibrationEnabled(it) },
                onFakeStopToggle = { viewModel.setFakeStopEnabled(it) },
                onModeSelect = { viewModel.setDisplayMode(it) },
                onEffectSelect = { viewModel.setEliminationEffect(it) },
                onSpinModeSelect = { viewModel.setSpinAnimationMode(it) },

                // Передаем коллбэк для фона и сброса обучения
                onCheeseBackgroundToggle = { viewModel.setCheeseBackgroundEnabled(it) },
                onResetTutorial = {
                    viewModel.resetTutorial()
                    showSettings = false // Автоматически закрываем настройки
                    viewModel.restorePlayersAfterGame() // Сбрасываем игру
                    onBackToMenu() // Откидываем в меню, чтобы показать подсказки с самого начала
                },

                onChanceChange = { viewModel.setFakeStopChance(it) },
                onSpinDurationChange = { viewModel.setSpinDuration(it) },
                onFakeStopDurationChange = { viewModel.setFakeStopDuration(it) },
                onFakeJumpDurationChange = { viewModel.setFakeJumpDuration(it) },
                onEasingFactorChange = { viewModel.setEasingFactor(it) },
                onFakeStopsCountDebugChange = { viewModel.setFakeStopsCountDebug(it) },
                onSoundVolumeChange = { viewModel.setSoundVolume(it) },
                onVibrationStrengthChange = { viewModel.setVibrationStrength(it) },

                onExitToMenu = {
                    showSettings = false; viewModel.restorePlayersAfterGame(); onBackToMenu()
                },
                onDismiss = { showSettings = false }
            )
        }

        if (showEliminateDialog) {
            EliminateDialog(
                players = players, eliminatedPlayers = eliminatedPlayers,
                onPlayerEliminated = { player ->
                    showEliminateDialog = false; triggerElimination(
                    player
                )
                },
                onPlayerRestored = { player ->
                    showEliminateDialog = false; viewModel.restorePlayer(
                    player
                )
                },
                onDismiss = { showEliminateDialog = false }
            )
        }
    }
}

// НОВОЕ: Компонент для красивого отображения статистики
@Composable
fun FakeStopStatsBanner(totalSpins: Int, fakeStopsCount: Int, onReset: () -> Unit) {
    val percent = if (totalSpins > 0) (fakeStopsCount * 100) / totalSpins else 0
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(
                Color.Black.copy(alpha = 0.3f),
                androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Запусков: $totalSpins | Рандом: $fakeStopsCount ($percent%)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            onClick = onReset,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Сбросить статистику",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}