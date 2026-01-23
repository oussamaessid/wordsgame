package app.wordgame.domain.usecase

class SaveStatsUseCase(private val repository: app.wordgame.domain.repository.GameRepository) {
    suspend operator fun invoke(stats: app.wordgame.domain.model.GameStats, language: app.wordgame.domain.model.Language) {
        repository.saveStats(stats, language)
    }
}

