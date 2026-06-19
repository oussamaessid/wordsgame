package app.wordgame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
//  HEADER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GameHeader(
    title: String,
    subtitle: String,
    backButtonText: String,
    onBackClick: () -> Unit,
    onStatsClick: () -> Unit,
    isSmallScreen: Boolean,
    language: app.wordgame.domain.model.Language = app.wordgame.domain.model.Language.FRENCH
) {
    val currentDate = remember {
        val sdf = if (language == app.wordgame.domain.model.Language.FRENCH) {
            SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH)
        } else {
            SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH)
        }
        sdf.format(Date())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(if (isSmallScreen) 40.dp else 48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backButtonText,
                        modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = if (isSmallScreen) 18.sp else 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1976D2),
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onStatsClick,
                    modifier = Modifier.size(if (isSmallScreen) 40.dp else 48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "📊",
                        fontSize = if (isSmallScreen) 14.sp else 20.sp
                    )
                }
            }

            Text(
                text = currentDate,
                fontSize = if (isSmallScreen) 10.sp else 12.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  GRILLE DE JEU
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GameGrid(
    currentGuess: String,
    guesses: List<String>,
    viewModel: app.wordgame.presentation.viewmodel.GameViewModel,
    cellSize: Dp,
    cellSpacing: Dp,
    maxAttempts: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(cellSpacing, Alignment.CenterVertically)
            ) {
                repeat(maxAttempts) { rowIndex ->
                    key(rowIndex, maxAttempts) {
                        GridRow(
                            rowIndex = rowIndex,
                            currentGuess = currentGuess,
                            guesses = guesses,
                            viewModel = viewModel,
                            cellSize = cellSize,
                            cellSpacing = cellSpacing,
                            isBonus = rowIndex >= app.wordgame.presentation.viewmodel.GameViewModel.BASE_ATTEMPTS
                        )
                    }
                }
            }
        }
    }
}

/** Une ligne de la grille (5 cellules). */
@Composable
private fun GridRow(
    rowIndex: Int,
    currentGuess: String,
    guesses: List<String>,
    viewModel: app.wordgame.presentation.viewmodel.GameViewModel,
    cellSize: Dp,
    cellSpacing: Dp,
    isBonus: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(cellSpacing)
    ) {
        repeat(app.wordgame.presentation.viewmodel.GameViewModel.WORD_LENGTH) { colIndex ->

            val letter: String
            val state: app.wordgame.domain.model.LetterState

            when {
                rowIndex < guesses.size -> {
                    val guess = guesses[rowIndex]
                    letter = guess.getOrNull(colIndex)?.toString() ?: ""
                    state = if (letter.isNotEmpty()) {
                        viewModel.getLetterState(guess[colIndex], colIndex, rowIndex)
                    } else {
                        app.wordgame.domain.model.LetterState.EMPTY
                    }
                }
                rowIndex == guesses.size -> {
                    letter = currentGuess.getOrNull(colIndex)?.toString() ?: ""
                    state = app.wordgame.domain.model.LetterState.EMPTY
                }
                else -> {
                    letter = ""
                    state = app.wordgame.domain.model.LetterState.EMPTY
                }
            }

            LetterCell(
                letter = letter,
                state = state,
                size = cellSize,
                isBonus = isBonus && rowIndex >= guesses.size
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CELLULE LETTRE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LetterCell(
    letter: String,
    state: app.wordgame.domain.model.LetterState,
    size: Dp,
    isBonus: Boolean = false
) {
    val backgroundColor = when (state) {
        app.wordgame.domain.model.LetterState.CORRECT -> Color(0xFF4CAF50)
        app.wordgame.domain.model.LetterState.PRESENT -> Color(0xFFFFC107)
        app.wordgame.domain.model.LetterState.WRONG   -> Color(0xFF9E9E9E)
        app.wordgame.domain.model.LetterState.EMPTY   ->
            if (isBonus) Color(0xFFF0F4FF) else Color.White
    }

    val borderColor = when {
        letter.isNotEmpty() && state == app.wordgame.domain.model.LetterState.EMPTY ->
            Color(0xFF1976D2)
        isBonus && state == app.wordgame.domain.model.LetterState.EMPTY ->
            Color(0xFFBBCCEE)
        else ->
            Color(0xFFDDDDDD)
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (letter.isNotEmpty()) {
            Text(
                text = letter.uppercase(),
                fontSize = when {
                    size < 35.dp -> 16.sp
                    size < 45.dp -> 20.sp
                    size < 56.dp -> 24.sp
                    else         -> 28.sp
                },
                fontWeight = FontWeight.Bold,
                color = if (state == app.wordgame.domain.model.LetterState.EMPTY) Color(0xFF333333) else Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CLAVIER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GameKeyboard(
    onKeyPress: (String) -> Unit,
    viewModel: app.wordgame.presentation.viewmodel.GameViewModel,
    gameOver: Boolean,
    language: app.wordgame.domain.model.Language,
    currentGuessLength: Int
) {
    val layout = if (language == app.wordgame.domain.model.Language.FRENCH) {
        listOf(
            listOf("A","Z","E","R","T","Y","U","I","O","P"),
            listOf("Q","S","D","F","G","H","J","K","L","M"),
            listOf("W","X","C","V","B","N","Ç","É","È")
        )
    } else {
        listOf(
            listOf("Q","W","E","R","T","Y","U","I","O","P"),
            listOf("A","S","D","F","G","H","J","K","L"),
            listOf("Z","X","C","V","B","N","M")
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            layout.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    row.forEach { char ->
                        KeyboardKey(
                            text = char,
                            state = viewModel.getKeyState(char),
                            onClick = { onKeyPress(char) },
                            enabled = !gameOver,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rowIndex == layout.lastIndex) {
                        KeyboardKey(
                            text = "⌫",
                            state = app.wordgame.domain.model.LetterState.EMPTY,
                            onClick = { onKeyPress("⌫") },
                            enabled = !gameOver,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val isEnabled = currentGuessLength == 5 && !gameOver

                Button(
                    onClick = { onKeyPress("ENTER") },
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) Color(0xFF1976D2) else Color(0xFFBBBBBB),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (language == app.wordgame.domain.model.Language.FRENCH) "VALIDER" else "VERIFY",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KeyboardKey(
    text: String,
    state: app.wordgame.domain.model.LetterState,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (state) {
        app.wordgame.domain.model.LetterState.CORRECT -> Color(0xFF4CAF50)
        app.wordgame.domain.model.LetterState.PRESENT -> Color(0xFFFFC107)
        app.wordgame.domain.model.LetterState.WRONG   -> Color(0xFF9E9E9E)
        app.wordgame.domain.model.LetterState.EMPTY   -> Color.White
    }

    val textColor = when (state) {
        app.wordgame.domain.model.LetterState.EMPTY -> Color.Black
        else                                        -> Color.White
    }

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}