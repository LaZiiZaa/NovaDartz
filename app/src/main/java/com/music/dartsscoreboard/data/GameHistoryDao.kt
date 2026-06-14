package com.music.dartsscoreboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    @Query("SELECT * FROM game_history ORDER BY date DESC")
    fun getAllGames(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history WHERE winnerName = :playerName ORDER BY date DESC")
    fun getGamesWonBy(playerName: String): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history WHERE playerNames LIKE '%' || :playerName || '%' ORDER BY date DESC")
    fun getGamesForPlayer(playerName: String): Flow<List<GameHistoryEntity>>

    @Insert
    suspend fun insertGame(game: GameHistoryEntity)

    @Delete
    suspend fun deleteGame(game: GameHistoryEntity)

    @Query("DELETE FROM game_history")
    suspend fun deleteAllGames()

    @Query("DELETE FROM player_stats")
    suspend fun deleteAllPlayerStats()

    // Player stats
    @Query("SELECT * FROM player_stats ORDER BY gamesWon DESC")
    fun getAllPlayerStats(): Flow<List<PlayerStatsEntity>>

    @Query("SELECT * FROM player_stats WHERE playerName = :name")
    suspend fun getPlayerStats(name: String): PlayerStatsEntity?

    @Upsert
    suspend fun upsertPlayerStats(stats: PlayerStatsEntity)

    // Saved players
    @Query("SELECT * FROM saved_players ORDER BY createdAt ASC")
    fun getAllSavedPlayers(): Flow<List<SavedPlayerEntity>>

    @Insert
    suspend fun insertSavedPlayer(player: SavedPlayerEntity)

    @Delete
    suspend fun deleteSavedPlayer(player: SavedPlayerEntity)

    @Query("UPDATE saved_players SET name = :newName WHERE id = :id")
    suspend fun renameSavedPlayer(id: Long, newName: String)
}
