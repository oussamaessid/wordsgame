package app.wordgame.data.datasource

interface LocalDataSource {
    suspend fun saveGameState(state: app.wordgame.domain.model.GameState, language: app.wordgame.domain.model.Language)
    suspend fun loadGameState(language: app.wordgame.domain.model.Language): app.wordgame.domain.model.GameState?
    suspend fun saveStats(stats: app.wordgame.domain.model.GameStats, language: app.wordgame.domain.model.Language)
    suspend fun loadStats(language: app.wordgame.domain.model.Language): app.wordgame.domain.model.GameStats
}