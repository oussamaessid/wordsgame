package com.example.wordgame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language
import com.example.wordgame.presentation.viewmodel.GameViewModel

@Composable
fun StatsDialog(
    won: Boolean,
    gameOver: Boolean,
    attempts: Int,
    targetWord: String,
    stats: GameStats,
    onDismiss: () -> Unit,
    language: Language
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 600

    val translations = if (language == Language.FRENCH) {
        mapOf(
            "victory" to "🎉 Félicitations! 🎉",
            "defeat" to "😢 Dommage!",
            "correct_word" to "Le mot était",
            "attempts" to "Tentatives",
            "play_again" to "Rejouer demain!",
            "statistics" to "Statistiques",
            "played" to "Parties",
            "win_rate" to "Victoires",
            "streak" to "Série",
            "max_streak" to "Record",
            "close" to "FERMER"
        )
    } else {
        mapOf(
            "victory" to "🎉 Congratulations! 🎉",
            "defeat" to "😢 Too bad!",
            "correct_word" to "The word was",
            "attempts" to "Attempts",
            "play_again" to "Play again tomorrow!",
            "statistics" to "Statistics",
            "played" to "Played",
            "win_rate" to "Win Rate",
            "streak" to "Streak",
            "max_streak" to "Max Streak",
            "close" to "CLOSE"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(if (isSmallScreen) 0.98f else 0.95f)
                .height(if (isSmallScreen) 450.dp else 500.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isSmallScreen) 16.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
            ) {
                Text(
                    text = if (won) translations["victory"]!! else translations["defeat"]!!,
                    fontSize = if (isSmallScreen) 20.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (won) Color(0xFF4CAF50) else Color(0xFFF44336),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${translations["correct_word"]}:",
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = targetWord,
                    fontSize = if (isSmallScreen) 24.sp else 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1976D2),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = if (isSmallScreen) 4.dp else 8.dp)
                )

                if (won) {
                    Text(
                        text = "${translations["attempts"]}: $attempts/${GameViewModel.MAX_ATTEMPTS}",
                        fontSize = if (isSmallScreen) 16.sp else 18.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (isSmallScreen) 4.dp else 8.dp),
                    color = Color(0xFFE0E0E0)
                )

                Text(
                    text = translations["statistics"]!!,
                    fontSize = if (isSmallScreen) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        title = translations["played"]!!,
                        value = stats.totalPlayed.toString(),
                        color = Color(0xFF2196F3),
                        isSmallScreen = isSmallScreen
                    )

                    StatBox(
                        title = translations["win_rate"]!!,
                        value = "${stats.winRate}%",
                        color = Color(0xFF4CAF50),
                        isSmallScreen = isSmallScreen
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        title = translations["streak"]!!,
                        value = stats.currentStreak.toString(),
                        color = Color(0xFFFFC107),
                        isSmallScreen = isSmallScreen
                    )

                    StatBox(
                        title = translations["max_streak"]!!,
                        value = stats.maxStreak.toString(),
                        color = Color(0xFFFF9800),
                        isSmallScreen = isSmallScreen
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = translations["play_again"]!!,
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSmallScreen) 45.dp else 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text(
                        text = translations["close"]!!,
                        fontSize = if (isSmallScreen) 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    color: Color,
    isSmallScreen: Boolean = false
) {
    val boxSize = if (isSmallScreen) 80.dp else 100.dp
    val valueFontSize = if (isSmallScreen) 24.sp else 32.sp
    val titleFontSize = if (isSmallScreen) 10.sp else 12.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(boxSize)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                .border(2.dp, color, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    fontSize = valueFontSize,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    fontSize = titleFontSize,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}