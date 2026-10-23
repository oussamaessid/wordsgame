package app.wordgame

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import app.wordgame.data.datasource.SharedPreferencesDataSource
import app.wordgame.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DailyPersistenceTest {
    @Test fun terminalSaveIsAtomicIdempotentAndArchived() = runBlocking {
        // Isolated preference files: never clear real player data.
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : android.content.ContextWrapper(target) {
            override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences("test_daily_$name", mode)
        }
        context.getSharedPreferences("WordGame_fr", Context.MODE_PRIVATE).edit().clear().commit()
        val local = SharedPreferencesDataSource(context)
        val state = GameState("2026-10-20", "PLAGE", listOf("CARTE", "PLAGE"), true, true)
        local.saveGameState(state, Language.FRENCH)
        local.saveGameState(state, Language.FRENCH)
        assertEquals(1, local.loadStats(Language.FRENCH).totalPlayed)
        assertEquals(1, local.loadStats(Language.FRENCH).wins)
        val reopened = SharedPreferencesDataSource(context)
        assertEquals(state.guesses, reopened.loadGameState(Language.FRENCH)?.guesses)
        reopened.saveGameState(state.copy(date = "2026-10-22", gameOver = false, guesses = emptyList()), Language.FRENCH)
        assertEquals(state.date, reopened.history(Language.FRENCH).single().date)
        assertEquals(state.word, reopened.history(Language.FRENCH).single().word)
    }
    @Test fun legacyTerminalResultMigratesWithoutIncrementingTotals() = runBlocking {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : android.content.ContextWrapper(target) {
            override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences("test_legacy_$name", mode)
        }
        context.getSharedPreferences("WordGame_fr", 0).edit().clear().putString("date", "2026-10-20")
            .putString("word", "PLAGE").putString("guesses", "PLAGE").putBoolean("gameOver", true)
            .putBoolean("won", true).putInt("totalPlayed", 10).putInt("wins", 8).commit()
        val local = SharedPreferencesDataSource(context)
        local.saveGameState(local.loadGameState(Language.FRENCH)!!, Language.FRENCH)
        assertEquals(10, local.loadStats(Language.FRENCH).totalPlayed)
        assertEquals(8, local.loadStats(Language.FRENCH).wins)
        assertEquals(1, local.history(Language.FRENCH).size)
    }
}
