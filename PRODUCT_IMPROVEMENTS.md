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
