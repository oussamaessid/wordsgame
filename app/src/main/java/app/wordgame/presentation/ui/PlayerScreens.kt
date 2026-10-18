package app.wordgame.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wordgame.data.repository.PlayerRepository
import app.wordgame.data.repository.ProfileSaveResult
import app.wordgame.domain.model.Language
import app.wordgame.domain.model.LetterState
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun PlayerScreen(repository: PlayerRepository, ranking: Boolean, setup: Boolean = false, onBack: () -> Unit) {
    if (ranking) {
        LeaderboardScreen(repository, onBack)
        return
    }
    var name by rememberSaveable { mutableStateOf(repository.profile().name) }
    var avatar by rememberSaveable { mutableStateOf(repository.profile().avatar) }
    var error by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    BackHandler { if (!saving) onBack() }
    var edited by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(repository.currentProfile) {
        if (!edited) { name = repository.currentProfile.name; avatar = repository.currentProfile.avatar }
    }
    val valid = PlayerRepository.isValidName(name)
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!setup) FilledTonalIconButton(enabled = !saving, onClick = onBack) {
                // Icon only; the description keeps it readable by screen readers.
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
            Text(if (ranking) "Classement" else if (setup) "Bienvenue dans Words Game" else "Mon profil", style = MaterialTheme.typography.headlineMedium)
        }
        if (!ranking) {
            Text(if (setup) "Votre compte invité est créé automatiquement. Personnalisez votre profil pour rejoindre les autres joueurs."
                else "Un pseudo et un avatar pour vous retrouver dans le classement.")
            PlayerAvatar(avatar, 104.dp, Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            Text(repository.syncMessage.ifBlank { "Compte invité • conservé sur cet appareil" }, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = name, onValueChange = { name = it; edited = true; error = false },
                label = { Text("Pseudo") }, singleLine = true, isError = !valid || error, enabled = !saving,
                supportingText = { Text(if (error) saveError else "De 2 à 24 caractères, sans « / ». Pseudo + avatar uniques") },
                modifier = Modifier.fillMaxWidth())
            if (repository.nameTaken && !edited) Text("Ce pseudo est déjà utilisé avec cet avatar par un autre joueur : changez de pseudo ou d’avatar.",
                color = MaterialTheme.colorScheme.error)
            Text("Choisissez votre avatar")
            Text("48 avatars Ludo et 8 avatars classiques", style = MaterialTheme.typography.bodySmall)
            val avatarGridState = rememberLazyGridState(
                initialFirstVisibleItemIndex = AvatarGallery.displayOrder.indexOf(avatar).coerceAtLeast(0))
            LazyVerticalGrid(state = avatarGridState, columns = GridCells.Adaptive(minSize = 72.dp),
                modifier = Modifier.fillMaxWidth().height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AvatarGallery.displayOrder.size, key = { AvatarGallery.displayOrder[it] }) { position ->
                    val index = AvatarGallery.displayOrder[position]
                    Column(Modifier
                        .selectable(selected = avatar == index, enabled = !saving,
                            role = androidx.compose.ui.semantics.Role.RadioButton,
                            onClick = { avatar = index; edited = true })
                        .border(if (avatar == index) 3.dp else 1.dp,
                            if (avatar == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).padding(vertical = 8.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        PlayerAvatar(index, 56.dp)
                        Text(AvatarGallery.names[index], style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
            Button(enabled = valid && !saving, onClick = {
                saving = true
                scope.launch {
                    try {
                        when (repository.saveGlobalProfile(name, avatar)) {
                            ProfileSaveResult.SAVED -> { repository.completeSetup(); onBack() }
                            // Same pseudo is allowed with another avatar, never with the same one.
                            ProfileSaveResult.NAME_TAKEN -> { error = true; saveError = "Ce pseudo est déjà pris avec cet avatar. Changez de pseudo ou d’avatar." }
                            ProfileSaveResult.OFFLINE -> { error = true; saveError = "Connexion requise pour vérifier que le pseudo est libre." }
                            ProfileSaveResult.FAILED -> { error = true; saveError = "Enregistrement impossible." }
                        }
                    } finally { saving = false }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (saving) "Enregistrement…" else if (setup) "Créer mon profil" else "Enregistrer")
            }
            if (setup) TextButton(enabled = !saving, onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Continuer avec mon profil par défaut")
            }
            if (error) Text(saveError, color = MaterialTheme.colorScheme.error)
            Text("Profil conservé sur cet appareil. Effacer les données de l’application supprime le profil.")
        }
    }
}

@Composable
fun PlayerToolbar(repository: PlayerRepository, onProfile: () -> Unit, onRanking: () -> Unit) {
    Surface(tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextButton(onClick = onProfile, modifier = Modifier.weight(1f)) {
                PlayerAvatar(repository.currentProfile.avatar, 32.dp)
                Spacer(Modifier.width(8.dp))
                Text(repository.currentProfile.name, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = if (repository.nameTaken) MaterialTheme.colorScheme.error else androidx.compose.ui.graphics.Color.Unspecified)
                if (repository.nameTaken) Text(" · pseudo pris", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            var showHelp by rememberSaveable { mutableStateOf(false) }
            // Round tonal buttons: ranking first, then "how to play".
            FilledTonalIconButton(onClick = onRanking,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFFFF3D6),
                    contentColor = androidx.compose.ui.graphics.Color(0xFFBA8518))) {
                Icon(Icons.Rounded.EmojiEvents, "Classements")
            }
            Spacer(Modifier.width(6.dp))
            // Same blue round button as the in-game header buttons.
            IconButton(onClick = { showHelp = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2),
                    contentColor = androidx.compose.ui.graphics.Color.White)) {
                Icon(Icons.AutoMirrored.Rounded.HelpOutline, "Comment jouer ?")
            }
            if (showHelp) HowToPlayDialog(onDismiss = { showHelp = false })
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("J’ai compris") } },
        title = { Text("Comment jouer ?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Trouvez le mot du jour de 5 lettres, en français ou en anglais.")
                Text("Tapez un mot existant de 5 lettres puis appuyez sur VALIDER. Après chaque essai, les cases changent de couleur :")
                HelpExample("PLAGE", 0, LetterState.CORRECT,
                    "Vert : la lettre P est dans le mot, à la bonne place.")
                HelpExample("CARTE", 1, LetterState.PRESENT,
                    "Jaune : la lettre A est dans le mot, mais à une autre place.")
                HelpExample("MONDE", 3, LetterState.WRONG,
                    "Gris : la lettre D n’est pas dans le mot.")
                Text("Vous avez 4 essais. Regardez une vidéo pour gagner jusqu’à 2 essais en plus.")
                Text("Un nouveau mot chaque jour, pour chaque langue.")
                Text("Le chronomètre ne compte que le temps passé sur le jeu : il s’arrête quand vous quittez l’écran.")
                Text("Classement : le plus de mots trouvés d’abord, puis le temps total le plus court. Jour, semaine et mois.")
                Text("Partagez votre résultat sans dévoiler le mot !")
            }
        })
}

@Composable
private fun HelpExample(word: String, highlighted: Int, state: LetterState, caption: String) {
    // Same cells as the game grid so the colours match exactly.
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            word.forEachIndexed { index, letter ->
                LetterCell(letter.toString(), if (index == highlighted) state else LetterState.EMPTY, 34.dp)
            }
        }
        Text(caption, style = MaterialTheme.typography.bodySmall)
    }
}
