package app.wordgame.domain.usecase


class UpdateStatsUseCase {
    operator fun invoke(currentStats: app.wordgame.domain.model.GameStats, won: Boolean): app.wordgame.domain.model.GameStats {
        return currentStats.copy(
            totalPlayed = currentStats.totalPlayed + 1,
            wins = if (won) currentStats.wins + 1 else currentStats.wins,
            currentStreak = if (won) currentStats.currentStreak + 1 else 0,
            maxStreak = if (won) maxOf(currentStats.maxStreak, currentStats.currentStreak + 1)
            else currentStats.maxStreak
        )
    }
}