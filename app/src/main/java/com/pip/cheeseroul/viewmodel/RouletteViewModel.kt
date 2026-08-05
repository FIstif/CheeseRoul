// viewmodel/RouletteViewModel.kt
package com.pip.cheeseroul.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pip.cheeseroul.model.DisplayMode
import com.pip.cheeseroul.model.EliminationEffect
import com.pip.cheeseroul.model.GameStat
import com.pip.cheeseroul.model.Player
import com.pip.cheeseroul.model.SpinState
import com.pip.cheeseroul.model.TutorialStep
import com.pip.cheeseroul.ui.theme.SectorColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RouletteViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("CheesePrefs", Context.MODE_PRIVATE)

    private val _tutorialStep = MutableStateFlow(
        if (prefs.getBoolean("isFirstRun", true)) TutorialStep.SETUP_HINT else TutorialStep.DONE
    )
    val tutorialStep: StateFlow<TutorialStep> = _tutorialStep.asStateFlow()

    private val _displayMode = MutableStateFlow(DisplayMode.COLOR_ONLY)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _playerCount = MutableStateFlow(4)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _spinState = MutableStateFlow(SpinState.IDLE)
    val spinState: StateFlow<SpinState> = _spinState.asStateFlow()

    private val _selectedPlayer = MutableStateFlow<Player?>(null)
    val selectedPlayer: StateFlow<Player?> = _selectedPlayer.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _isFakeStopEnabled = MutableStateFlow(true)
    val isFakeStopEnabled: StateFlow<Boolean> = _isFakeStopEnabled.asStateFlow()

    private val _fakeStopChance = MutableStateFlow(0.5f)
    val fakeStopChance: StateFlow<Float> = _fakeStopChance.asStateFlow()

    // НОВОЕ: Состояние эффекта удаления
    private val _eliminationEffect = MutableStateFlow(EliminationEffect.EXPLOSION)
    val eliminationEffect: StateFlow<EliminationEffect> = _eliminationEffect.asStateFlow()

    private val _gameHistory = MutableStateFlow<List<GameStat>>(emptyList())
    val gameHistory: StateFlow<List<GameStat>> = _gameHistory.asStateFlow()

    private var initialPlayersSnapshot = emptyList<Player>()
    private var currentSessionStartTime = 0L
    private val currentSpinCounts = mutableMapOf<Int, Int>()
    private val currentEliminations = mutableListOf<Player>()

    init {
        generatePlayers(_playerCount.value)
    }

    fun nextTutorialStep() {
        val next = when (_tutorialStep.value) {
            TutorialStep.SETUP_HINT -> TutorialStep.SPIN_HINT
            TutorialStep.SPIN_HINT -> TutorialStep.DELETE_HINT
            else -> TutorialStep.DONE
        }
        _tutorialStep.value = next
        if (next == TutorialStep.DONE) finishTutorial()
    }

    fun skipTutorial() {
        _tutorialStep.value = TutorialStep.DONE
        finishTutorial()
    }

    private fun finishTutorial() {
        prefs.edit().putBoolean("isFirstRun", false).apply()
    }

    fun setDisplayMode(mode: DisplayMode) { _displayMode.value = mode }

    fun setPlayerCount(count: Int) {
        if (count in 2..7 && count != _playerCount.value) {
            _playerCount.value = count
            generatePlayers(count)
        }
    }

    private fun generatePlayers(count: Int) {
        val currentNames = _players.value.associate { it.id to it.name }
        val newPlayers = (0 until count).map { index ->
            val id = index + 1
            Player(
                id = id,
                name = currentNames[id] ?: "Игрок $id",
                color = SectorColors[index % SectorColors.size]
            )
        }
        _players.value = newPlayers
    }

    fun updatePlayerName(id: Int, newName: String) {
        _players.update { list -> list.map { if (it.id == id) it.copy(name = newName) else it } }
    }

    fun setSoundEnabled(enabled: Boolean) { _isSoundEnabled.value = enabled }
    fun setVibrationEnabled(enabled: Boolean) { _isVibrationEnabled.value = enabled }
    fun setFakeStopEnabled(enabled: Boolean) { _isFakeStopEnabled.value = enabled }
    fun setFakeStopChance(chance: Float) { _fakeStopChance.value = chance }
    // НОВОЕ: Функция переключения эффекта
    fun setEliminationEffect(effect: EliminationEffect) { _eliminationEffect.value = effect }

    fun startGameSession() {
        initialPlayersSnapshot = _players.value
        currentSessionStartTime = System.currentTimeMillis()
        currentSpinCounts.clear()
        currentEliminations.clear()
        _players.value.forEach { currentSpinCounts[it.id] = 0 }
    }

    fun recordSpinResult(playerId: Int) {
        currentSpinCounts[playerId] = (currentSpinCounts[playerId] ?: 0) + 1
    }

    fun removePlayer(player: Player) {
        val updated = _players.value.filter { it.id != player.id }
        currentEliminations.add(player)
        _players.value = updated
        if (updated.size == 1) {
            val winner = updated.first()
            saveGameStat(winner)
        }
    }

    private fun saveGameStat(winner: Player) {
        val duration = System.currentTimeMillis() - currentSessionStartTime
        val stat = GameStat(
            durationMs = duration,
            initialPlayerCount = initialPlayersSnapshot.size,
            winner = winner,
            firstEliminated = currentEliminations.firstOrNull(),
            spinCounts = currentSpinCounts.toMap()
        )
        _gameHistory.update { listOf(stat) + it }
    }

    fun restorePlayersAfterGame() {
        if (initialPlayersSnapshot.isNotEmpty()) {
            _players.value = initialPlayersSnapshot
            _playerCount.value = initialPlayersSnapshot.size
        }
    }
}