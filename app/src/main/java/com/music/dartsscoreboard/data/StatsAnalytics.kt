package com.music.dartsscoreboard.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/* ============================================================================
 *  Moteur d'analyse : transforme l'historique brut des parties en statistiques
 *  riches (résumé global, records, détail par joueur, par mode, head-to-head).
 *  Tout est recalculé à la volée depuis la table game_history.
 * ========================================================================== */

/** Une partie de l'historique, déjà décodée (noms + stats par joueur). */
data class ParsedGame(
    val mode: String,
    val date: Long,
    val durationMinutes: Int,
    val startScore: Int,
    val winnerName: String,
    val players: List<PlayerGameStats>
) {
    val isX01: Boolean get() = mode == "301" || mode == "501"
}

data class GlobalSummary(
    val totalGames: Int,
    val totalPlayers: Int,
    val totalDarts: Int,
    val totalPlayMinutes: Int,
    val total180: Int,
    val total140: Int,
    val total100: Int,
    val avgDurationMin: Double,
    val gamesPerMode: List<Pair<String, Int>>,
    val mostPlayedMode: String?,
    val firstPlayed: Long?,
    val lastPlayed: Long?
)

data class StatRecord(
    val title: String,
    val value: String,
    val holder: String,
    val context: String
)

data class ModeStats(
    val mode: String,
    val played: Int,
    val won: Int
) {
    val winRate: Double get() = if (played == 0) 0.0 else won.toDouble() / played * 100
}

/** Un point chronologique pour les graphiques d'évolution d'un joueur. */
data class GamePoint(
    val date: Long,
    val mode: String,
    val isWin: Boolean,
    val average: Double,
    val score: Double
)

data class PlayerAnalytics(
    val name: String,
    val played: Int,
    val won: Int,
    val totalDarts: Int,
    val total180: Int,
    val total140: Int,
    val total100: Int,
    val total60: Int,
    val bestAverage: Double,
    val avgThreeDart: Double,
    val checkoutAttempts: Int,
    val checkoutHits: Int,
    val highestFinish: Int,
    val totalKills: Int,
    val totalMarks: Int,
    val bestCountUp: Int,
    val playMinutes: Int,
    val currentStreak: Int,
    val bestWinStreak: Int,
    val favoriteMode: String?,
    val perMode: List<ModeStats>,
    val timeline: List<GamePoint>
) {
    val lost: Int get() = played - won
    val winRate: Double get() = if (played == 0) 0.0 else won.toDouble() / played * 100
    val checkoutRate: Double get() = if (checkoutAttempts == 0) 0.0 else checkoutHits.toDouble() / checkoutAttempts * 100
}

data class HeadToHead(
    val a: String,
    val b: String,
    val commonGames: Int,
    val aWins: Int,
    val bWins: Int,
    val draws: Int,
    val perMode: List<Triple<String, Int, Int>>
)

data class DartsAnalytics(
    val global: GlobalSummary,
    val records: List<StatRecord>,
    val players: List<PlayerAnalytics>,
    val parsedGames: List<ParsedGame>
) {
    val isEmpty: Boolean get() = parsedGames.isEmpty()

    fun player(name: String): PlayerAnalytics? = players.find { it.name == name }

    fun headToHead(a: String, b: String): HeadToHead {
        val common = parsedGames.filter { g ->
            g.players.any { it.playerName == a } && g.players.any { it.playerName == b }
        }
        var aWins = 0
        var bWins = 0
        var draws = 0
        val perModeA = mutableMapOf<String, Int>()
        val perModeB = mutableMapOf<String, Int>()
        for (g in common) {
            when (g.winnerName) {
                a -> { aWins++; perModeA[g.mode] = (perModeA[g.mode] ?: 0) + 1 }
                b -> { bWins++; perModeB[g.mode] = (perModeB[g.mode] ?: 0) + 1 }
                else -> draws++
            }
        }
        val modes = (perModeA.keys + perModeB.keys).toSortedSet()
        val perMode = modes.map { Triple(it, perModeA[it] ?: 0, perModeB[it] ?: 0) }
        return HeadToHead(a, b, common.size, aWins, bWins, draws, perMode)
    }
}

object StatsAnalytics {
    private val gson = Gson()
    private val statsType = object : TypeToken<List<PlayerGameStats>>() {}.type
    private val namesType = object : TypeToken<List<String>>() {}.type

