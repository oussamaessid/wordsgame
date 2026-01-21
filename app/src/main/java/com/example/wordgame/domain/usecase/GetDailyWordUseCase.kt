package com.example.wordgame.domain.usecase

import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.repository.GameRepository

class GetDailyWordUseCase(private val repository: GameRepository) {
    operator fun invoke(language: Language, date: String): String {
        return repository.getDailyWord(language, date)
    }
}