package app.wordgame.domain.model

data class GuessResult(
    val guess: String,
    val letterStates: List<LetterState>,
    val isCorrect: Boolean
)