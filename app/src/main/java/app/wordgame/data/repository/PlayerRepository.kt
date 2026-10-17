package app.wordgame.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.SharedPreferences
import app.wordgame.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class ProfileSaveResult { SAVED, NAME_TAKEN, OFFLINE, FAILED }

/** Device-local guest store. Never represents these scores as server-verified. */
class PlayerRepository(private val prefs: SharedPreferences, val firebase: FirebasePlayers? = null) {
    init {
        if (!prefs.contains("player_id")) {
            val id = UUID.randomUUID().toString()
            check(prefs.edit().putString("player_id", id)
                .putString("player_name", "Joueur-${id.take(8)}").putInt("player_avatar", PlayerAvatars.DEFAULT).commit())
        } else if ((!prefs.getBoolean("profile_setup_complete", false) ||
            prefs.getString("player_name", "") == "Joueur-${prefs.getString("player_id", "")!!.take(8)}") &&
            !prefs.getBoolean("avatar_customized", false) && prefs.getInt("player_avatar", 8) == 8) {
            // Only migrate the old automatic avatar; preserve personalized profiles.
            check(prefs.edit().putInt("player_avatar", PlayerAvatars.DEFAULT).commit())
        }
    }
    var currentProfile by androidx.compose.runtime.mutableStateOf(profile())
        private set
    var syncMessage by androidx.compose.runtime.mutableStateOf("")
        private set
    /** True when another player already owns the current pseudo; the player must pick another one. */
    var nameTaken by androidx.compose.runtime.mutableStateOf(false)
        private set
    val needsSetup get() = !prefs.getBoolean("profile_setup_complete", false)
    fun completeSetup() { prefs.edit().putBoolean("profile_setup_complete", true).apply() }
    fun profile() = PlayerProfile(prefs.getString("player_id", "")!!,
        prefs.getString("player_name", "Joueur")!!, prefs.getInt("player_avatar", PlayerAvatars.DEFAULT).takeIf { PlayerAvatars.isValid(it) } ?: PlayerAvatars.DEFAULT)

    fun update(name: String, avatar: Int): Boolean {
        val clean = cleanName(name)
        require(isValidName(clean))
        require(PlayerAvatars.isValid(avatar))
        val saved = prefs.edit().putString("player_name", clean).putInt("player_avatar", avatar).putBoolean("avatar_customized", true).commit()
        if (saved) currentProfile = profile()
        return saved
    }
    @Synchronized
    fun start(mode: Language, day: LocalDate): Long {
        val key = "start_${day}_${mode.name}"
        if (!prefs.contains(key)) check(prefs.edit().putLong(key, System.currentTimeMillis()).commit())
        return prefs.getLong(key, System.currentTimeMillis())
    }
    @Synchronized
    fun record(mode: Language, day: LocalDate, won: Boolean, durationMs: Long): Boolean {
        val key = "score_${day}_${mode.name}"
        if (prefs.contains(key)) return false
        return prefs.edit().putString(key, JSONObject().put("day", day.toString())
            .put("mode", mode.name).put("won", won).put("duration", durationMs.coerceAtLeast(1)).toString()).commit()
    }
    fun ranking(day: LocalDate, period: RankingPeriod, mode: Language?): List<RankingEntry> {
        val scores = prefs.all.filterKeys { it.startsWith("score_") }.values.mapNotNull { value ->
            runCatching {
                val obj = JSONObject(value as String)
                PlayerScore(obj.getString("day"), obj.getString("mode"), obj.getBoolean("won"), obj.getLong("duration"))
            }.getOrNull()
        }
        return PlayerRanking.sort(PlayerRanking.calculate(profile(), scores.filter { it.durationMs in 1L..172800000L }, day, period, mode))
    }
    val globalEnabled get() = firebase?.configured == true
    private val syncLock = Mutex()

