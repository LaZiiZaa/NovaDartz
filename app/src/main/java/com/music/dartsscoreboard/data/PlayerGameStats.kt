package com.music.dartsscoreboard.data

data class PlayerGameStats(
    val playerName: String,
    val isWinner: Boolean = false,
    /** Affichage lisible (libellés FR -> valeurs formatées). */
    val stats: Map<String, String> = emptyMap(),
    /** Valeurs numériques structurées, pour l'agrégation/les records/graphiques. */
    val numeric: Map<String, Double> = emptyMap()
) {
    fun num(key: String): Double = numeric[key] ?: 0.0
}

/** Clés numériques standardisées partagées entre tous les modes de jeu. */
object StatKeys {
    const val DARTS = "darts"
    const val ROUNDS = "rounds"
    const val AVERAGE = "average"          // moyenne 3 fléchettes (X01, Count Up)
    const val BEST_THROW = "bestThrow"
    const val WORST_THROW = "worstThrow"
    const val SCORE = "score"              // Count Up: score total
    const val COUNT_180 = "count180"
    const val COUNT_140 = "count140"
    const val COUNT_100 = "count100"
    const val COUNT_60 = "count60"
    const val CHECKOUT_ATTEMPTS = "checkoutAttempts"
    const val CHECKOUT_HITS = "checkoutHits"
    const val HIGHEST_FINISH = "highestFinish"
    const val POINTS = "points"            // Cricket
    const val MARKS = "marks"              // Cricket: marques totales
    const val CLOSED = "closed"            // Cricket: numéros fermés
    const val TARGETS = "targets"          // Around the Clock
    const val ACCURACY = "accuracy"        // ATC: précision %
    const val KILLS = "kills"              // Killer
}
