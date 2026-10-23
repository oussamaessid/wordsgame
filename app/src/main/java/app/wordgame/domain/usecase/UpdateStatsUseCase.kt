package app.wordgame.domain.usecase

import app.wordgame.domain.model.GameStats
import java.time.LocalDate

class UpdateStatsUseCase {
    operator fun invoke(currentStats: GameStats, won: Boolean, day: String = LocalDate.now().toString()): GameStats {
        if (currentStats.lastPlayedDate.isNotEmpty() && day <= currentStats.lastPlayedDate) return currentStats
        val consecutive = currentStats.lastPlayedDate == LocalDate.parse(day).minusDays(1).toString()
        val streak = if (won) (if (consecutive) currentStats.currentStreak + 1 else 1) else 0
        return currentStats.copy(totalPlayed = currentStats.totalPlayed + 1,
            wins = currentStats.wins + if (won) 1 else 0, currentStreak = streak,
            maxStreak = maxOf(currentStats.maxStreak, streak), lastPlayedDate = day)
    }
}
