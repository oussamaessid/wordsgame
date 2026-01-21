package com.example.wordgame.domain.usecase

import com.example.wordgame.domain.model.GameStats

class UpdateStatsUseCase {
    operator fun invoke(currentStats: GameStats, won: Boolean): GameStats {
        return currentStats.copy(
            totalPlayed = currentStats.totalPlayed + 1,
            wins = if (won) currentStats.wins + 1 else currentStats.wins,
            currentStreak = if (won) currentStats.currentStreak + 1 else 0,
            maxStreak = if (won) maxOf(currentStats.maxStreak, currentStats.currentStreak + 1)
            else currentStats.maxStreak
        )
    }
}