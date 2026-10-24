# DailyWord — améliorations produit

## Analyse de l’existant

Application Android native Kotlin/Jetpack Compose, nom Android déjà DailyWord mais anciens écrans et partages « WORD GAME » / « Words Game ». Interface principalement française, défi disponible en français et anglais. Quatre essais, deux essais supplémentaires possibles via publicité récompensée, classement Firebase, chronomètre actif, partage texte et image déjà implémentés.

La sauvegarde SharedPreferences conservait seulement la dernière partie par langue. Le dictionnaire était déjà mis en cache, mais son chargement précédait la restauration des résultats. Les statistiques incrémentaient une série à chaque victoire sans date de référence. L’accueil servait uniquement à choisir la langue. L’aide était statique.

## Changements réalisés

- Nom DailyWord harmonisé sur les écrans et dans le résultat partagé. Identifiant Android, fichiers de préférences et profil conservés.
- Tutoriel interactif guidé en trois étapes : lancement de CARTE, explication des trois couleurs, résolution avec PLAGE. Même validateur et mêmes cases que le jeu. Aucun accès au défi, aux statistiques, au classement ou aux publicités depuis l’entraînement.
- Choix de terminer/passer conservé sur l’appareil dans une nouvelle clé, sans effacer les autres préférences. Tutoriel réaccessible dans « Comment jouer », depuis l’accueil et la barre existante. Publicité d’ouverture désactivée pour le premier parcours.
- Accueil avec défi daté, langue, état disponible/commencé/terminé et action Jouer/Reprendre/Consulter le résultat. Écran défilant et boutons d’au moins 48 dp pour l’accueil et le tutoriel. Actualisation de la date à minuit selon le fuseau existant Africa/Tunis.
- Parties, nombre de victoires, pourcentage de réussite, série actuelle de victoires et record affichés par langue. Une défaite ou un jour manqué rompt la série. La victoire d’hier garde sa série tant que le défi d’aujourd’hui reste disponible.
- Date du dernier résultat comptabilisé persistée. Rejouer la sauvegarde du même jour ou d’une date antérieure ne rajoute aucune partie. Résultat terminal, archive et statistiques sont écrits dans le même commit SharedPreferences.
- Migration de la dernière partie terminée existante depuis l’accueil : archivage sans recompter les totaux historiques. Les totaux et records anciens restent conservés ; aucune date historique inventée.
- Archive locale datée des résultats terminés, séparée par langue ; accès depuis l’accueil sans réseau, même après remplacement de la partie courante. La dernière partie terminée se restaure aussi sans charger le dictionnaire. La cible d’une partie sauvegardée reste stable en cas de mise à jour du dictionnaire.
- Partage existant texte/image conservé, avec grille de couleurs sans lettres, essais, date, temps et invitation à relever le défi. Le lien Google Play déjà présent dans ShareResult.PLAY_LINK est réutilisé ; aucun nouveau lien inventé. Aucun partage automatique.
- Protection supplémentaire contre un retour publicitaire tardif qui ajouterait un essai à une partie déjà terminée.

## Vérifications

- Compilation debug et compilation des tests Android réussies.
- 16 tests unitaires réussis : partage, classement, statistiques datées, jours manqués, doublons, défaite, conservation des anciens totaux et couleurs du tutoriel.
- Première passe sur emulator-5554 : 5 tests Android réussis (tutoriel complet, passage, accueil, sauvegarde idempotente/archivage/restauration et migration historique).
- Tests de persistance isolés dans des préférences de test : aucune suppression des données du joueur.
- Lancement Gradle des tests connectés bloqué en mode hors connexion par une dépendance UTP non mise en cache. Contournement : installation des deux APK sur l’émulateur puis exécution directe du runner Android, avec succès.
- Vérification finale : 6 tests Android réussis sur emulator-5554, dont le parcours complet avec une surface de 280 × 360 dp.
- `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline` : BUILD SUCCESSFUL. 16 tests unitaires sans échec ; analyse lint terminée avec succès (0 erreur, 50 avertissements et 2 conseils : modernisation Kotlin/Gradle, ressources, icônes et adaptations de configuration ; rapport dans `app/build/reports/lint-results-debug.html`).
- `git diff --check` : aucune erreur d’espacement.
- APK final : `app/build/outputs/apk/debug/app-debug.apk`.
- Aucune publication sur les boutiques ; APK debug de test uniquement.

