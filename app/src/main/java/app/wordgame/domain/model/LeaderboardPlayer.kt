package app.wordgame.domain.model

data class LeaderboardPlayer(val uid: String, val name: String, val avatar: Int, val wins: Int, val durationMs: Long)
