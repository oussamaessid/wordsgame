package app.wordgame

import app.wordgame.presentation.ui.responsiveCellSize
import org.junit.Assert.*
import org.junit.Test

class ResponsiveSizingTest {
    @Test fun boardNeverOverflowsPhoneWidth() {
        for (width in listOf(240f, 280f, 320f, 360f, 411f, 600f)) {
            for (rows in 4..6) {
                val size = responsiveCellSize(width, 400f, rows, 6f)
                assertTrue("width=$width rows=$rows", size * 5 + 4 * 6 + 32 <= width + 0.01f)
            }
        }
    }
    @Test fun shortViewportsKeepReadableCellsForScrollableBoard() {
        for (height in listOf(0f, 20f, 60f, 120f)) {
            val size = responsiveCellSize(280f, height, 6, 4f)
            assertTrue(size >= 28f)
            assertTrue(size.isFinite())
        }
    }
    @Test fun boardFitsAvailableHeightWhenThereIsRoom() {
        for (height in listOf(250f, 320f, 500f, 800f)) {
            for (rows in 4..6) {
                val size = responsiveCellSize(360f, height, rows, 4f)
                assertTrue(size * rows + (rows - 1) * 4 + 32 <= height + 0.01f)
                assertTrue(size <= 52f)
            }
        }
    }
}
