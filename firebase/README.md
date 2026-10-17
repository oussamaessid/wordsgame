# WordsGame — Firestore direct, comme Equal To

## Configuration active

Projet réel : `wordsgame-80893`. Package : `app.wordgame`. Base Firestore Standard `(default)` : `europe-west1`. L’application utilise son vrai `app/google-services.json`, copié dans les assets pendant le build.

La solution utilise Firestore REST, sans Firebase Auth, Cloud Functions ni activation Blaze. Firebase Hosting (gratuit, plan Spark) sert uniquement le lien de partage `https://wordsgame-80893.web.app/play` : il ouvre l’app si elle est installée (Android App Links, `hosting/.well-known/assetlinks.json`), sinon redirige vers le Play Store. Le propriétaire a explicitement approuvé les scores déclarés par le téléphone et l’absence de protection fiable de l’identité. Le profil est un invité local persistant (UUID, pseudo et avatar), avec copie publique dans Firestore. L’effacement des données de l’application perd cet invité.

`firebase.json` déploie uniquement règles et index Firestore. Le répertoire `functions/` contient l’ancienne proposition de serveur validé, non utilisée par l’application, non déployée et non référencée par la configuration courante. Ses tests ne valident pas la solution directe.

## Données

- `publicPlayers/{playerUuid}` : pseudo, avatar, `nameKey` (pseudo en minuscules) et horodatage serveur.
- `playerNames/{pseudo en minuscules}` : propriétaire (`playerId`) du pseudo. Créé avec la précondition « n’existe pas » dans le même commit que le profil : un pseudo appartient à un seul joueur, quel que soit l’avatar.
- `leaderboards/{period}_{language}_{periodId}/entries/{playerUuid}` : une entrée par joueur et classement.
- Périodes : `day`, `week` (ISO), `month`, calculées dans `Africa/Tunis`.
- Langues : `FRENCH`, `ENGLISH`, `ALL` (les deux langues additionnées).
- Tri : `correctCount` décroissant, `totalMs` croissant, identifiant du document croissant.

Une partie sauvegardée par date et langue est enregistrée une seule fois localement. La synchronisation remplace le total de la période au même emplacement : les reprises réseau ne l’incrémentent pas. Les anciennes périodes enregistrées sont aussi réessayées après une période hors connexion. Seul le temps de jeu actif compte : le chronomètre se met en pause hors de l’écran de jeu et en arrière-plan. Le classement affiche le joueur avec les 2 joueurs avant et après lui (ou le top 5 s’il n’est pas encore classé). Les résultats de plus de 48 h restent locaux et n’entrent pas dans le classement.

Les règles limitent le format, les avatars (0–55), le nombre de victoires (1/7/31 par langue, deux fois ces plafonds pour Général), les durées et l’horodatage serveur. Elles refusent les suppressions clientes, la baisse du score et un changement de temps à nombre de victoires identique. Elles ne vérifient pas qu’une victoire a réellement eu lieu et ne prouvent pas la propriété d’un UUID : les identifiants publics peuvent être usurpés et les scores peuvent être falsifiés par un client modifié. Cette limite a été explicitement acceptée par le propriétaire.

## Déploiement et build

Depuis ce dossier : `firebase deploy --only firestore,hosting --project wordsgame-80893`. Déployer les règles **avant** de publier la version de l’app qui envoie `nameKey`.

Depuis le projet Android : `./gradlew assembleDebug testDebugUnitTest` avec Java 21. Aucun SDK Firebase Android supplémentaire n’est nécessaire ; le réseau/JSON existant est réutilisé.

## Vérification

Les écritures, lectures, remplacements idempotents, le tri et les limites se vérifient avec le même accès REST non authentifié que l’application. Les données de vérification sont placées dans des périodes historiques isolées, puis supprimées via l’accès administrateur. Ne pas présenter ces données comme de vrais joueurs.
