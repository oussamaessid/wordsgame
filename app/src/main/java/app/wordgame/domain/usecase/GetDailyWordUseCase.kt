package app.wordgame.domain.usecase
class GetDailyWordUseCase(private val repository: app.wordgame.domain.repository.GameRepository) {
    operator fun invoke(language: app.wordgame.domain.model.Language, date: String): String {
        return repository.getDailyWord(language, date)
    }
}