    fun analyze(games: List<GameHistoryEntity>): DartsAnalytics {
        val parsed = games.mapNotNull { parse(it) }.sortedBy { it.date }

        // ---------- Résumé global ----------
        val players = parsed.flatMap { it.players.map { p -> p.playerName } }.toSortedSet()
        val totalDarts = parsed.sumOf { g -> g.players.sumOf { it.num(StatKeys.DARTS).toInt() } }
        val total180 = parsed.sumOf { g -> g.players.sumOf { it.num(StatKeys.COUNT_180).toInt() } }
        val total140 = parsed.sumOf { g -> g.players.sumOf { it.num(StatKeys.COUNT_140).toInt() } }
        val total100 = parsed.sumOf { g -> g.players.sumOf { it.num(StatKeys.COUNT_100).toInt() } }
        val totalMinutes = parsed.sumOf { it.durationMinutes }
        val perMode = parsed.groupingBy { it.mode }.eachCount().toList().sortedByDescending { it.second }

        val global = GlobalSummary(
            totalGames = parsed.size,
            totalPlayers = players.size,
            totalDarts = totalDarts,
            totalPlayMinutes = totalMinutes,
            total180 = total180,
            total140 = total140,
            total100 = total100,
            avgDurationMin = if (parsed.isEmpty()) 0.0 else totalMinutes.toDouble() / parsed.size,
            gamesPerMode = perMode,
            mostPlayedMode = perMode.firstOrNull()?.first,
            firstPlayed = parsed.firstOrNull()?.date,
            lastPlayed = parsed.lastOrNull()?.date
        )

        // ---------- Records (hall of fame) ----------
        val records = buildRecords(parsed)

        // ---------- Détail par joueur ----------
        val playerAnalytics = players.map { name -> analyzePlayer(name, parsed) }
            .sortedWith(compareByDescending<PlayerAnalytics> { it.won }.thenByDescending { it.winRate })

        return DartsAnalytics(global, records, playerAnalytics, parsed)
    }

    private fun parse(e: GameHistoryEntity): ParsedGame? {
        return try {
            val stats: List<PlayerGameStats> =
                if (e.playerStats.isNotBlank()) gson.fromJson(e.playerStats, statsType) else emptyList()
            val players = stats.ifEmpty {
                // Pas de stats détaillées : on reconstruit au moins les noms/vainqueur.
                val names: List<String> = gson.fromJson(e.playerNames, namesType)
                names.map { PlayerGameStats(it, isWinner = it == e.winnerName) }
            }
            ParsedGame(e.gameType, e.date, e.durationMinutes, e.startScore, e.winnerName, players)
        } catch (_: Exception) {
            null
        }
    }

    private fun analyzePlayer(name: String, parsed: List<ParsedGame>): PlayerAnalytics {
        val mine = parsed.filter { g -> g.players.any { it.playerName == name } }
        var played = 0; var won = 0
        var darts = 0; var c180 = 0; var c140 = 0; var c100 = 0; var c60 = 0
        var coAtt = 0; var coHit = 0; var highestFinish = 0
        var kills = 0; var marks = 0; var bestCountUp = 0
        var bestAvg = 0.0
        var weightedAvgSum = 0.0; var weightedAvgDarts = 0.0
        val perModePlayed = mutableMapOf<String, Int>()
        val perModeWon = mutableMapOf<String, Int>()
        val timeline = mutableListOf<GamePoint>()

        for (g in mine) {
            val p = g.players.first { it.playerName == name }
            val isWin = g.winnerName == name
            played++
            if (isWin) won++
            perModePlayed[g.mode] = (perModePlayed[g.mode] ?: 0) + 1
            if (isWin) perModeWon[g.mode] = (perModeWon[g.mode] ?: 0) + 1

            darts += p.num(StatKeys.DARTS).toInt()
            c180 += p.num(StatKeys.COUNT_180).toInt()
            c140 += p.num(StatKeys.COUNT_140).toInt()
            c100 += p.num(StatKeys.COUNT_100).toInt()
            c60 += p.num(StatKeys.COUNT_60).toInt()
            coAtt += p.num(StatKeys.CHECKOUT_ATTEMPTS).toInt()
            coHit += p.num(StatKeys.CHECKOUT_HITS).toInt()
            kills += p.num(StatKeys.KILLS).toInt()
            marks += p.num(StatKeys.MARKS).toInt()
            highestFinish = maxOf(highestFinish, p.num(StatKeys.HIGHEST_FINISH).toInt())
            bestCountUp = maxOf(bestCountUp, p.num(StatKeys.SCORE).toInt())

            val avg = p.num(StatKeys.AVERAGE)
            if (avg > 0.0) {
                bestAvg = maxOf(bestAvg, avg)
                val d = p.num(StatKeys.DARTS)
                if (d > 0) { weightedAvgSum += avg * d; weightedAvgDarts += d }
            }
            timeline += GamePoint(g.date, g.mode, isWin, avg, p.num(StatKeys.SCORE))
        }

        // Séries (en ordre chronologique)
        var bestStreak = 0; var run = 0
        for (g in mine) {
            if (g.winnerName == name) { run++; bestStreak = maxOf(bestStreak, run) } else run = 0
        }
        var current = 0
        for (g in mine.reversed()) {
            val win = g.winnerName == name
            if (current == 0) current = if (win) 1 else -1
            else if (current > 0 && win) current++
            else if (current < 0 && !win) current--
            else break
        }

        val perMode = perModePlayed.keys.map { ModeStats(it, perModePlayed[it] ?: 0, perModeWon[it] ?: 0) }
            .sortedByDescending { it.played }

        return PlayerAnalytics(
            name = name,
            played = played,
            won = won,
            totalDarts = darts,
            total180 = c180,
            total140 = c140,
            total100 = c100,
            total60 = c60,
            bestAverage = bestAvg,
            avgThreeDart = if (weightedAvgDarts > 0) weightedAvgSum / weightedAvgDarts else 0.0,
            checkoutAttempts = coAtt,
            checkoutHits = coHit,
            highestFinish = highestFinish,
            totalKills = kills,
            totalMarks = marks,
            bestCountUp = bestCountUp,
            playMinutes = mine.sumOf { it.durationMinutes },
            currentStreak = current,
            bestWinStreak = bestStreak,
            favoriteMode = perMode.firstOrNull()?.mode,
            perMode = perMode,
            timeline = timeline
        )
    }

