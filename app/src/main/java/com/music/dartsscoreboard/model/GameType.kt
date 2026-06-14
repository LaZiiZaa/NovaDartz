package com.music.dartsscoreboard.model

enum class GameType(val displayName: String, val description: String) {
    THREE_O_ONE("301", "Partez de 301, atteignez 0"),
    FIVE_O_ONE("501", "Partez de 501, atteignez 0"),
    CRICKET("Cricket", "Fermez 15-20 et le Bull"),
    AROUND_THE_CLOCK("Around the Clock", "Touchez 1 à 20 dans l'ordre"),
    COUNT_UP("Count Up", "Score le plus haut en 8 manches"),
    KILLER("Killer", "Éliminez vos adversaires")
}
