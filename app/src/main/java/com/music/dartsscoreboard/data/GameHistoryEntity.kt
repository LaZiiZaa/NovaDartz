package com.music.dartsscoreboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: String,
    val startScore: Int,
    val playerNames: String,       // JSON array of names
    val winnerName: String,
    val date: Long,
    val durationMinutes: Int,
    val playerStats: String = ""   // JSON: List<PlayerGameStats>
)

@Entity(tableName = "saved_players")
data class SavedPlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val playerName: String,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalDartsThrown: Int = 0,
    val bestAverage: Double = 0.0,
    val total180s: Int = 0,
    val total140Plus: Int = 0,
    val total100Plus: Int = 0,
    val highestFinish: Int = 0,
    val bestGameScore: Int = 0,     // For Count Up
    val totalPoints: Long = 0,
    val lastPlayed: Long = 0
) {
    val winPercentage: Double
        get() = if (gamesPlayed == 0) 0.0 else (gamesWon.toDouble() / gamesPlayed) * 100

    val overallAverage: Double
        get() = if (totalDartsThrown == 0) 0.0 else (totalPoints.toDouble() / totalDartsThrown) * 3
}