## Limites et prochaines étapes

- L’ancienne version écrasait les résultats antérieurs : seuls la dernière partie encore sauvegardée et les nouveaux résultats peuvent être archivés. Les anciennes séries sans date ne peuvent pas être reconstituées avec certitude ; les totaux et records sont préservés et les nouvelles séries deviennent datées.
- L’entraînement propose deux essais guidés, sans saisie libre. Les mots d’exemple ne sont pas des tentatives sur le défi réel, même si un dictionnaire contient aussi ces mots.
- L’accueil et l’entraînement utilisent le français principal ; le jeu et le partage conservent la traduction anglaise existante. Une internationalisation complète via ressources Android reste une prochaine étape.
- Le premier téléchargement du dictionnaire exige toujours une connexion ; les résultats archivés n’en exigent pas. Les résultats restent sur cet appareil, sans synchronisation multi-appareils ajoutée.
- Le lien Google Play provient du code existant ; sa disponibilité publique n’a pas été vérifiée dans cette tâche.
- À compléter avant diffusion publique : recette sur plusieurs tailles physiques/polices d’accessibilité, parcours d’annonces récompensées et synchronisation réelle du classement, test prolongé du changement de jour, rétention et sauvegarde des archives à long terme.

## Préparation de production sur un autre PC

- Version Android portée à 1.1.3 (code 9), à comparer avec le dernier code utilisé dans Play Console avant envoi.
- Signature release configurable via `keystore.properties` privé ou variables d’environnement. Aucun certificat debug utilisé pour une release.
- Clés privées et configuration de signature exclues de Git ; modèle sans secrets fourni dans `keystore.properties.example`.
- Guide Windows/macOS/Linux dans `PRODUCTION.md` : SDK, Firebase, transfert privé de la clé existante, génération AAB et APK, conservation des données lors d’une mise à jour.
- Vérification du garde-fou `:app:preReleaseBuild` : échec attendu avec « Signature de production absente ». Aucun APK/AAB signé de production créé, aucune publication.
- Vérification après changement de version : `testDebugUnitTest assembleDebug --offline` réussi, 16 tests unitaires sans échec.

## Refonte visuelle de l’accueil — 24 octobre 2026

- Fond clair bleuté, typographie hiérarchisée, carte principale en dégradé bleu avec état et date lisibles, cinq cases décoratives sans indice sur la solution, bouton d’action blanc très visible.
- Choix de langue en contrôle segmenté, mémorisé immédiatement sur l’appareil.
- Statistiques en trois cartes et série dans un encart distinct, aide en carte et historique en lignes tactiles.
- Écran toujours défilant, états du défi et accès aux résultats conservés. Aucun changement des sauvegardes ou des règles du jeu.
- Compilation debug et tests unitaires réussis. Test Android ajouté pour l’accès au défi et à l’aide dans une surface de 280 × 420 dp.
- Vérification visuelle sur émulateur limitée : le système Android affiche « System UI isn’t responding » et le runner s’arrête avant les tests. Les nouveaux tests d’interface sont compilés, mais leur exécution n’est pas validée sur cette passe.

## Accueil fixe et bilingue — 24 octobre 2026

- Suppression du défilement vertical de l’accueil, de sa carte « Comment jouer » et de sa section « Vos derniers défis », à la demande du joueur.
- Mise en page compacte selon la hauteur disponible : les éléments décoratifs et textes secondaires disparaissent sur les petits écrans pour garder la langue, le défi, son bouton et les statistiques visibles.
- Tous les textes de l’accueil, états, actions, statistiques et dates passent en anglais ou français immédiatement selon la langue choisie ; choix toujours mémorisé.
- Archives et sauvegardes existantes conservées. L’aide reste accessible dans la barre supérieure existante ; l’ancien historique n’est plus présenté sur l’accueil.
- Tests d’interface adaptés : absence de défilement, bouton Jouer accessible sur 280 × 420 dp, bascule français/anglais et absence des deux sections retirées.
- Vérification finale : compilation APK et tests Android réussie ; 16 tests unitaires réussis. Les tests d’interface adaptés sont compilés, sans nouvelle exécution sur l’émulateur instable décrit ci-dessus.

