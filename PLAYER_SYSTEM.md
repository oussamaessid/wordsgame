# Joueurs et classements WordsGame

Profil invité local automatique, pseudo modifiable et galerie de 56 avatars (dont 48 LudoKing). Trophée accessible depuis le jeu et les résultats. Chronomètre persistant ; périodes Africa/Tunis. Classements jour, semaine ISO et mois pour Français, English et Général.

La solution active est Firestore direct, comme la version actuelle d’Equal To : pas de Blaze, Auth ni fonctions serveur. Profil public et entrées de classement sont enregistrés dans le vrai projet wordsgame-80893. Les victoires sont comptées avant le temps total. Une partie par date/langue et une entrée par période/joueur évitent les doublons ordinaires.

Les scores restent déclarés par le téléphone ; il n’y a pas de preuve serveur des victoires ni de protection fiable de la propriété des profils. Ce fonctionnement a été explicitement autorisé par l’utilisateur après présentation des limites. Détails et déploiement : firebase/README.md.
