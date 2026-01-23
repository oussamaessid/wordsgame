package app.wordgame.domain.usecase

class LoadGameStateUseCase(private val repository: app.wordgame.domain.repository.GameRepository) {
    suspend operator fun invoke(language: app.wordgame.domain.model.Language, currentDate: String): app.wordgame.domain.model.GameState? {
        return repository.loadGameState(language, currentDate)
    }
}