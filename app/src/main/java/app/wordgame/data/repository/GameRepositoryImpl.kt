package app.wordgame.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class GameRepositoryImpl(
    private val localDataSource: app.wordgame.data.datasource.LocalDataSource
) : app.wordgame.domain.repository.GameRepository {

    private var englishWords: List<String> = emptyList()
    private var frenchWords: List<String> = emptyList()

    /** Background refresh of the saved lists, applied at the next launch. */
    private val refreshScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO)
    private var refreshStarted = false

    /**
     * Words are loaded once per launch: from memory if already there, else instantly from the saved
     * lists (refreshed in the background), and from the network only on the very first launch.
     */
    suspend fun loadWordsFromUrl(url: String = WORDS_URL): Boolean {
        if (englishWords.isNotEmpty() && frenchWords.isNotEmpty()) return true
        val cached = withContext(Dispatchers.IO) { localDataSource.loadWordLists() }
        if (cached != null && cached.first.isNotEmpty() && cached.second.isNotEmpty()) {
            englishWords = cached.first
            frenchWords = cached.second
            if (!refreshStarted) {
                refreshStarted = true
                // Saved only: changing the lists mid-session could change today's word.
                refreshScope.launch { runCatching { download(url) }.onSuccess { localDataSource.saveWordLists(it.first, it.second) } }
            }
            return true
        }
        return downloadAndUse(url)
    }

    private fun download(url: String): Pair<List<String>, List<String>> {
        val connection = URL(url).openConnection().apply { connectTimeout = 10000; readTimeout = 15000 }
        val json = JSONObject(connection.getInputStream().bufferedReader().use { it.readText() })
        val english = List(json.getJSONArray("english").length()) { json.getJSONArray("english").getString(it).uppercase(Locale.ROOT) }
        val french = List(json.getJSONArray("french").length()) { json.getJSONArray("french").getString(it).uppercase(Locale.ROOT) }
        check(english.isNotEmpty() && french.isNotEmpty())
        return english to french
    }

    private suspend fun downloadAndUse(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            // ✅ Étape 1 : Essayer de télécharger depuis internet
            try {
                val (newEnglish, newFrench) = download(url)

                englishWords = newEnglish
                frenchWords = newFrench

                localDataSource.saveWordLists(englishWords, frenchWords)

                true // ✅ Succès internet

            } catch (e: Exception) {
                e.printStackTrace()

                // ✅ Étape 2 : Pas d'internet → essayer le cache local
                val cached = localDataSource.loadWordLists()
                if (cached != null && cached.first.isNotEmpty() && cached.second.isNotEmpty()) {
                    englishWords = cached.first
                    frenchWords = cached.second
                    true // ✅ Succès via cache
                } else {
                    // ❌ Pas d'internet ET pas de cache → impossible de jouer
                    false
                }
            }
        }
    }

    override suspend fun saveGameState(state: app.wordgame.domain.model.GameState, language: app.wordgame.domain.model.Language) {
        localDataSource.saveGameState(state, language)
    }

    override suspend fun loadGameState(language: app.wordgame.domain.model.Language, currentDate: String): app.wordgame.domain.model.GameState? {
        val state = localDataSource.loadGameState(language)
        return if (state?.date == currentDate) state else null
    }

    override suspend fun saveStats(stats: app.wordgame.domain.model.GameStats, language: app.wordgame.domain.model.Language) {
        localDataSource.saveStats(stats, language)
    }

    override suspend fun loadStats(language: app.wordgame.domain.model.Language): app.wordgame.domain.model.GameStats {
        return localDataSource.loadStats(language)
    }

    override fun getDailyWord(language: app.wordgame.domain.model.Language, date: String): String {
        val wordList = if (language == app.wordgame.domain.model.Language.FRENCH) frenchWords else englishWords
        if (wordList.isEmpty()) return "ABCDE"

        val days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.parse(date))
        val index = Math.floorMod(days, wordList.size.toLong()).toInt()
        return wordList[index]
    }

    private companion object {
        const val WORDS_URL = "https://raw.githubusercontent.com/oussamaessid/WordsData/refs/heads/main/words.json"
    }

    override fun isValidWord(word: String, language: app.wordgame.domain.model.Language): Boolean {
        val wordList = if (language == app.wordgame.domain.model.Language.FRENCH) frenchWords else englishWords
        return wordList.contains(word.uppercase(Locale.ROOT))
    }
}
