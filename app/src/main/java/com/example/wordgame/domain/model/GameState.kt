package com.example.wordgame.domain.model

data class GameState(
    val date: String,
    val word: String,
    val guesses: List<String>,
    val gameOver: Boolean,
    val won: Boolean
)