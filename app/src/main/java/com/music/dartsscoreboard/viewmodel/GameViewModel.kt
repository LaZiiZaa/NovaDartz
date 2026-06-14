package com.music.dartsscoreboard.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.music.dartsscoreboard.data.AppDatabase
import com.music.dartsscoreboard.data.GameHistoryEntity
import com.music.dartsscoreboard.data.PlayerGameStats
import com.music.dartsscoreboard.data.PlayerStatsEntity
import com.music.dartsscoreboard.data.SavedPlayerEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.music.dartsscoreboard.model.*
import com.music.dartsscoreboard.ui.components.DartThrow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).gameHistoryDao()
    private val gson = Gson()

    private val prefs = application.getSharedPreferences("active_game", Context.MODE_PRIVATE)
    private val KEY_ROUTE = "active_route"

    // Route du jeu en cours (pour reprise après fermeture de l'app), null si aucun.
    private val _activeRoute = MutableStateFlow<String?>(null)
    val activeRoute: StateFlow<String?> = _activeRoute.asStateFlow()

    // Game states
    private val _x01State = MutableStateFlow<X01GameState?>(null)
    val x01State: StateFlow<X01GameState?> = _x01State.asStateFlow()

    private val _cricketState = MutableStateFlow<CricketGameState?>(null)
    val cricketState: StateFlow<CricketGameState?> = _cricketState.asStateFlow()

    private val _atcState = MutableStateFlow<ATCGameState?>(null)
    val atcState: StateFlow<ATCGameState?> = _atcState.asStateFlow()

    private val _countUpState = MutableStateFlow<CountUpGameState?>(null)
    val countUpState: StateFlow<CountUpGameState?> = _countUpState.asStateFlow()

    private val _killerState = MutableStateFlow<KillerGameState?>(null)
    val killerState: StateFlow<KillerGameState?> = _killerState.asStateFlow()

    // End-of-game stats for display
    private val _endGameStats = MutableStateFlow<List<PlayerGameStats>>(emptyList())
    val endGameStats: StateFlow<List<PlayerGameStats>> = _endGameStats.asStateFlow()

    // Saved players
    val savedPlayers: StateFlow<List<SavedPlayerEntity>> = dao.getAllSavedPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavedPlayer(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insertSavedPlayer(SavedPlayerEntity(name = name.trim()))
        }
    }

    fun deleteSavedPlayer(player: SavedPlayerEntity) {
        viewModelScope.launch {
            dao.deleteSavedPlayer(player)
        }
    }

    fun renameSavedPlayer(id: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            dao.renameSavedPlayer(id, newName.trim())
        }
    }

    // ===================== Reprise de partie =====================

    init {
        restoreActiveGame()
        observeAndPersistGames()
    }

    /** Marque le début d'une partie d'un type donné (pour la reprise). */
    private fun beginGame(route: String) {
        _activeRoute.value = route
        prefs.edit().putString(KEY_ROUTE, route).apply()
    }

    /** Persiste l'état d'une partie ; efface la route de reprise si la partie est terminée. */
    private fun saveGame(route: String, json: String, finished: Boolean) {
        prefs.edit().apply {
            putString("state_$route", json)
            if (finished) remove(KEY_ROUTE)
            apply()
        }
        if (finished && _activeRoute.value == route) _activeRoute.value = null
    }

    /** Sauvegarde automatiquement chaque état de jeu à chaque modification. */
    private fun observeAndPersistGames() {
        viewModelScope.launch { _x01State.collect { s -> if (s != null) saveGame("x01", gson.toJson(s), s.isFinished) } }
        viewModelScope.launch { _cricketState.collect { s -> if (s != null) saveGame("cricket", gson.toJson(s), s.isFinished) } }
        viewModelScope.launch { _atcState.collect { s -> if (s != null) saveGame("atc", gson.toJson(s), s.isFinished) } }
        viewModelScope.launch { _countUpState.collect { s -> if (s != null) saveGame("countup", gson.toJson(s), s.isFinished) } }
        viewModelScope.launch { _killerState.collect { s -> if (s != null) saveGame("killer", gson.toJson(s), s.isFinished) } }
    }

    /** Restaure la dernière partie non terminée au démarrage. Tolérant aux données corrompues. */
    private fun restoreActiveGame() {
        try {
            val route = prefs.getString(KEY_ROUTE, null) ?: return
            val finished: Boolean = when (route) {
                "x01" -> prefs.getString("state_x01", null)?.let {
                    _x01State.value = gson.fromJson(it, X01GameState::class.java); _x01State.value?.isFinished
                } ?: true
                "cricket" -> prefs.getString("state_cricket", null)?.let {
                    _cricketState.value = gson.fromJson(it, CricketGameState::class.java); _cricketState.value?.isFinished
                } ?: true
                "atc" -> prefs.getString("state_atc", null)?.let {
                    _atcState.value = gson.fromJson(it, ATCGameState::class.java); _atcState.value?.isFinished
                } ?: true
                "countup" -> prefs.getString("state_countup", null)?.let {
                    _countUpState.value = gson.fromJson(it, CountUpGameState::class.java); _countUpState.value?.isFinished
                } ?: true
                "killer" -> prefs.getString("state_killer", null)?.let {
                    _killerState.value = gson.fromJson(it, KillerGameState::class.java); _killerState.value?.isFinished
                } ?: true
                else -> true
            }
            // On ne reprend pas une partie déjà terminée.
            _activeRoute.value = if (finished) null else route
        } catch (e: Exception) {
            prefs.edit().clear().apply()
            _activeRoute.value = null
        }
    }

    // Undo stacks
    private val x01UndoStack = mutableListOf<X01GameState>()
    private val cricketUndoStack = mutableListOf<CricketGameState>()
    private val atcUndoStack = mutableListOf<ATCGameState>()
    private val countUpUndoStack = mutableListOf<CountUpGameState>()
    private val killerUndoStack = mutableListOf<KillerGameState>()

    // ===================== X01 =====================

    fun startX01Game(playerNames: List<String>, gameType: GameType, doubleOut: Boolean, isTeamMode: Boolean = false) {
        val startScore = when (gameType) {
            GameType.THREE_O_ONE -> 301
            GameType.FIVE_O_ONE -> 501
            else -> 501
        }
        val players = playerNames.mapIndexed { index, name ->
            val teamIdx = if (isTeamMode) index / 2 else -1
            X01PlayerState(player = Player(name, index, teamIndex = teamIdx), scoreRemaining = startScore)
        }
        // Team mode: alternate teams → [0, 2, 1, 3]
        val turnOrder = if (isTeamMode && players.size == 4) listOf(0, 2, 1, 3) else players.indices.toList()
        x01UndoStack.clear()
        beginGame("x01")
        _x01State.value = X01GameState(
            gameType = gameType,
            players = players,
            doubleOut = doubleOut,
            isTeamMode = isTeamMode,
            turnOrder = turnOrder,
            currentPlayerIndex = turnOrder.first()
        )
    }

    private fun getNextX01Player(state: X01GameState): Int {
        if (state.turnOrder.isEmpty()) return (state.currentPlayerIndex + 1) % state.players.size
        val currentTurnPos = state.turnOrder.indexOf(state.currentPlayerIndex)
        val nextTurnPos = (currentTurnPos + 1) % state.turnOrder.size
        return state.turnOrder[nextTurnPos]
    }

    private fun syncX01TeamScores(players: MutableList<X01PlayerState>, updatedIndex: Int): MutableList<X01PlayerState> {
        val updatedPlayer = players[updatedIndex]
        val teamIdx = updatedPlayer.player.teamIndex
        if (teamIdx < 0) return players
        // Sync scoreRemaining to teammate
        for (i in players.indices) {
            if (i != updatedIndex && players[i].player.teamIndex == teamIdx) {
                players[i] = players[i].copy(scoreRemaining = updatedPlayer.scoreRemaining)
            }
        }
        return players
    }

    fun submitX01Score(score: Int) {
        val state = _x01State.value ?: return
        if (state.isFinished) return
        x01UndoStack.add(state)

        val cp = state.currentPlayer
        val newScore = cp.scoreRemaining - score

        // Check if this was a checkout attempt (remaining <= 170 for double out, or <= 180)
        val isCheckoutRange = if (state.doubleOut) cp.scoreRemaining <= 170 else cp.scoreRemaining <= 180
        val checkoutAttempts = cp.checkoutAttempts + if (isCheckoutRange && score > 0) 1 else 0

        val updatedPlayer = when {
            newScore < 0 || (state.doubleOut && newScore == 1) -> {
                // Bust
                cp.copy(dartsThrown = cp.dartsThrown + 3, checkoutAttempts = checkoutAttempts)
            }
            newScore == 0 -> {
                // Winner
                cp.copy(
                    scoreRemaining = 0,
                    throwHistory = cp.throwHistory + score,
                    dartsThrown = cp.dartsThrown + 3,
                    checkoutAttempts = checkoutAttempts,
                    checkoutHits = cp.checkoutHits + 1
                )
            }
            else -> {
                cp.copy(
                    scoreRemaining = newScore,
                    throwHistory = cp.throwHistory + score,
                    dartsThrown = cp.dartsThrown + 3,
                    checkoutAttempts = checkoutAttempts
                )
            }
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = updatedPlayer

        // Sync team scores if in team mode
        if (state.isTeamMode) {
            syncX01TeamScores(updatedPlayers, state.currentPlayerIndex)
        }

        val isWinner = updatedPlayer.scoreRemaining == 0
        _x01State.value = state.copy(
            players = updatedPlayers,
            currentPlayerIndex = if (isWinner) state.currentPlayerIndex
            else getNextX01Player(state),
            isFinished = isWinner,
            winnerIndex = if (isWinner) state.currentPlayerIndex else -1
        )

        if (isWinner) {
            val startScore = if (state.gameType == GameType.THREE_O_ONE) 301 else 501
            val stats = updatedPlayers.map { p ->
                PlayerGameStats(
                    playerName = p.player.name,
                    isWinner = p.player.index == state.currentPlayerIndex,
                    stats = p.toStatsMap(),
                    numeric = p.toNumericMap()
                )
            }
            _endGameStats.value = stats
            saveGameToHistory(
                gameType = state.gameType.displayName,
                startScore = startScore,
                playerNames = state.players.map { it.player.name },
                winnerName = updatedPlayer.player.name,
                startTime = state.startTime,
                playerStats = stats,
                playerScores = updatedPlayers.associate { it.player.name to Pair(it.throwHistory.sum().toLong(), it.dartsThrown) },
                bestAvg = updatedPlayers.associate { it.player.name to it.average },
                count180s = updatedPlayers.associate { it.player.name to it.count180 },
                count140Plus = updatedPlayers.associate { it.player.name to it.count140Plus },
                count100Plus = updatedPlayers.associate { it.player.name to it.count100Plus },
                highestFinish = updatedPlayer.throwHistory.lastOrNull() ?: 0,
                winnerPlayerName = updatedPlayer.player.name
            )
        }
    }

    /**
     * Soumission en mode fléchette par fléchette.
     * En Double Out, vérifie que la dernière fléchette est un double si le score atteint 0.
     */
    fun submitX01Darts(darts: List<DartThrow>) {
        val state = _x01State.value ?: return
        if (state.isFinished) return

        val totalScore = darts.sumOf { it.score }
        val cp = state.currentPlayer
        val newScore = cp.scoreRemaining - totalScore

        // En double out, si on atteint 0 la dernière fléchette effective doit être un double
        if (state.doubleOut && newScore == 0) {
            val lastEffectiveDart = darts.lastOrNull { it.number > 0 }
            if (lastEffectiveDart == null || lastEffectiveDart.multiplier != 2) {
                // Bust : pas fini sur un double
                x01UndoStack.add(state)
                val isCheckoutRange = cp.scoreRemaining <= 170
                val checkoutAttempts = cp.checkoutAttempts + if (isCheckoutRange && totalScore > 0) 1 else 0
                val updatedPlayer = cp.copy(
                    dartsThrown = cp.dartsThrown + 3,
                    checkoutAttempts = checkoutAttempts
                )
                val updatedPlayers = state.players.toMutableList()
                updatedPlayers[state.currentPlayerIndex] = updatedPlayer
                _x01State.value = state.copy(
                    players = updatedPlayers,
                    currentPlayerIndex = getNextX01Player(state)
                )
                return
            }
        }

        // Sinon, on délègue à la logique standard
        submitX01Score(totalScore)
    }

    fun undoX01() {
        if (x01UndoStack.isNotEmpty()) {
            _x01State.value = x01UndoStack.removeAt(x01UndoStack.lastIndex)
        }
    }

    // ===================== Cricket =====================

    fun startCricketGame(playerNames: List<String>, isTeamMode: Boolean = false) {
        val players = playerNames.mapIndexed { index, name ->
            val teamIdx = if (isTeamMode) index / 2 else -1
            CricketPlayerState(player = Player(name, index, teamIndex = teamIdx))
        }
        val turnOrder = if (isTeamMode && players.size == 4) listOf(0, 2, 1, 3) else players.indices.toList()
        cricketUndoStack.clear()
        beginGame("cricket")
        _cricketState.value = CricketGameState(
            players = players,
            isTeamMode = isTeamMode,
            turnOrder = turnOrder,
            currentPlayerIndex = turnOrder.first()
        )
    }

    private fun getNextCricketPlayer(state: CricketGameState): Int {
        if (state.turnOrder.isEmpty()) return (state.currentPlayerIndex + 1) % state.players.size
        val currentTurnPos = state.turnOrder.indexOf(state.currentPlayerIndex)
        val nextTurnPos = (currentTurnPos + 1) % state.turnOrder.size
        return state.turnOrder[nextTurnPos]
    }

    private fun syncCricketTeam(players: MutableList<CricketPlayerState>, updatedIndex: Int): MutableList<CricketPlayerState> {
        val updatedPlayer = players[updatedIndex]
        val teamIdx = updatedPlayer.player.teamIndex
        if (teamIdx < 0) return players
        for (i in players.indices) {
            if (i != updatedIndex && players[i].player.teamIndex == teamIdx) {
                players[i] = players[i].copy(
                    marks = updatedPlayer.marks,
                    points = updatedPlayer.points
                )
            }
        }
        return players
    }

    fun submitCricketHit(number: Int, multiplier: Int) {
        val state = _cricketState.value ?: return
        if (state.isFinished) return
        if (state.dartsThisTurn >= CRICKET_MAX_DARTS) return // max 3 flechettes par tour
        cricketUndoStack.add(state)

        val cp = state.currentPlayer
        val currentMarks = cp.marks[number] ?: 0
        val newMarksTotal = currentMarks + multiplier

        var pointsGained = 0
        if (newMarksTotal > 3) {
            val excess = newMarksTotal - maxOf(currentMarks, 3)
            if (excess > 0) {
                // In team mode, only check opponents (other team)
                val opponents = if (state.isTeamMode) {
                    state.players.filter { it.player.teamIndex != cp.player.teamIndex }
                } else {
                    state.players.filterIndexed { i, _ -> i != state.currentPlayerIndex }
                }
                val allOthersClosed = opponents.all { (it.marks[number] ?: 0) >= 3 }
                if (!allOthersClosed) {
                    pointsGained = excess * number
                }
            }
        }

        val updatedMarks = cp.marks.toMutableMap()
        updatedMarks[number] = newMarksTotal

        val updatedPlayer = cp.copy(
            marks = updatedMarks,
            points = cp.points + pointsGained,
            totalMarksHit = cp.totalMarksHit + multiplier,
            dartsThrown = cp.dartsThrown + 1
        )

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = updatedPlayer

        // Sync team marks/points
        if (state.isTeamMode) {
            syncCricketTeam(updatedPlayers, state.currentPlayerIndex)
        }

        val hasWon = if (state.isTeamMode) {
            // Team wins: all closed + highest points (check against other team)
            updatedPlayer.allClosed() &&
                    updatedPlayers.filter { it.player.teamIndex != updatedPlayer.player.teamIndex }
                        .all { it.points <= updatedPlayer.points }
        } else {
            updatedPlayer.allClosed() &&
                    updatedPlayers.all { it.points <= updatedPlayer.points }
        }

        _cricketState.value = state.copy(
            players = updatedPlayers,
            isFinished = hasWon,
            winnerIndex = if (hasWon) state.currentPlayerIndex else -1,
            currentTurnMarks = state.currentTurnMarks + multiplier,
            dartsThisTurn = state.dartsThisTurn + 1
        )

        if (hasWon) {
            val stats = updatedPlayers.map { p ->
                PlayerGameStats(p.player.name, p.player.index == state.currentPlayerIndex, p.toStatsMap(), p.toNumericMap())
            }
            _endGameStats.value = stats
            saveGameToHistory(
                gameType = "Cricket", startScore = 0,
                playerNames = state.players.map { it.player.name },
                winnerName = updatedPlayer.player.name,
                startTime = state.startTime, playerStats = stats,
                playerScores = updatedPlayers.associate { it.player.name to Pair(it.points.toLong(), it.dartsThrown) },
                bestAvg = emptyMap(), count180s = emptyMap(), count140Plus = emptyMap(),
                count100Plus = emptyMap(), highestFinish = 0, winnerPlayerName = updatedPlayer.player.name
            )
        }
    }

    /** Fléchette ratée (ou numéro hors cricket) : compte une fléchette sans marque ni point. */
    fun submitCricketMiss() {
        val state = _cricketState.value ?: return
        if (state.isFinished) return
        if (state.dartsThisTurn >= CRICKET_MAX_DARTS) return
        cricketUndoStack.add(state)

        val cp = state.currentPlayer
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = cp.copy(dartsThrown = cp.dartsThrown + 1)

        _cricketState.value = state.copy(
            players = updatedPlayers,
            dartsThisTurn = state.dartsThisTurn + 1
        )
    }

    fun endCricketTurn() {
        val state = _cricketState.value ?: return
        if (state.isFinished) return
        val updatedPlayers = state.players.toMutableList()
        val cp = state.currentPlayer
        updatedPlayers[state.currentPlayerIndex] = cp.copy(roundsPlayed = cp.roundsPlayed + 1)
        _cricketState.value = state.copy(
            players = updatedPlayers,
            currentPlayerIndex = getNextCricketPlayer(state),
            currentTurnMarks = 0,
            dartsThisTurn = 0
        )
    }

    fun undoCricket() {
        if (cricketUndoStack.isNotEmpty()) {
            _cricketState.value = cricketUndoStack.removeAt(cricketUndoStack.lastIndex)
        }
    }

    // ===================== Around the Clock =====================

    fun startATCGame(playerNames: List<String>) {
        val players = playerNames.mapIndexed { index, name ->
            ATCPlayerState(player = Player(name, index))
        }
        atcUndoStack.clear()
        beginGame("atc")
        _atcState.value = ATCGameState(players = players)
    }

    fun submitATCThrow(hitTarget: Boolean) {
        val state = _atcState.value ?: return
        if (state.isFinished) return
        atcUndoStack.add(state)

        val cp = state.currentPlayer
        val newDarts = state.currentDartsInTurn + 1

        val updatedPlayer = if (hitTarget) {
            val nextTarget = when {
                cp.currentTarget < 20 -> cp.currentTarget + 1
                cp.currentTarget == 20 -> 25
                else -> 25 // Already at bull
            }
            val finished = cp.currentTarget == 25
            cp.copy(
                currentTarget = if (finished) 25 else nextTarget,
                dartsThrown = cp.dartsThrown + 1,
                hitsPerTarget = cp.hitsPerTarget + (cp.currentTarget to (cp.hitsPerTarget[cp.currentTarget] ?: 0) + 1),
                isFinished = finished
            )
        } else {
            cp.copy(dartsThrown = cp.dartsThrown + 1)
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = updatedPlayer

        val gameFinished = updatedPlayer.isFinished
        val nextPlayer = if (newDarts >= 3 || gameFinished) {
            (state.currentPlayerIndex + 1) % state.players.size
        } else {
            state.currentPlayerIndex
        }
        val resetDarts = if (newDarts >= 3 || gameFinished) 0 else newDarts

        _atcState.value = state.copy(
            players = updatedPlayers,
            currentPlayerIndex = if (gameFinished) state.currentPlayerIndex else nextPlayer,
            isFinished = gameFinished,
            winnerIndex = if (gameFinished) state.currentPlayerIndex else -1,
            currentDartsInTurn = resetDarts
        )

        if (gameFinished) {
            val stats = updatedPlayers.map { p ->
                PlayerGameStats(p.player.name, p.player.index == state.currentPlayerIndex, p.toStatsMap(), p.toNumericMap())
            }
            _endGameStats.value = stats
            saveGameToHistory(
                gameType = "Around the Clock", startScore = 0,
                playerNames = state.players.map { it.player.name },
                winnerName = updatedPlayer.player.name,
                startTime = state.startTime, playerStats = stats,
                playerScores = updatedPlayers.associate { it.player.name to Pair(it.targetsHit.toLong(), it.dartsThrown) },
                bestAvg = emptyMap(), count180s = emptyMap(), count140Plus = emptyMap(),
                count100Plus = emptyMap(), highestFinish = 0, winnerPlayerName = updatedPlayer.player.name
            )
        }
    }

    fun undoATC() {
        if (atcUndoStack.isNotEmpty()) {
            _atcState.value = atcUndoStack.removeAt(atcUndoStack.lastIndex)
        }
    }

    // ===================== Count Up =====================

    fun startCountUpGame(playerNames: List<String>, rounds: Int = 8) {
        val players = playerNames.mapIndexed { index, name ->
            CountUpPlayerState(player = Player(name, index))
        }
        countUpUndoStack.clear()
        beginGame("countup")
        _countUpState.value = CountUpGameState(players = players, maxRounds = rounds)
    }

    fun submitCountUpScore(score: Int) {
        val state = _countUpState.value ?: return
        if (state.isFinished) return
        countUpUndoStack.add(state)

        val cp = state.currentPlayer
        val updatedPlayer = cp.copy(
            totalScore = cp.totalScore + score,
            throwHistory = cp.throwHistory + score,
            dartsThrown = cp.dartsThrown + 3
        )

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = updatedPlayer

        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size

        // Les joueurs jouent à tour de rôle : la partie est terminée quand
        // chaque joueur a joué ses maxRounds manches.
        val gameFinished = updatedPlayers.all { it.throwHistory.size >= state.maxRounds }

        if (gameFinished) {
            val winnerIdx = updatedPlayers.indices.maxByOrNull { updatedPlayers[it].totalScore } ?: 0
            val stats = updatedPlayers.map { p ->
                PlayerGameStats(p.player.name, p.player.index == winnerIdx, p.toStatsMap(), p.toNumericMap())
            }
            _endGameStats.value = stats
            _countUpState.value = state.copy(
                players = updatedPlayers,
                isFinished = true,
                winnerIndex = winnerIdx
            )
            saveGameToHistory(
                gameType = "Count Up", startScore = state.maxRounds,
                playerNames = state.players.map { it.player.name },
                winnerName = updatedPlayers[winnerIdx].player.name,
                startTime = state.startTime, playerStats = stats,
                playerScores = updatedPlayers.associate { it.player.name to Pair(it.totalScore.toLong(), it.dartsThrown) },
                bestAvg = updatedPlayers.associate { it.player.name to it.average },
                count180s = updatedPlayers.associate { it.player.name to it.count180 },
                count140Plus = emptyMap(),
                count100Plus = updatedPlayers.associate { it.player.name to it.count100Plus },
                highestFinish = 0, winnerPlayerName = updatedPlayers[winnerIdx].player.name
            )
        } else {
            _countUpState.value = state.copy(
                players = updatedPlayers,
                currentPlayerIndex = nextIndex
            )
        }
    }

    fun undoCountUp() {
        if (countUpUndoStack.isNotEmpty()) {
            _countUpState.value = countUpUndoStack.removeAt(countUpUndoStack.lastIndex)
        }
    }

    // ===================== Killer =====================

    fun startKillerGame(playerNames: List<String>) {
        val players = playerNames.mapIndexed { index, name ->
            KillerPlayerState(player = Player(name, index))
        }
        killerUndoStack.clear()
        beginGame("killer")
        _killerState.value = KillerGameState(players = players, phase = KillerPhase.ASSIGN_NUMBERS)
    }

    fun assignKillerNumber(number: Int) {
        val state = _killerState.value ?: return
        if (state.phase != KillerPhase.ASSIGN_NUMBERS) return
        killerUndoStack.add(state)

        // Check number not already taken
        if (state.players.any { it.assignedNumber == number }) return

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = state.currentPlayer.copy(assignedNumber = number)

        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val allAssigned = updatedPlayers.all { it.assignedNumber != 0 }

        _killerState.value = state.copy(
            players = updatedPlayers,
            currentPlayerIndex = if (allAssigned) 0 else nextIndex,
            phase = if (allAssigned) KillerPhase.PLAYING else KillerPhase.ASSIGN_NUMBERS
        )
    }

    fun submitKillerThrow(hitNumber: Int, isDouble: Boolean) {
        val state = _killerState.value ?: return
        if (state.phase != KillerPhase.PLAYING || state.isFinished) return
        killerUndoStack.add(state)

        val cp = state.currentPlayer
        val updatedPlayers = state.players.toMutableList()

        if (hitNumber == cp.assignedNumber && isDouble && !cp.isKiller) {
            // Gain a life / become killer
            val newLives = minOf(cp.lives + 1, cp.maxLives)
            updatedPlayers[state.currentPlayerIndex] = cp.copy(
                lives = newLives,
                isKiller = newLives >= cp.maxLives,
                dartsThrown = cp.dartsThrown + 1
            )
        } else if (cp.isKiller && isDouble) {
            // Try to kill someone
            val targetPlayerIdx = updatedPlayers.indexOfFirst {
                it.assignedNumber == hitNumber && !it.isEliminated && it.player.index != cp.player.index
            }
            if (targetPlayerIdx >= 0) {
                val target = updatedPlayers[targetPlayerIdx]
                val newLives = target.lives - 1
                updatedPlayers[targetPlayerIdx] = target.copy(
                    lives = newLives,
                    isEliminated = newLives <= 0,
                    isKiller = if (newLives <= 0) false else target.isKiller
                )
                updatedPlayers[state.currentPlayerIndex] = cp.copy(
                    dartsThrown = cp.dartsThrown + 1,
                    killCount = cp.killCount + if (newLives <= 0) 1 else 0
                )
            } else if (hitNumber == cp.assignedNumber) {
                // Hit own number as killer - lose a life
                val newLives = cp.lives - 1
                updatedPlayers[state.currentPlayerIndex] = cp.copy(
                    lives = newLives,
                    isKiller = newLives >= cp.maxLives,
                    isEliminated = newLives <= 0,
                    dartsThrown = cp.dartsThrown + 1
                )
            } else {
                updatedPlayers[state.currentPlayerIndex] = cp.copy(dartsThrown = cp.dartsThrown + 1)
            }
        } else {
            updatedPlayers[state.currentPlayerIndex] = cp.copy(dartsThrown = cp.dartsThrown + 1)
        }

        val alive = updatedPlayers.filter { !it.isEliminated }
        val gameFinished = alive.size <= 1

        // Find next non-eliminated player
        var nextIdx = (state.currentPlayerIndex + 1) % state.players.size
        while (updatedPlayers[nextIdx].isEliminated && nextIdx != state.currentPlayerIndex) {
            nextIdx = (nextIdx + 1) % state.players.size
        }

        _killerState.value = state.copy(
            players = updatedPlayers,
            currentPlayerIndex = if (gameFinished) state.currentPlayerIndex else nextIdx,
            isFinished = gameFinished,
            winnerIndex = if (gameFinished) alive.firstOrNull()?.player?.index ?: -1 else -1
        )

        if (gameFinished && alive.isNotEmpty()) {
            val winner = alive.first()
            val stats = updatedPlayers.map { p ->
                PlayerGameStats(p.player.name, p.player.index == winner.player.index, p.toStatsMap(), p.toNumericMap())
            }
            _endGameStats.value = stats
            saveGameToHistory(
                gameType = "Killer", startScore = 0,
                playerNames = state.players.map { it.player.name },
                winnerName = winner.player.name,
                startTime = state.startTime, playerStats = stats,
                playerScores = updatedPlayers.associate { it.player.name to Pair(it.killCount.toLong(), it.dartsThrown) },
                bestAvg = emptyMap(), count180s = emptyMap(), count140Plus = emptyMap(),
                count100Plus = emptyMap(), highestFinish = 0, winnerPlayerName = winner.player.name
            )
        }
    }

    fun endKillerTurn() {
        val state = _killerState.value ?: return
        if (state.isFinished) return
        var nextIdx = (state.currentPlayerIndex + 1) % state.players.size
        while (state.players[nextIdx].isEliminated && nextIdx != state.currentPlayerIndex) {
            nextIdx = (nextIdx + 1) % state.players.size
        }
        _killerState.value = state.copy(currentPlayerIndex = nextIdx)
    }

    fun undoKiller() {
        if (killerUndoStack.isNotEmpty()) {
            _killerState.value = killerUndoStack.removeAt(killerUndoStack.lastIndex)
        }
    }

    // ===================== Save to DB =====================

    private fun saveGameToHistory(
        gameType: String,
        startScore: Int,
        playerNames: List<String>,
        winnerName: String,
        startTime: Long,
        playerStats: List<PlayerGameStats>,
        playerScores: Map<String, Pair<Long, Int>>,
        bestAvg: Map<String, Double>,
        count180s: Map<String, Int>,
        count140Plus: Map<String, Int>,
        count100Plus: Map<String, Int>,
        highestFinish: Int,
        winnerPlayerName: String
    ) {
        viewModelScope.launch {
            val duration = ((System.currentTimeMillis() - startTime) / 60000).toInt()
            dao.insertGame(
                GameHistoryEntity(
                    gameType = gameType,
                    startScore = startScore,
                    playerNames = gson.toJson(playerNames),
                    winnerName = winnerName,
                    date = System.currentTimeMillis(),
                    durationMinutes = duration,
                    playerStats = gson.toJson(playerStats)
                )
            )

            // Update per-player stats
            for (name in playerNames) {
                val existing = dao.getPlayerStats(name) ?: PlayerStatsEntity(playerName = name)
                val score = playerScores[name]
                val totalPts = score?.first ?: 0L
                val darts = score?.second ?: 0
                val avg = bestAvg[name] ?: 0.0
                dao.upsertPlayerStats(
                    existing.copy(
                        gamesPlayed = existing.gamesPlayed + 1,
                        gamesWon = existing.gamesWon + if (name == winnerPlayerName) 1 else 0,
                        totalDartsThrown = existing.totalDartsThrown + darts,
                        bestAverage = maxOf(existing.bestAverage, avg),
                        total180s = existing.total180s + (count180s[name] ?: 0),
                        total140Plus = existing.total140Plus + (count140Plus[name] ?: 0),
                        total100Plus = existing.total100Plus + (count100Plus[name] ?: 0),
                        highestFinish = if (name == winnerPlayerName) maxOf(existing.highestFinish, highestFinish) else existing.highestFinish,
                        totalPoints = existing.totalPoints + totalPts,
                        lastPlayed = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
