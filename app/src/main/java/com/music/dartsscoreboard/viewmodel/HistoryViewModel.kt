package com.music.dartsscoreboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.music.dartsscoreboard.data.AppDatabase
import com.music.dartsscoreboard.data.GameHistoryEntity
import com.music.dartsscoreboard.data.PlayerStatsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).gameHistoryDao()

    val games = dao.getAllGames().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val playerStats = dao.getAllPlayerStats().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun deleteGame(game: GameHistoryEntity) {
        viewModelScope.launch { dao.deleteGame(game) }
    }

    fun deleteAllHistory() {
        viewModelScope.launch { dao.deleteAllGames() }
    }

    fun resetAll() {
        viewModelScope.launch {
            dao.deleteAllGames()
            dao.deleteAllPlayerStats()
        }
    }
}
