package com.example.wordgame.domain.model

data class GameStats(
    val totalPlayed: Int = 0,
    val wins: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0
) {
    val winRate: Int
        get() = if (totalPlayed > 0) (wins * 100 / totalPlayed) else 0
}