    /**
     * Pushes local wins to the global boards (Firestore, no account needed). Silent by design:
     * failures are retried at the next launch, game end or leaderboard visit.
     */
    suspend fun syncScores() {
        if (!globalEnabled) return
        syncLock.withLock {
            val profile = profile()
            if (prefs.getString("synced_profile", null) != profileSignature(profile.name, profile.avatar)) {
                try {
                    publishProfile(profile.name, profile.avatar)
                } catch (e: CancellationException) { throw e }
                catch (_: NameTakenException) {
                    // Profile chosen before names were unique: keep its scores online, ask for a new pseudo.
                    try {
                        firebase!!.write(mapOf("publicPlayers/${profile.id}" to JSONObject()
                            .put("name", JSONObject().put("stringValue", profile.name))
                            .put("avatar", JSONObject().put("integerValue", profile.avatar.toString()))))
                    } catch (e: CancellationException) { throw e }
                    catch (_: Exception) { syncMessage = "Profil enregistré ici · synchronisation en attente"; return }
                }
                catch (_: Exception) { syncMessage = "Profil enregistré ici · synchronisation en attente"; return }
            }
            val today = PlayerPeriods.today()
            // Revisit every saved period when a player comes back online.
            val days = (listOf(today) + prefs.all.filterKeys { it.startsWith("score_") }.values.mapNotNull {
                runCatching { LocalDate.parse(JSONObject(it as String).getString("day")) }.getOrNull()
            }).distinct()
            val boards = linkedMapOf<String, RankingEntry>()
            // null = general board, both languages together.
            for (mode in listOf<Language?>(null) + Language.entries) for (period in RankingPeriod.entries) {
                days.distinctBy { period.id(it) }.forEach { day ->
                    ranking(day, period, mode).firstOrNull()?.let { boards[boardId(period, mode, period.id(day))] = it }
                }
            }
            for ((board, entry) in boards) {
                val signature = "${entry.correct}:${entry.durationMs}:${profile.name}:${profile.avatar}"
                if (prefs.getString("synced_$board", null) == signature) continue
                try {
                    firebase!!.write(mapOf("leaderboards/$board/entries/${profile.id}" to JSONObject()
                        .put("name", JSONObject().put("stringValue", profile.name))
                        .put("avatar", JSONObject().put("integerValue", profile.avatar.toString()))
                        .put("correctCount", JSONObject().put("integerValue", entry.correct.toString()))
                        .put("totalMs", JSONObject().put("integerValue", entry.durationMs.toString()))))
                } catch (e: CancellationException) { throw e }
                catch (e: FirebaseRequestException) {
                    syncMessage = "Résultat enregistré ici · synchronisation en attente"; return
                } catch (_: Exception) { syncMessage = "Résultat enregistré ici · synchronisation en attente"; return }
                prefs.edit().putString("synced_$board", signature).apply()
            }
            syncMessage = "Profil synchronisé"
        }
    }
    /**
     * Saves the profile once its pseudo is reserved online. A pseudo belongs to a single player,
     * whatever the avatar: "Ali" is refused if another player already uses "ali" with another icon.
     */
    suspend fun saveGlobalProfile(name: String, avatar: Int): ProfileSaveResult {
        val clean = cleanName(name)
        if (!isValidName(clean)) return ProfileSaveResult.FAILED
        if (globalEnabled) {
            try { publishProfile(clean, avatar) }
            catch (e: CancellationException) { throw e }
            catch (_: NameTakenException) { return ProfileSaveResult.NAME_TAKEN }
            catch (_: FirebaseRequestException) { return ProfileSaveResult.FAILED }
            // Without a connection the pseudo cannot be checked, so it is not saved.
            catch (_: Exception) { return ProfileSaveResult.OFFLINE }
        }
        if (!update(clean, avatar)) return ProfileSaveResult.FAILED
        syncScores()
        return ProfileSaveResult.SAVED
    }

    private class NameTakenException : Exception()

