package com.music.dartsscoreboard.model

import com.music.dartsscoreboard.data.StatKeys

// ============================================================
// X01 (301 / 501)
// ============================================================

data class X01PlayerState(
    val player: Player,
    val scoreRemaining: Int,
    val throwHistory: List<Int> = emptyList(),
    val dartsThrown: Int = 0,
    val checkoutAttempts: Int = 0,
    val checkoutHits: Int = 0
) {
    val average: Double
        get() = if (dartsThrown == 0) 0.0
        else (throwHistory.sum().toDouble() / dartsThrown) * 3

    val bestThrow: Int
        get() = throwHistory.maxOrNull() ?: 0

    val worstThrow: Int
        get() = if (throwHistory.isEmpty()) 0 else throwHistory.min()

    val count180: Int
        get() = throwHistory.count { it == 180 }

    val count140Plus: Int
        get() = throwHistory.count { it >= 140 }

    val count100Plus: Int
        get() = throwHistory.count { it >= 100 }

    val count60Plus: Int
        get() = throwHistory.count { it >= 60 }

    val rounds: Int
        get() = throwHistory.size

    val checkoutPercentage: Double
        get() = if (checkoutAttempts == 0) 0.0
        else (checkoutHits.toDouble() / checkoutAttempts) * 100

    val highestFinish: Int
        get() = if (scoreRemaining == 0 && throwHistory.isNotEmpty()) throwHistory.last() else 0

    fun toStatsMap(): Map<String, String> = buildMap {
        put("Moyenne (3 fléchettes)", "%.1f".format(average))
        put("Meilleur tour", "$bestThrow")
        put("Pire tour", "$worstThrow")
        put("Tours joués", "$rounds")
        put("Fléchettes lancées", "$dartsThrown")
        put("180", "$count180")
        put("140+", "$count140Plus")
        put("100+", "$count100Plus")
        put("60+", "$count60Plus")
        put("Checkout %", "%.1f%%".format(checkoutPercentage))
        put("Tentatives checkout", "$checkoutAttempts")
    }

    fun toNumericMap(): Map<String, Double> = mapOf(
        StatKeys.AVERAGE to average,
        StatKeys.BEST_THROW to bestThrow.toDouble(),
        StatKeys.WORST_THROW to worstThrow.toDouble(),
        StatKeys.ROUNDS to rounds.toDouble(),
        StatKeys.DARTS to dartsThrown.toDouble(),
        StatKeys.COUNT_180 to count180.toDouble(),
        StatKeys.COUNT_140 to count140Plus.toDouble(),
        StatKeys.COUNT_100 to count100Plus.toDouble(),
        StatKeys.COUNT_60 to count60Plus.toDouble(),
        StatKeys.CHECKOUT_ATTEMPTS to checkoutAttempts.toDouble(),
        StatKeys.CHECKOUT_HITS to checkoutHits.toDouble(),
        StatKeys.HIGHEST_FINISH to highestFinish.toDouble()
    )
}

data class X01GameState(
    val gameType: GameType,
    val players: List<X01PlayerState>,
    val currentPlayerIndex: Int = 0,
    val doubleOut: Boolean = false,
    val isFinished: Boolean = false,
    val winnerIndex: Int = -1,
    val startTime: Long = System.currentTimeMillis(),
    val isTeamMode: Boolean = false,
    val turnOrder: List<Int> = emptyList()
) {
    val currentPlayer: X01PlayerState get() = players[currentPlayerIndex]
}

// ============================================================
// Cricket
// ============================================================

data class CricketPlayerState(
    val player: Player,
    val marks: Map<Int, Int> = cricketNumbers.associateWith { 0 },
    val points: Int = 0,
    val totalMarksHit: Int = 0,
    val roundsPlayed: Int = 0,
    val dartsThrown: Int = 0
) {
    fun isNumberClosed(number: Int): Boolean = (marks[number] ?: 0) >= 3
    fun allClosed(): Boolean = cricketNumbers.all { isNumberClosed(it) }

    val marksPerRound: Double
        get() = if (roundsPlayed == 0) 0.0 else totalMarksHit.toDouble() / roundsPlayed

    val closedCount: Int
        get() = cricketNumbers.count { isNumberClosed(it) }

    fun toStatsMap(): Map<String, String> = buildMap {
        put("Points", "$points")
        put("Marques totales", "$totalMarksHit")
        put("Marques/tour", "%.1f".format(marksPerRound))
        put("Numéros fermés", "$closedCount / ${cricketNumbers.size}")
        put("Tours joués", "$roundsPlayed")
        put("Fléchettes lancées", "$dartsThrown")
    }

    fun toNumericMap(): Map<String, Double> = mapOf(
        StatKeys.POINTS to points.toDouble(),
        StatKeys.MARKS to totalMarksHit.toDouble(),
        StatKeys.CLOSED to closedCount.toDouble(),
        StatKeys.ROUNDS to roundsPlayed.toDouble(),
        StatKeys.DARTS to dartsThrown.toDouble()
    )
}

data class CricketGameState(
    val players: List<CricketPlayerState>,
    val currentPlayerIndex: Int = 0,
    val isFinished: Boolean = false,
    val winnerIndex: Int = -1,
    val startTime: Long = System.currentTimeMillis(),
    val currentTurnMarks: Int = 0,
    val dartsThisTurn: Int = 0,
    val isTeamMode: Boolean = false,
    val turnOrder: List<Int> = emptyList()
) {
    val currentPlayer: CricketPlayerState get() = players[currentPlayerIndex]
    val dartsRemaining: Int get() = (3 - dartsThisTurn).coerceAtLeast(0)
}

const val CRICKET_MAX_DARTS = 3

