package app.wordgame.domain.usecase

class LoadStatsUseCase(private val repository: app.wordgame.domain.repository.GameRepository) {
    suspend operator fun invoke(language: app.wordgame.domain.model.Language): app.wordgame.domain.model.GameStats {
        return repository.loadStats(language)
    }
}