    /**
     * Writes publicPlayers/{id} and reserves playerNames/{nameKey} in one atomic commit, releasing the
     * previous reservation. The create-only precondition makes two players unable to claim one pseudo.
     */
    private suspend fun publishProfile(name: String, avatar: Int) {
        val fb = firebase!!
        val id = profile().id
        val key = nameKey(name)
        val claimed = prefs.getString("claimed_name", null)
        val writes = mutableListOf(fb.updateWrite("publicPlayers/$id", JSONObject()
            .put("name", JSONObject().put("stringValue", name))
            .put("avatar", JSONObject().put("integerValue", avatar.toString()))
            .put("nameKey", JSONObject().put("stringValue", key))))
        if (claimed != key) {
            val owner = fb.get("playerNames/$key")?.optJSONObject("fields")
                ?.optJSONObject("playerId")?.optString("stringValue")
            if (owner != null && owner != id) { nameTaken = true; throw NameTakenException() }
            if (owner == null) writes += fb.updateWrite("playerNames/$key",
                JSONObject().put("playerId", JSONObject().put("stringValue", id)), exists = false, stamp = false)
            if (claimed != null) writes += fb.deleteWrite("playerNames/$claimed")
        }
        try { fb.commit(writes) }
        catch (e: FirebaseRequestException) {
            if (e.status == "FAILED_PRECONDITION" || e.status == "ALREADY_EXISTS") { nameTaken = true; throw NameTakenException() }
            throw e
        }
        nameTaken = false
        prefs.edit().putString("claimed_name", key).putString("synced_profile", profileSignature(name, avatar)).commit()
    }
    /**
     * The current player's position with up to [NEIGHBOURS] players before and after, or the top
     * players when they are not ranked yet. Never throws: falls back to the last cached page.
     * Each entry carries its absolute rank.
     */
    suspend fun globalRanking(range: RankingPeriod, mode: Language?): JSONObject {
        val period = range.id(PlayerPeriods.today())
        val board = boardId(range, mode, period)
        val parent = "leaderboards/$board"
        check(globalEnabled) { "Firebase non configuré" }
        return try {
            val me = profile().id
            val mine = firebase!!.get("$parent/entries/$me")?.getJSONObject("fields")
            val ranked = mine != null
            var offset = 0L
            var limit = NEIGHBOURS * 2 + 1
            if (mine != null) {
                val correct = mine.getJSONObject("correctCount").getString("integerValue").toLong()
                val total = mine.getJSONObject("totalMs").getString("integerValue").toLong()
                // Players ahead: more wins, or as many wins in less time.
                val ahead = firebase.count(parent, JSONObject().put("from", entries())
                    .put("where", compare("correctCount", "GREATER_THAN", correct))) +
                    firebase.count(parent, JSONObject().put("from", entries()).put("where", JSONObject()
                        .put("compositeFilter", JSONObject().put("op", "AND").put("filters", JSONArray()
                            .put(compare("correctCount", "EQUAL", correct))
                            .put(compare("totalMs", "LESS_THAN", total))))))
                offset = (ahead - NEIGHBOURS).coerceAtLeast(0)
                limit = (ahead - offset + 1 + NEIGHBOURS).toInt()
            }
            val query = JSONObject().put("from", entries())
                .put("orderBy", JSONArray().put(order("correctCount", "DESCENDING"))
                    .put(order("totalMs", "ASCENDING")).put(order("__name__", "ASCENDING")))
                .put("offset", offset).put("limit", limit)
            val rows = JSONArray()
            firebase.query(parent, query).forEachIndexed { index, document ->
                val fields = document.getJSONObject("fields")
                rows.put(JSONObject().put("uid", document.getString("name").substringAfterLast('/'))
                    .put("rank", offset + index + 1)
                    .put("name", fields.getJSONObject("name").getString("stringValue"))
                    .put("avatar", fields.optJSONObject("avatar")?.optString("integerValue")?.toIntOrNull() ?: PlayerAvatars.DEFAULT)
                    .put("correctCount", fields.getJSONObject("correctCount").getString("integerValue").toInt())
                    .put("totalMs", fields.getJSONObject("totalMs").getString("integerValue").toLong()))
            }
            val page = JSONObject().put("period", period).put("mode", mode?.name ?: ALL)
                .put("ranked", ranked).put("entries", rows)
            prefs.edit().putString("cache_$board", page.toString()).apply()
            page
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) {
            prefs.getString("cache_$board", null)?.let { runCatching { JSONObject(it).put("cached", true) }.getOrNull() }
                ?: throw IllegalStateException("Classement indisponible")
        }
    }
    private fun entries() = JSONArray().put(JSONObject().put("collectionId", "entries"))
    private fun compare(field: String, op: String, value: Long) = JSONObject().put("fieldFilter", JSONObject()
        .put("field", JSONObject().put("fieldPath", field)).put("op", op)
        .put("value", JSONObject().put("integerValue", value.toString())))
    private fun order(field: String, direction: String) =
        JSONObject().put("field", JSONObject().put("fieldPath", field)).put("direction", direction)

    companion object {
        /** Players shown before and after the current player. */
        const val NEIGHBOURS = 2
        const val ALL = "ALL"
        /** Collapses spaces so "Ali  Ben" and "Ali Ben" are the same pseudo. */
        fun cleanName(name: String) = name.trim().replace(Regex("\\s+"), " ")
        /** Case-insensitive pseudo key, also the playerNames document id (firestore.rules checks name.lower()). */
        fun nameKey(name: String) = cleanName(name).lowercase(Locale.ROOT)
        fun isValidName(name: String): Boolean {
            val clean = cleanName(name)
            // '/' and the reserved forms cannot be Firestore document ids.
            return clean.length in 2..24 && clean.none { it.isISOControl() || it == '/' } &&
                clean != ".." && !clean.matches(Regex("^__.*__$"))
        }
        // v2: profiles now carry nameKey, so profiles synced before are published again.
        private fun profileSignature(name: String, avatar: Int) = "v2:$name:$avatar"
        /** Firestore board id, e.g. day_HARD_2026-10-14, week_ALL_2026-W42 or month_EASY_2026-10. */
        fun boardId(period: RankingPeriod, mode: Language?, id: String) = "${period.key}_${mode?.name ?: ALL}_$id"
    }
}