val cricketNumbers = listOf(20, 19, 18, 17, 16, 15, 25)

// ============================================================
// Around the Clock
// ============================================================

data class ATCPlayerState(
    val player: Player,
    val currentTarget: Int = 1, // 1-20, then 25 (bull)
    val dartsThrown: Int = 0,
    val hitsPerTarget: Map<Int, Int> = emptyMap(), // darts needed per target
    val isFinished: Boolean = false
) {
    val targetsHit: Int
        get() = if (currentTarget == 25 && isFinished) 21
        else if (currentTarget == 25) 20
        else currentTarget - 1

    val accuracy: Double
        get() = if (dartsThrown == 0) 0.0
        else (targetsHit.toDouble() / dartsThrown) * 100

    val nextTargetDisplay: String
        get() = if (currentTarget == 25) "Bull" else "$currentTarget"

    fun toStatsMap(): Map<String, String> = buildMap {
        put("Cibles touchées", "$targetsHit / 21")
        put("Fléchettes lancées", "$dartsThrown")
        put("Précision", "%.1f%%".format(accuracy))
        put("Moy. fléchettes/cible", if (targetsHit == 0) "-" else "%.1f".format(dartsThrown.toDouble() / targetsHit))
    }

    fun toNumericMap(): Map<String, Double> = mapOf(
        StatKeys.TARGETS to targetsHit.toDouble(),
        StatKeys.DARTS to dartsThrown.toDouble(),
        StatKeys.ACCURACY to accuracy
    )
}

data class ATCGameState(
    val players: List<ATCPlayerState>,
    val currentPlayerIndex: Int = 0,
    val isFinished: Boolean = false,
    val winnerIndex: Int = -1,
    val startTime: Long = System.currentTimeMillis(),
    val currentDartsInTurn: Int = 0
) {
    val currentPlayer: ATCPlayerState get() = players[currentPlayerIndex]
}

// ============================================================
// Count Up
// ============================================================

data class CountUpPlayerState(
    val player: Player,
    val totalScore: Int = 0,
    val throwHistory: List<Int> = emptyList(),
    val dartsThrown: Int = 0
) {
    val rounds: Int get() = throwHistory.size
    val average: Double
        get() = if (dartsThrown == 0) 0.0
        else (totalScore.toDouble() / dartsThrown) * 3

    val bestThrow: Int get() = throwHistory.maxOrNull() ?: 0
    val worstThrow: Int get() = if (throwHistory.isEmpty()) 0 else throwHistory.min()
    val count180: Int get() = throwHistory.count { it == 180 }
    val count100Plus: Int get() = throwHistory.count { it >= 100 }

    fun toStatsMap(): Map<String, String> = buildMap {
        put("Score total", "$totalScore")
        put("Moyenne (3 fléchettes)", "%.1f".format(average))
        put("Meilleur tour", "$bestThrow")
        put("Pire tour", "$worstThrow")
        put("Tours joués", "$rounds")
        put("Fléchettes lancées", "$dartsThrown")
        put("180", "$count180")
        put("100+", "$count100Plus")
    }

    fun toNumericMap(): Map<String, Double> = mapOf(
        StatKeys.SCORE to totalScore.toDouble(),
        StatKeys.AVERAGE to average,
        StatKeys.BEST_THROW to bestThrow.toDouble(),
        StatKeys.WORST_THROW to worstThrow.toDouble(),
        StatKeys.ROUNDS to rounds.toDouble(),
        StatKeys.DARTS to dartsThrown.toDouble(),
        StatKeys.COUNT_180 to count180.toDouble(),
        StatKeys.COUNT_100 to count100Plus.toDouble()
    )
}

data class CountUpGameState(
    val players: List<CountUpPlayerState>,
    val currentPlayerIndex: Int = 0,
    val maxRounds: Int = 8,
    val isFinished: Boolean = false,
    val winnerIndex: Int = -1,
    val startTime: Long = System.currentTimeMillis()
) {
    val currentPlayer: CountUpPlayerState get() = players[currentPlayerIndex]
    val currentRound: Int
        get() {
            val minRounds = players.minOf { it.rounds }
            return minRounds + 1
        }
}

// ============================================================
// Killer
// ============================================================

data class KillerPlayerState(
    val player: Player,
    val assignedNumber: Int = 0,
    val lives: Int = 0,
    val maxLives: Int = 3,
    val isKiller: Boolean = false,
    val isEliminated: Boolean = false,
    val dartsThrown: Int = 0,
    val killCount: Int = 0
) {
    fun toStatsMap(): Map<String, String> = buildMap {
        put("Numéro", "$assignedNumber")
        put("Vies", "$lives / $maxLives")
        put("Killer", if (isKiller) "Oui" else "Non")
        put("Éliminations", "$killCount")
        put("Fléchettes lancées", "$dartsThrown")
        put("Éliminé", if (isEliminated) "Oui" else "Non")
    }

    fun toNumericMap(): Map<String, Double> = mapOf(
        StatKeys.KILLS to killCount.toDouble(),
        StatKeys.DARTS to dartsThrown.toDouble()
    )
}

data class KillerGameState(
    val players: List<KillerPlayerState>,
    val currentPlayerIndex: Int = 0,
    val phase: KillerPhase = KillerPhase.ASSIGN_NUMBERS,
    val isFinished: Boolean = false,
    val winnerIndex: Int = -1,
    val startTime: Long = System.currentTimeMillis()
) {
    val currentPlayer: KillerPlayerState get() = players[currentPlayerIndex]
    val activePlayers: List<KillerPlayerState> get() = players.filter { !it.isEliminated }
}

enum class KillerPhase {
    ASSIGN_NUMBERS, // Each player throws to get their number
    PLAYING         // Main game
}
