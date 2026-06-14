# NovaDartz 🎯

Application Android de tableau de scores de fléchettes (Jetpack Compose, Kotlin, MVVM, Room).

Modes de jeu : **X01 (301/501)**, **Cricket**, **Around the Clock**, **Count Up**, **Killer** — avec mode équipe, statistiques détaillées, historique et reprise de partie.

## Mise à jour automatique

À chaque ouverture, l'app vérifie la **dernière release GitHub** de ce dépôt. Si une version plus récente existe, elle propose à l'utilisateur de la télécharger et de l'installer directement.

### Publier une nouvelle version

1. Incrémenter la version dans `app/build.gradle.kts` :
   ```kotlin
   versionCode = 3          // +1 à chaque version
   versionName = "1.2"      // la version visible (sert à la comparaison)
   ```
2. Construire l'APK release signé :
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   $gradle = "C:\Users\1\.gradle\wrapper\dists\gradle-9.3.0-bin\79n14ral3mx1ozqr3csh2u872\gradle-9.3.0\bin\gradle.bat"
   & $gradle :app:assembleRelease
   ```
3. Publier la release (le **tag doit correspondre à la version**, ex. `v1.2`) :
   ```powershell
   gh release create v1.2 "app/build/outputs/apk/release/app-release.apk" --title "NovaDartz 1.2" --notes "Quoi de neuf…"
   ```

Les utilisateurs ayant une version antérieure seront alors invités à mettre à jour.

> ⚠️ La clé de signature (`novadartz-release.jks`) et `keystore.properties` ne sont **pas** dans le dépôt (voir `.gitignore`). Elles doivent être conservées précieusement : la **même clé** est obligatoire pour mettre à jour une app déjà installée.

## Construire en local

Le dossier contient un emoji dans son chemin, ce qui casse `./gradlew`. Utiliser le `gradle.bat` direct (Gradle 9.3.0) avec le JBR 21 d'Android Studio, comme ci-dessus.
