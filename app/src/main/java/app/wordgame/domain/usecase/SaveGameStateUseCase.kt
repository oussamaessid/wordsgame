package app.wordgame.domain.usecase

class SaveGameStateUseCase(private val repository: app.wordgame.domain.repository.GameRepository) {
    suspend operator fun invoke(state: app.wordgame.domain.model.GameState, language: app.wordgame.domain.model.Language) {
        repository.saveGameState(state, language)
    }
}
