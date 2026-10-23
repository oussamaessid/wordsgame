# DailyWord — production depuis un autre PC

## Version préparée

Version 1.1.3, code Android 9, identifiant inchangé `app.wordgame`.
Le code de version doit être supérieur à tous les codes déjà envoyés dans votre Play Console ; le vérifier avant un envoi.

La release conserve la réduction de code et de ressources R8. Elle exige désormais une signature explicite et la configuration Firebase existante. Les compilations debug restent indépendantes de la signature de production.

## Récupérer le projet

Installer Git et Android Studio, puis cloner :

```sh
git clone https://github.com/oussamaessid/wordsgame.git
cd wordsgame
git switch main
```

Installer Android SDK 36 dans Android Studio. Utiliser le JDK intégré à Android Studio (JDK 21 compatible avec cette configuration). Ouvrir le projet et laisser Android Studio créer `local.properties` avec le chemin SDK propre au PC. Ne pas recopier le chemin SDK du Mac.

## Configuration privée à transférer

Depuis votre machine de confiance, transférer par un canal privé :

- La clé de signature ou clé d’importation DailyWord existante, avec son alias et ses mots de passe. Ne pas générer une nouvelle clé pour mettre à jour une application existante. Avec Play App Signing, utiliser la clé d’importation enregistrée ; les APK hors Play doivent correspondre au certificat des installations qu’ils mettent à jour.
- Le fichier Firebase `app/google-services.json`, ou la valeur `firebase.apiKey` à mettre dans le `local.properties` de l’autre PC. Le projet Firebase existant est conservé. Aucun compte de service administrateur n’est nécessaire dans l’application.

Copier `keystore.properties.example` vers `keystore.properties` et remplacer les quatre valeurs localement. Le chemin `storeFile` peut être absolu ou relatif à la racine du projet. Sur Windows utiliser des `/` dans ce fichier.

La clé et les mots de passe ne doivent jamais être envoyés dans le chat ou dans Git. `keystore.properties` et les fichiers de clés sont ignorés. Pour une machine de compilation dédiée, les variables `DAILYWORD_STORE_FILE`, `DAILYWORD_STORE_PASSWORD`, `DAILYWORD_KEY_ALIAS` et `DAILYWORD_KEY_PASSWORD` remplacent les propriétés locales.

## Vérifier et produire

Windows (PowerShell) :

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
.\gradlew.bat bundleRelease assembleRelease
```

macOS / Linux :

```sh
./gradlew testDebugUnitTest lintDebug
./gradlew bundleRelease assembleRelease
```

Le premier lancement sur le nouveau PC exige Internet pour télécharger les outils et dépendances.

Fichiers attendus après succès :

- Google Play : `app/build/outputs/bundle/release/app-release.aab`.
- Installation directe : `app/build/outputs/apk/release/app-release.apk`.

Une signature ou une clé Firebase manquante arrête volontairement la compilation release avec un message explicite. Il n’existe aucun repli vers la clé debug.

## Avant la mise en production

Tester la mise à jour sur une installation existante signée avec le certificat adéquat, pour vérifier les sauvegardes. Vérifier le classement, les publicités réelles, le partage et le parcours sans connexion. Relire les exigences et formulaires affichés dans votre Play Console, notamment confidentialité, sécurité des données et publicités. Utiliser d’abord une piste de test interne et la validation Google Play.

Aucun envoi Google Play ni déploiement Firebase n’est effectué par ces commandes. La mise à disposition publique reste une action distincte.

## Vérifications de cette préparation

Les résultats de compilation figurent dans PRODUCT_IMPROVEMENTS.md. La présence d’une clé privée valide sur le futur PC ne peut pas être vérifiée depuis ce projet. Aucun artefact ne doit être présenté comme signé pour la production avant une compilation release réussie avec votre clé existante.
