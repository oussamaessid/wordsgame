package app.wordgame.presentation.ui

/** The board can scroll vertically when readable cells cannot fit the available height. */
internal fun responsiveCellSize(width: Float, height: Float, rows: Int, gap: Float): Float {
    require(rows in 4..6)
    val byWidth = ((width - 32f - gap * 4f) / 5f).coerceAtLeast(1f)
    val byHeight = (height - 32f - gap * (rows - 1)) / rows
    return minOf(byWidth, maxOf(28f, minOf(52f, byHeight)))
}
