package com.example.wordgame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.model.LetterState
import com.example.wordgame.presentation.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// HEADER COMPONENT - Avec date formatée
// ============================================================================

@Composable
fun GameHeader(
    title: String,
    subtitle: String,
    backButtonText: String,
    onBackClick: () -> Unit,
    onStatsClick: () -> Unit,
    isSmallScreen: Boolean,
    language: Language = Language.FRENCH
) {
    // ✅ Formater la date actuelle selon la langue
    val currentDate = remember {
        val sdf = if (language == Language.FRENCH) {
            SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH)
        } else {
            SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH)
        }
        sdf.format(Date())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
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
                // Bouton retour
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(if (isSmallScreen) 40.dp else 48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = backButtonText,
                        modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                    )
                }

                // Titre
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

                // Bouton statistiques
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

            // ✅ Date formatée centrée
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

// ============================================================================
// GAME GRID COMPONENT
// ============================================================================

@Composable
fun GameGrid(
    currentGuess: String,
    guesses: List<String>,
    viewModel: GameViewModel,
    cellSize: Dp,
    cellSpacing: Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    cellSpacing,
                    Alignment.CenterVertically
                )
            ) {
                repeat(GameViewModel.MAX_ATTEMPTS) { rowIndex ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(cellSpacing)
                    ) {
                        repeat(GameViewModel.WORD_LENGTH) { colIndex ->
                            val letter = when {
                                rowIndex < guesses.size ->
                                    guesses[rowIndex].getOrNull(colIndex)?.toString() ?: ""
                                rowIndex == guesses.size ->
                                    currentGuess.getOrNull(colIndex)?.toString() ?: ""
                                else -> ""
                            }

                            val state = if (rowIndex < guesses.size) {
                                viewModel.getLetterState(
                                    guesses[rowIndex][colIndex],
                                    colIndex,
                                    rowIndex
                                )
                            } else {
                                LetterState.EMPTY
                            }

                            LetterCell(letter = letter, state = state, size = cellSize)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// LETTER CELL COMPONENT
// ============================================================================

@Composable
fun LetterCell(letter: String, state: LetterState, size: Dp) {
    val backgroundColor = when (state) {
        LetterState.CORRECT -> Color(0xFF4CAF50)
        LetterState.PRESENT -> Color(0xFFFFC107)
        LetterState.WRONG -> Color(0xFF9E9E9E)
        LetterState.EMPTY -> Color.White
    }

    val borderColor = if (letter.isNotEmpty() && state == LetterState.EMPTY) {
        Color(0xFF1976D2)
    } else {
        Color(0xFFDDDDDD)
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.uppercase(),
            fontSize = when {
                size < 35.dp -> 16.sp
                size < 45.dp -> 20.sp
                size < 56.dp -> 24.sp
                else -> 28.sp
            },
            fontWeight = FontWeight.Bold,
            color = if (state == LetterState.EMPTY) Color(0xFF333333) else Color.White
        )
    }
}

@Composable
fun GameKeyboard(
    onKeyPress: (String) -> Unit,
    viewModel: GameViewModel,
    gameOver: Boolean,
    language: Language,
    currentGuessLength: Int   // ← NOUVEAU paramètre important
) {
    val layout = if (language == Language.FRENCH) {
        listOf(
            listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P"),
            listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"),
            listOf("W", "X", "C", "V", "B", "N")
        )
    } else {
        listOf(
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf("Z", "X", "C", "V", "B", "N", "M")
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
                            state = LetterState.EMPTY,
                            onClick = { onKeyPress("⌫") },
                            enabled = !gameOver,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }

            // Bouton Valider / Verify
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
                        text = if (language == Language.FRENCH) "VALIDER" else "VERIFY",
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
    state: LetterState,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (state) {
        LetterState.CORRECT -> Color(0xFF4CAF50)
        LetterState.PRESENT -> Color(0xFFFFC107)
        LetterState.WRONG -> Color(0xFF9E9E9E)
        LetterState.EMPTY -> Color.White
    }

    val textColor = when (state) {
        LetterState.CORRECT, LetterState.PRESENT, LetterState.WRONG -> Color.White
        LetterState.EMPTY -> Color.Black
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