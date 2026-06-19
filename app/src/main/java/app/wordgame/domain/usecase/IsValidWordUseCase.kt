package app.wordgame.domain.usecase

class IsValidWordUseCase(private val repository: app.wordgame.domain.repository.GameRepository) {
    operator fun invoke(word: String, language: app.wordgame.domain.model.Language): Boolean {
        return repository.isValidWord(word, language)
    }
}
