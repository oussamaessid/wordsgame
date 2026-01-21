package com.example.wordgame.data.datasource

import android.content.Context
import com.example.wordgame.domain.model.GameState
import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language

class SharedPreferencesDataSource(private val context: Context) : LocalDataSource {

    override suspend fun saveGameState(state: GameState, language: Language) {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("date", state.date)
            putString("word", state.word)
            putString("guesses", state.guesses.joinToString(","))
            putBoolean("gameOver", state.gameOver)
            putBoolean("won", state.won)
            apply()
        }
    }

    override suspend fun loadGameState(language: Language): GameState? {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        val date = prefs.getString("date", null) ?: return null
        val word = prefs.getString("word", null) ?: return null
        val guessesStr = prefs.getString("guesses", "") ?: ""
        val guesses = if (guessesStr.isEmpty()) emptyList() else guessesStr.split(",")
        val gameOver = prefs.getBoolean("gameOver", false)
        val won = prefs.getBoolean("won", false)

        return GameState(date, word, guesses, gameOver, won)
    }

    override suspend fun saveStats(stats: GameStats, language: Language) {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("totalPlayed", stats.totalPlayed)
            putInt("wins", stats.wins)
            putInt("currentStreak", stats.currentStreak)
            putInt("maxStreak", stats.maxStreak)
            apply()
        }
    }

    override suspend fun loadStats(language: Language): GameStats {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        return GameStats(
            totalPlayed = prefs.getInt("totalPlayed", 0),
            wins = prefs.getInt("wins", 0),
            currentStreak = prefs.getInt("currentStreak", 0),
            maxStreak = prefs.getInt("maxStreak", 0)
        )
    }
}
