package app.wordgame.domain.usecase

class ValidateGuessUseCase {
    operator fun invoke(guess: String, targetWord: String): app.wordgame.domain.model.GuessResult {
        val letterStates = guess.mapIndexed { index, letter ->
            getLetterState(letter, index, targetWord, guess)
        }
        return app.wordgame.domain.model.GuessResult(
            guess = guess,
            letterStates = letterStates,
            isCorrect = guess == targetWord
        )
    }

    private fun getLetterState(letter: Char, position: Int, targetWord: String, guess: String): app.wordgame.domain.model.LetterState {
        if (targetWord[position] == letter) {
            return app.wordgame.domain.model.LetterState.CORRECT
        }

        if (!targetWord.contains(letter)) {
            return app.wordgame.domain.model.LetterState.WRONG
        }

        val countInTarget = targetWord.count { it == letter }
        val correctCount = guess.indices.count { i ->
            guess[i] == letter && targetWord[i] == letter
        }

        var yellowCountBefore = 0
        for (i in 0 until position) {
            if (guess[i] == letter && targetWord[i] != letter) {
                yellowCountBefore++
            }
        }

        if (correctCount + yellowCountBefore >= countInTarget) {
            return app.wordgame.domain.model.LetterState.WRONG
        }

        return app.wordgame.domain.model.LetterState.PRESENT
    }
}