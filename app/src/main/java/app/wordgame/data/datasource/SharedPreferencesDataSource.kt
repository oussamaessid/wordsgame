package app.wordgame.data.datasource

import android.content.Context

class SharedPreferencesDataSource(private val context: Context) : LocalDataSource {

    override suspend fun saveGameState(state: app.wordgame.domain.model.GameState, language: app.wordgame.domain.model.Language) {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("date", state.date)
            putString("word", state.word)
            putString("guesses", state.guesses.joinToString(","))
            putBoolean("gameOver", state.gameOver)
            putBoolean("won", state.won)
            putLong("gameStartTime", state.gameStartTime)
            putLong("gameEndTime", state.gameEndTime)
            putInt("extraTriesGranted", state.extraTriesGranted) // ✅ FIX
            apply()
        }
    }

    override suspend fun loadGameState(language: app.wordgame.domain.model.Language): app.wordgame.domain.model.GameState? {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        val date = prefs.getString("date", null) ?: return null
        val word = prefs.getString("word", null) ?: return null
        val guessesStr = prefs.getString("guesses", "") ?: ""
        val guesses = if (guessesStr.isEmpty()) emptyList() else guessesStr.split(",")
        val gameOver = prefs.getBoolean("gameOver", false)
        val won = prefs.getBoolean("won", false)
        val gameStartTime = prefs.getLong("gameStartTime", 0L)
        val gameEndTime = prefs.getLong("gameEndTime", 0L)
        val extraTriesGranted = prefs.getInt("extraTriesGranted", 0) // ✅ FIX

        return app.wordgame.domain.model.GameState(
            date = date,
            word = word,
            guesses = guesses,
            gameOver = gameOver,
            won = won,
            gameStartTime = gameStartTime,
            gameEndTime = gameEndTime,
            extraTriesGranted = extraTriesGranted // ✅ FIX
        )
    }

    override suspend fun saveStats(stats: app.wordgame.domain.model.GameStats, language: app.wordgame.domain.model.Language) {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("totalPlayed", stats.totalPlayed)
            putInt("wins", stats.wins)
            putInt("currentStreak", stats.currentStreak)
            putInt("maxStreak", stats.maxStreak)
            apply()
        }
    }

    override suspend fun loadStats(language: app.wordgame.domain.model.Language): app.wordgame.domain.model.GameStats {
        val prefs = context.getSharedPreferences("WordGame_${language.code}", Context.MODE_PRIVATE)
        return app.wordgame.domain.model.GameStats(
            totalPlayed = prefs.getInt("totalPlayed", 0),
            wins = prefs.getInt("wins", 0),
            currentStreak = prefs.getInt("currentStreak", 0),
            maxStreak = prefs.getInt("maxStreak", 0)
        )
    }

    override fun saveWordLists(englishWords: List<String>, frenchWords: List<String>) {
        val prefs = context.getSharedPreferences("WordGame_WordLists", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("english_words", englishWords.joinToString(","))
            putString("french_words", frenchWords.joinToString(","))
            apply()
        }
    }

    override fun loadWordLists(): Pair<List<String>, List<String>>? {
        val prefs = context.getSharedPreferences("WordGame_WordLists", Context.MODE_PRIVATE)
        val english = prefs.getString("english_words", null) ?: return null
        val french = prefs.getString("french_words", null) ?: return null
        return Pair(
            english.split(",").filter { it.isNotEmpty() },
            french.split(",").filter { it.isNotEmpty() }
        )
    }
}