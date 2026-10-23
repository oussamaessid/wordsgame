package app.wordgame.domain.model

data class GameStats(
    val totalPlayed: Int = 0,
    val wins: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastPlayedDate: String = ""
) {
    fun asOf(day: String): GameStats = if (lastPlayedDate.isNotEmpty() &&
        java.time.LocalDate.parse(lastPlayedDate).isBefore(java.time.LocalDate.parse(day).minusDays(1)))
        copy(currentStreak = 0) else this

    val winRate: Int
        get() = if (totalPlayed > 0) (wins * 100 / totalPlayed) else 0
}