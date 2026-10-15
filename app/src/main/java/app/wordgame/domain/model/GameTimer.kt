package app.wordgame.domain.model

/**
 * Wall time restores a saved start; monotonic time measures this process without clock jumps.
 * Paused while the game screen is hidden, so only active play time is counted.
 */
class GameTimer(startedAtWallMs: Long, nowWallMs: Long, startedAtMonotonicMs: Long) {
    private var accumulatedMs = (nowWallMs - startedAtWallMs).coerceAtLeast(0)
    private var runningSince: Long? = startedAtMonotonicMs
    private var frozenMs: Long? = null
    fun elapsed(nowMonotonicMs: Long): Long = frozenMs ?: (accumulatedMs +
        (runningSince?.let { (nowMonotonicMs - it).coerceAtLeast(0) } ?: 0))
    fun pause(nowMonotonicMs: Long): Long {
        if (frozenMs == null) { accumulatedMs = elapsed(nowMonotonicMs); runningSince = null }
        return elapsed(nowMonotonicMs)
    }
    fun resume(nowMonotonicMs: Long) { if (frozenMs == null && runningSince == null) runningSince = nowMonotonicMs }
    fun stop(nowMonotonicMs: Long): Long = elapsed(nowMonotonicMs).also { frozenMs = it }
    fun restoreFinished(durationMs: Long) { frozenMs = durationMs.coerceAtLeast(0) }
    companion object {
        fun format(durationMs: Long): String {
            val seconds = durationMs.coerceAtLeast(0) / 1000
            return if (seconds < 3600) String.format(java.util.Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
            else String.format(java.util.Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
        }
    }
}
