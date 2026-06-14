package com.music.dartsscoreboard.model

data class Player(
    val name: String,
    val index: Int,
    val teamIndex: Int = -1  // -1 = pas d'équipe
)
