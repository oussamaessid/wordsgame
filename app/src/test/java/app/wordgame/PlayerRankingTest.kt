package app.wordgame

import app.wordgame.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PlayerRankingTest {
    private val player = PlayerProfile("a", "Alice", 8)
    @Test fun duplicateWinsAndLanguagesStaySeparate() {
        val day = LocalDate.parse("2026-10-15")
        val win = PlayerScore(day.toString(), "FRENCH", true, 2500)
        val scores = listOf(win, win, PlayerScore(day.toString(), "ENGLISH", true, 9000), PlayerScore("2026-10-14", "FRENCH", false, 300))
        assertEquals(1, PlayerRanking.calculate(player, scores, day, RankingPeriod.DAY, Language.FRENCH).single().correct)
        assertEquals(2, PlayerRanking.calculate(player, scores, day, RankingPeriod.DAY, null).single().correct)
        assertEquals(11500L, PlayerRanking.calculate(player, scores, day, RankingPeriod.MONTH, null).single().durationMs)
    }
    @Test fun orderUsesWinsThenDurationThenStableId() {
        val rows = listOf(RankingEntry(player, 1, 100), RankingEntry(player.copy(id="b"), 2, 500), RankingEntry(player.copy(id="c"), 2, 200))
        assertEquals(listOf("c", "b", "a"), PlayerRanking.sort(rows).map { it.player.id })
    }
    @Test fun isoWeekKeepsItsWeekYear() {
        assertEquals("2026-W53", RankingPeriod.WEEK.id(LocalDate.parse("2027-01-01")))
        assertEquals(LocalDate.parse("2026-12-28"), RankingPeriod.weekStart("2026-W53"))
    }
    @Test fun timerUsesMonotonicClockAndFreezes() {
        val timer = GameTimer(1000, 3000, 100)
        assertEquals(2500L, timer.elapsed(600))
        assertEquals(3000L, timer.stop(1100))
        assertEquals(3000L, timer.elapsed(9999))
    }
}