## Adaptation aux tailles de téléphone — 24 octobre 2026

- Accueil : organisation en colonnes en portrait et côte à côte en paysage. Mode minimal lorsque la hauteur est très réduite. Toujours sans défilement vertical et toujours bilingue.
- Avec une très grande police et peu de place, les statistiques restent accessibles dans une fenêtre via un bouton de 48 dp, plutôt que de repousser le bouton Jouer hors de l’écran.
- Jeu : dimensionnement des cases selon la largeur et la hauteur réellement disponibles. Taille minimale lisible et défilement local de la grille si les six lignes ne peuvent pas tenir. Plus de taille négative lorsque le clavier prend la majeure partie de la hauteur.
- Paysage : grille et clavier côte à côte, en-tête réduit. Sur les petits portraits, touche Valider intégrée au clavier. Hauteur des touches portée à 48 dp ; description d’accessibilité conservée. Les lettres restent ajustées à leurs cases.
- Suppression du double espacement de barre de navigation autour du clavier. Barre de profil affichée à l’accueil, espace de jeu réservé à la grille/clavier. Profil et classement restent accessibles depuis l’accueil, et classement depuis les résultats.
- Rotation : écran de jeu et langue conservés avec l’état sauvegardable Compose, au lieu de revenir automatiquement à l’accueil.
- Résultats : statistiques en colonnes de largeur flexible et de hauteur extensible ; bouton Fermer de 48 dp minimum. Fenêtres et aide restent défilantes pour le texte agrandi. Cases de tutoriel adaptées à la largeur réelle.
- Vérifications initiales : 19 tests unitaires réussis (dont trois tests de dimensionnement avec 4 à 6 lignes, largeurs 240 à 600 dp et hauteurs très faibles). Neuf tests sur émulateur réussis, incluant six formats d’accueil (240 × 320 à 800 × 360 dp), polices jusqu’à 200 %, clavier français/anglais, tutoriel et sauvegardes.
- Les contraintes extrêmes sont traitées par une présentation compacte ou un défilement local dans le jeu et les fenêtres. Cette recette ne remplace pas les essais sur chaque appareil physique, téléphone pliant ou configuration constructeur.

## « Comment jouer » centré et ouverture initiale — 24 octobre 2026

- Tutoriel présenté dans une carte blanche centrée sur un fond doux, largeur limitée, coins arrondis et indicateur de progression en trois étapes. Sur les petites hauteurs, le contenu peut défiler à l’intérieur de la carte.
- Exemples interactifs avec les mêmes règles de coloration que le jeu : CARTE / PLAGE en français, TRAIN / PLANT en anglais. Légendes explicites en plus des couleurs.
- Ouverture automatique au premier lancement conservée. Le choix terminé/passé est mémorisé ; aucune réinitialisation des choix ou statistiques des joueurs existants.
- À la première utilisation, le bouton final ouvre directement le défi quotidien. Passer revient à l’accueil. Le tutoriel reste accessible depuis l’icône d’aide ; sa langue suit le choix de langue enregistré.
- Aucune publicité dans le tutoriel ni publicité d’ouverture avant le premier tutoriel.
- Tests ajoutés : ouverture initiale et persistance du choix avec des préférences isolées ; tutoriel anglais et action de démarrage de partie.
- Un test supplémentaire a détecté un débordement de l’accueil à 200 % sur 280 × 420 dp : les éléments décoratifs ont été supprimés dans ce mode pour rendre les statistiques accessibles.
- Vérification finale combinée : `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline` réussi, 19 tests unitaires et 12 tests sur émulateur réussis. Le test à 200 % précédemment en échec passe après correction.
- Vérification visuelle réelle : tutoriel centré contrôlé sur emulator-5554 ; aperçu local dans `app/build/outputs/previews/tutorial-modern.png`. Aucune suppression des données de l’application pour simuler une réinstallation : le test de premier lancement utilise ses propres préférences isolées.
