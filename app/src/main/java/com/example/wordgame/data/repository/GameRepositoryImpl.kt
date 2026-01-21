package com.example.wordgame.data.repository

import com.example.wordgame.data.datasource.LocalDataSource
import com.example.wordgame.domain.model.GameState
import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.repository.GameRepository
import java.text.SimpleDateFormat
import java.util.*

class GameRepositoryImpl(
    private val localDataSource: LocalDataSource
) : GameRepository {

    private val frenchWords = listOf(
        // Janvier 2026
        "ARBRE", "LIVRE", "TABLE", "CHAUD", "FROID", "PLAGE", "MONDE", "TEMPS", "AMOUR", "JOUER",
        "POMME", "FRUIT", "FLEUR", "ROUGE", "JAUNE", "BLANC", "PORTE", "LAMPE", "TERRE", "PLUIE",
        "NEIGE", "GLACE", "SUCRE", "HUILE", "LUNDI", "MARDI", "MERCI", "GRAND", "PETIT", "LARGE",
        "AVION",
        // Février 2026
        "TRAIN", "ECOLE", "MALIN", "GENRE", "SAUCE", "DANSE", "RICHE", "JUSTE", "BOIRE", "BELLE",
        "CARTE", "PHONE", "MUSIC", "VILLE", "CHIEN", "CHAIR", "SPORT", "HABIT", "NOIRE", "AUTRE",
        "VENIR", "CHOSE", "FEMME", "HOMME", "ENFANT", "MAISON", "SOLEIL", "LUNE",
        // Mars 2026
        "ETOILE", "CIEL", "NUAGE", "VENT", "FEUILLE", "ROUTE", "PISTE", "PLUME", "DOIGT", "COEUR",
        "FORCE", "CALME", "JOLIE", "BRAVE", "AIGRE", "DROIT", "GAUCHE", "HEURE", "MINUTE", "CYCLE",
        "ETUDE", "LIVRE", "CONTE", "REVER", "PENSER", "AGIR", "DANSER", "PARLER", "ECRIRE", "LIRE",
        "VIVRE",
        // Avril 2026
        "PAUSE", "REPOS", "EVEIL", "MATIN", "SOIR", "NUIT", "MIDI", "DEBUT", "FINAL", "BONNE",
        "DOUX", "DUR", "LOURD", "LEGER", "RAPIDE", "LENT", "VITE", "TARD", "TOT", "HIER",
        "DEMAIN", "AVANT", "APRES", "ENTRE", "PARMI", "CONTRE", "POUR", "SANS", "AVEC", "CHEZ",
        // Mai 2026
        "VERS", "DEVANT", "DERRIERE", "DESSUS", "DESSOUS", "DEDANS", "DEHORS", "AUTOUR", "PROCHE", "LOIN",
        "HAUTE", "BASSE", "PLEINE", "VIDE", "PLAT", "ROND", "CARRE", "OVALE", "MINCE", "GROS",
        "JEUNE", "VIEUX", "NEUF", "ANCIEN", "FRAIS", "SEC", "HUMIDE", "CHAUD", "FROID", "TIEDE",
        "DOUX",
        // Juin 2026
        "AIGRE", "AMER", "SALE", "SUCRE", "FADE", "EPICE", "FORT", "FAIBLE", "BON", "MAUVAIS",
        "BEAU", "LAID", "PROPRE", "SALE", "NET", "FLOU", "CLAIR", "SOMBRE", "BRILLANT", "MAT",
        "LISSE", "RUGUEUX", "MOUS", "DUR", "SOUPLE", "RAIDE", "OUVERT", "FERME", "LIBRE", "PRIS",
        // Juillet 2026 et au-delà
        "PLEIN", "CREUX", "ETROIT", "LARGE", "LONG", "COURT", "PROFOND", "SURFACE", "SIMPLE", "COMPLEXE"
    )

    private val englishWords = listOf(
        // January 2026
        "APPLE", "BRAIN", "CHAIR", "DREAM", "EARTH", "FLAME", "GRAPE", "HEART", "JUICE", "KNIFE",
        "LEMON", "MONEY", "NIGHT", "OCEAN", "PEACE", "QUEEN", "RIVER", "SNAKE", "TIGER", "VOICE",
        "WATER", "YACHT", "ZEBRA", "ALBUM", "BEACH", "CLOUD", "DANCE", "EAGLE", "FROST", "GHOST",
        "HOUSE",
        // February 2026
        "IMAGE", "JOKER", "LIGHT", "MUSIC", "NORTH", "PARTY", "QUIET", "ROBOT", "SMILE", "TRAIN",
        "UNION", "VALUE", "WORLD", "YOUTH", "ANGEL", "BREAD", "CLOCK", "DRINK", "FRUIT", "GLOVE",
        "HAPPY", "IDEAL", "JEWEL", "KINGS", "LORDS", "MAGIC", "NOBLE", "OMEGA",
        // March 2026
        "PIANO", "QUEST", "ROYAL", "STORM", "THEME", "ULTRA", "VITAL", "WAVES", "XENON", "YIELDS",
        "ZONES", "ABODE", "BLADE", "CHARM", "DELTA", "EMBER", "FABLE", "GRACE", "HAVEN", "IVORY",
        "JOLLY", "KARMA", "LUNAR", "METAL", "NOVEL", "ORBIT", "PRISM", "QUILT", "REALM", "SOLAR",
        "TIDAL",
        // April 2026
        "URBAN", "VIVID", "WRIST", "XENON", "YOUNG", "ZESTY", "ADAPT", "BLOOM", "CRAFT", "DRIFT",
        "ETHIC", "FRANK", "GLOBE", "HERBS", "INDEX", "JOINT", "KNACK", "LASER", "MODAL", "NICHE",
        "OXIDE", "PIXEL", "QUOTA", "RATIO", "SCENE", "TEMPO", "UNITY", "VAGUE", "WEARY", "XRAYS",
        // May 2026
        "YIELD", "ZONES", "AGREE", "BLUNT", "CRISP", "DENSE", "ELECT", "FOCUS", "GRASP", "HASTE",
        "INPUT", "JUMPS", "KIOSK", "LOGIC", "MERIT", "NORMS", "OCCUR", "PHASE", "QUEST", "REACH",
        "SCOPE", "TOUCH", "UNIFY", "VOCAL", "WASTE", "XEROX", "YELLS", "ZILCH", "ATOMS", "BASIN",
        "CRUMB",
        // June 2026
        "DWELL", "ENDOW", "FIXES", "GLAND", "HELIX", "INLET", "JOUST", "KNOBS", "LEASE", "MODEM",
        "NAVAL", "OZONE", "PLANK", "QUAKE", "RANCH", "SHAFT", "THORN", "USHER", "VAULT", "WAGON",
        "XYLEM", "YEAST", "ZONAL", "ABACK", "BADGE", "CABIN", "DAISY", "ELBOW", "FLARE", "GRILL",
        // July 2026 and beyond
        "HEDGE", "IMAGE", "JEANS", "KNEAD", "LUNAR", "MAPLE", "NERVE", "OASIS", "PLUME", "QUILT"
    )

    private val startDate: Date by lazy {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("01/01/2026")!!
    }

    override suspend fun saveGameState(state: GameState, language: Language) {
        localDataSource.saveGameState(state, language)
    }

    override suspend fun loadGameState(language: Language, currentDate: String): GameState? {
        val state = localDataSource.loadGameState(language)
        return if (state?.date == currentDate) state else null
    }

    override suspend fun saveStats(stats: GameStats, language: Language) {
        localDataSource.saveStats(stats, language)
    }

    override suspend fun loadStats(language: Language): GameStats {
        return localDataSource.loadStats(language)
    }

    override fun getDailyWord(language: Language, date: String): String {
        val wordList = if (language == Language.FRENCH) frenchWords else englishWords

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!
        val daysSinceStart = ((currentDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()

        val index = daysSinceStart % wordList.size

        return wordList[index]
    }
}