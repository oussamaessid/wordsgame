package app.wordgame.sharing

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ShareTile { CORRECT, PRESENT, ABSENT }

/** A frozen, spoiler-free result. No solution or puzzle answer is part of this object. */
data class ShareResult(
    val game: String,
    val player: String,
    val day: String,
    val category: String,
    val won: Boolean,
    val attempts: Int,
    val maxAttempts: Int,
    val durationMs: Long?,
    val english: Boolean = false,
    val wordGame: Boolean = false,
    val grid: List<List<ShareTile>> = emptyList(),
    val avatarResource: Int = 0
) {
    val dateLabel: String get() = LocalDate.parse(day).format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT))
    /** Shown instead of the level/category: the day the game was played. */
    val gameDateLabel: String get() = if (english) "Game of $dateLabel" else "Partie du $dateLabel"
    val headline: String get() = if (wordGame) {
        if (english) { if (won) "I found today's word!" else "Today's word challenged me!" }
        else { if (won) "J’ai trouvé le mot du jour !" else "J’ai tenté le mot du jour !" }
    } else if (won) "Défi du jour réussi !" else "J’ai relevé le défi du jour !"
    val timeLabel: String get() = durationMs?.takeIf { it > 0 }?.let { app.wordgame.domain.model.GameTimer.format(it) }
        ?: if (english) "Unavailable" else "Indisponible"
    val attemptsLabel: String get() = "${if (won) attempts else "X"}/$maxAttempts"
    val text: String get() = buildString {
        appendLine(game)
        appendLine(player)
        appendLine(headline)
        appendLine(gameDateLabel)
        appendLine("${if (english) "Time" else "Temps"} : $timeLabel")
        appendLine("${if (english) "Attempts" else "Essais"} : $attemptsLabel")
        if (grid.isNotEmpty()) {
            appendLine()
            grid.forEach { row -> appendLine(row.joinToString("") { when (it) {
                ShareTile.CORRECT -> "🟩"; ShareTile.PRESENT -> "🟨"; ShareTile.ABSENT -> "⬛"
            } }) }
        }
        appendLine()
        appendLine(if (english) "Can you beat my score? Take on today’s challenge!" else "Ferez-vous mieux ? Relevez le défi du jour !")
        // Google Play shows "Open" when installed and "Install" otherwise.
        append("${if (english) "Open or install" else "Ouvrir ou installer"} $game : $PLAY_LINK")
    }

    companion object {
        const val PLAY_LINK = "https://play.google.com/store/apps/details?id=app.wordgame"
    }
}
