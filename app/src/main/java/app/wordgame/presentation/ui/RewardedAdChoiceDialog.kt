package app.wordgame.presentation.ui

import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RewardedAdChoiceDialog(
    onWatchExtraTryAd: () -> Unit,
    onDismiss: () -> Unit,
    language: app.wordgame.domain.model.Language
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                text = if (language == app.wordgame.domain.model.Language.FRENCH)
                    "Dernier essai disponible ! 🎁"
                else
                    "Last try available ! 🎁",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                text = if (language == app.wordgame.domain.model.Language.FRENCH)
                    "Regardez une courte vidéo pour obtenir un essai supplémentaire. Voulez-vous continuer ?"
                else
                    "Watch a short video to get an extra try. Do you want to continue?",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onWatchExtraTryAd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(if (language == app.wordgame.domain.model.Language.FRENCH) "Regarder" else "Watch Video")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == app.wordgame.domain.model.Language.FRENCH) "Annuler" else "Cancel")
            }
        }
    )
}