package app.wordgame

import app.wordgame.domain.model.GameStats
import app.wordgame.domain.usecase.UpdateStatsUseCase
import app.wordgame.domain.usecase.ValidateGuessUseCase
import app.wordgame.domain.model.LetterState
import org.junit.Assert.*
import org.junit.Test

class DailyStatsTest {
    private val update = UpdateStatsUseCase()
    @Test fun sameDayAndOlderResultsAreNotCountedTwice() {
        val first = update(GameStats(), true, "2026-10-20")
        assertEquals(first, update(first, true, "2026-10-20"))
        assertEquals(first, update(first, false, "2026-10-19"))
    }
    @Test fun missedDayBreaksStreakAndKeepsRecord() {
        val first = update(GameStats(), true, "2026-10-20")
        val second = update(first, true, "2026-10-21")
        assertEquals(2, second.currentStreak)
        assertEquals(2, second.asOf("2026-10-22").currentStreak)
        assertEquals(0, second.asOf("2026-10-23").currentStreak)
        val afterGap = update(second, true, "2026-10-23")
        assertEquals(1, afterGap.currentStreak)
        assertEquals(2, afterGap.maxStreak)
        assertEquals(3, afterGap.totalPlayed)
    }
    @Test fun lossResetsStreakAndWinRate() {
        val result = update(update(GameStats(), true, "2026-10-20"), false, "2026-10-21")
        assertEquals(0, result.currentStreak)
        assertEquals(1, result.wins)
        assertEquals(50, result.winRate)
    }
    @Test fun legacyTotalsArePreservedWithoutInventingDates() {
        val result = update(GameStats(20, 12, 4, 7), true, "2026-10-23")
        assertEquals(21, result.totalPlayed)
        assertEquals(13, result.wins)
        assertEquals(1, result.currentStreak)
        assertEquals(7, result.maxStreak)
    }
    @Test fun tutorialExplainsActualColors() {
        assertEquals(listOf(LetterState.WRONG, LetterState.PRESENT, LetterState.WRONG, LetterState.WRONG, LetterState.CORRECT), ValidateGuessUseCase()("CARTE", "PLAGE").letterStates)
        assertTrue(ValidateGuessUseCase()("PLAGE", "PLAGE").isCorrect)
    }
}
