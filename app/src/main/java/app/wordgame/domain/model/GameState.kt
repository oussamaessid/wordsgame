package app.wordgame.domain.model

data class GameState(
    val date: String,
    val word: String,
    val guesses: List<String>,
    val gameOver: Boolean,
    val won: Boolean,
    val gameStartTime: Long = 0L,
    val gameEndTime: Long = 0L
)