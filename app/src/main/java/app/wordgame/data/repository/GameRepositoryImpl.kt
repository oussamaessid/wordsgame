package app.wordgame.data.repository

import kotlinx.coroutines.Dispatchers
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

    private val startDate: Date by lazy {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("01/01/2026")!!
    }

    suspend fun loadWordsFromUrl(
        url: String = "https://raw.githubusercontent.com/oussamaessid/WordsData/refs/heads/main/words.json"
    ) {
        withContext(Dispatchers.IO) {
            try {
                val jsonText = URL(url).readText()
                val json = JSONObject(jsonText)

                englishWords = List(json.getJSONArray("english").length()) { i ->
                    json.getJSONArray("english").getString(i).uppercase(Locale.getDefault())
                }

                frenchWords = List(json.getJSONArray("french").length()) { i ->
                    json.getJSONArray("french").getString(i).uppercase(Locale.getDefault())
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // fallback si erreur → mot bidon
                englishWords = listOf("APPLE")
                frenchWords = listOf("TABLE")
            }
        }
    }

    // ===============================
    // GameRepository overrides
    // ===============================
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
        val wordList = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) frenchWords else englishWords
        if (wordList.isEmpty()) return "ABCDE" // fallback sécurisé

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!
        val daysSinceStart = ((currentDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
        val index = daysSinceStart % wordList.size
        return wordList[index]
    }
}
