// viewmodel/RouletteViewModel.kt
package com.pip.cheeseroul.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pip.cheeseroul.model.*
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

    private val _eliminatedPlayers = MutableStateFlow<List<Player>>(emptyList())
    val eliminatedPlayers: StateFlow<List<Player>> = _eliminatedPlayers.asStateFlow()

    private val _spinState = MutableStateFlow(SpinState.IDLE)
    val spinState: StateFlow<SpinState> = _spinState.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _isFakeStopEnabled = MutableStateFlow(true)
    val isFakeStopEnabled: StateFlow<Boolean> = _isFakeStopEnabled.asStateFlow()

    private val _fakeStopChance = MutableStateFlow(0.5f)
    val fakeStopChance: StateFlow<Float> = _fakeStopChance.asStateFlow()

    private val _eliminationEffect = MutableStateFlow(EliminationEffect.RANDOM)
    val eliminationEffect: StateFlow<EliminationEffect> = _eliminationEffect.asStateFlow()

    private val _spinAnimationMode = MutableStateFlow(SpinAnimationMode.SPINNING_ARROW)
    val spinAnimationMode: StateFlow<SpinAnimationMode> = _spinAnimationMode.asStateFlow()

    private val _gameHistory = MutableStateFlow<List<GameStat>>(emptyList())
    val gameHistory: StateFlow<List<GameStat>> = _gameHistory.asStateFlow()

    // НОВОЕ: Стейты для статистики ложных срабатываний
    private val _totalSpins = MutableStateFlow(0)
    val totalSpins: StateFlow<Int> = _totalSpins.asStateFlow()

    private val _fakeStopsCount = MutableStateFlow(0)
    val fakeStopsCount: StateFlow<Int> = _fakeStopsCount.asStateFlow()

    private var initialPlayersSnapshot = emptyList<Player>()
    private var currentSessionStartTime = 0L
    private val currentSpinCounts = mutableMapOf<Int, Int>()

    init { generatePlayers(_playerCount.value) }

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

    private fun finishTutorial() { prefs.edit().putBoolean("isFirstRun", false).apply() }

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
            Player(id = id, name = currentNames[id] ?: "", color = SectorColors[index % SectorColors.size])
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
    fun setEliminationEffect(effect: EliminationEffect) { _eliminationEffect.value = effect }
    fun setSpinAnimationMode(mode: SpinAnimationMode) { _spinAnimationMode.value = mode }

    fun startGameSession() {
        initialPlayersSnapshot = _players.value
        currentSessionStartTime = System.currentTimeMillis()
        currentSpinCounts.clear()
        _eliminatedPlayers.value = emptyList()
        _players.value.forEach { currentSpinCounts[it.id] = 0 }
    }

    fun recordSpinResult(playerId: Int) { currentSpinCounts[playerId] = (currentSpinCounts[playerId] ?: 0) + 1 }

    // НОВОЕ: Учет статистики каждого вращения
    fun recordSpin(wasFakeStop: Boolean) {
        _totalSpins.value++
        if (wasFakeStop) {
            _fakeStopsCount.value++
        }
    }

    // НОВОЕ: Сброс статистики
    fun resetFakeStopStats() {
        _totalSpins.value = 0
        _fakeStopsCount.value = 0
    }

    fun removePlayer(player: Player) {
        val updated = _players.value.filter { it.id != player.id }
        _eliminatedPlayers.update { it + player }
        _players.value = updated
        if (updated.size == 1) saveGameStat(updated.first())
    }

    fun restorePlayer(player: Player) {
        val updatedEliminated = _eliminatedPlayers.value.filter { it.id != player.id }
        _eliminatedPlayers.value = updatedEliminated
        val updatedPlayers = (_players.value + player).sortedBy { it.id }
        _players.value = updatedPlayers
    }

    private fun saveGameStat(winner: Player) {
        val stat = GameStat(
            durationMs = System.currentTimeMillis() - currentSessionStartTime,
            initialPlayerCount = initialPlayersSnapshot.size,
            winner = winner,
            firstEliminated = _eliminatedPlayers.value.firstOrNull(),
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