    private fun buildRecords(parsed: List<ParsedGame>): List<StatRecord> {
        val out = mutableListOf<StatRecord>()

        // (joueur, valeur, mode, date) helper sur une clé numérique
        fun best(key: String, predicate: (ParsedGame) -> Boolean = { true }): Triple<PlayerGameStats, ParsedGame, Double>? {
            var bestVal = Double.NEGATIVE_INFINITY
            var bestPlayer: PlayerGameStats? = null
            var bestGame: ParsedGame? = null
            for (g in parsed) {
                if (!predicate(g)) continue
                for (p in g.players) {
                    val v = p.num(key)
                    if (v > bestVal) { bestVal = v; bestPlayer = p; bestGame = g }
                }
            }
            return if (bestPlayer != null && bestGame != null && bestVal > 0.0)
                Triple(bestPlayer, bestGame, bestVal) else null
        }

        best(StatKeys.AVERAGE)?.let { (p, g, v) ->
            out += StatRecord("Meilleure moyenne", "%.1f".format(v), p.playerName, g.mode)
        }
        best(StatKeys.HIGHEST_FINISH)?.let { (p, g, v) ->
            out += StatRecord("Plus haut finish", "${v.toInt()}", p.playerName, g.mode)
        }
        best(StatKeys.COUNT_180)?.let { (p, g, v) ->
            out += StatRecord("Plus de 180 (1 partie)", "${v.toInt()}", p.playerName, g.mode)
        }
        best(StatKeys.SCORE) { it.mode == "Count Up" }?.let { (p, _, v) ->
            out += StatRecord("Meilleur score Count Up", "${v.toInt()}", p.playerName, "Count Up")
        }
        best(StatKeys.KILLS)?.let { (p, _, v) ->
            out += StatRecord("Plus d'éliminations", "${v.toInt()}", p.playerName, "Killer")
        }
        best(StatKeys.MARKS)?.let { (p, _, v) ->
            out += StatRecord("Plus de marques", "${v.toInt()}", p.playerName, "Cricket")
        }

        // Victoire X01 la plus rapide (moins de fléchettes par le vainqueur)
        var fewest = Int.MAX_VALUE
        var fewestHolder: String? = null
        var fewestMode = ""
        for (g in parsed.filter { it.isX01 }) {
            val winner = g.players.firstOrNull { it.playerName == g.winnerName } ?: continue
            val d = winner.num(StatKeys.DARTS).toInt()
            if (d in 1 until fewest) { fewest = d; fewestHolder = g.winnerName; fewestMode = g.mode }
        }
        if (fewestHolder != null) {
            out += StatRecord("Victoire la plus rapide", "$fewest fléch.", fewestHolder, fewestMode)
        }

        // Partie la plus longue
        parsed.maxByOrNull { it.durationMinutes }?.let { g ->
            if (g.durationMinutes > 0) {
                out += StatRecord("Partie la plus longue", "${g.durationMinutes} min", g.winnerName, g.mode)
            }
        }

        return out
    }
}
