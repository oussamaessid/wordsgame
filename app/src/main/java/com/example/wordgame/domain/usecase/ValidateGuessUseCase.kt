package com.example.wordgame.domain.usecase

import com.example.wordgame.domain.model.GuessResult
import com.example.wordgame.domain.model.LetterState

class ValidateGuessUseCase {
    operator fun invoke(guess: String, targetWord: String): GuessResult {
        val letterStates = guess.mapIndexed { index, letter ->
            getLetterState(letter, index, targetWord, guess)
        }
        return GuessResult(
            guess = guess,
            letterStates = letterStates,
            isCorrect = guess == targetWord
        )
    }

    private fun getLetterState(letter: Char, position: Int, targetWord: String, guess: String): LetterState {
        if (targetWord[position] == letter) {
            return LetterState.CORRECT
        }

        if (!targetWord.contains(letter)) {
            return LetterState.WRONG
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
            return LetterState.WRONG
        }

        return LetterState.PRESENT
    }
}