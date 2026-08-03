# Nuits Spéciales — Cobbleverse (cvnights)

Mod Fabric 1.21.1 pour votre pack (Cobbleverse 1.7.42, Enhanced Celestials 6.0.2.5, Cobblemon Integrations 1.1.6).

État : compile et fonctionne en jeu (testé). Ce zip inclut toutes les corrections trouvées pendant
les premiers tests, plus le support multi-dimension à état global.

## Contenu

### Datapack embarqué (dans `src/main/resources/data/` et `assets/`)
- **5 nuits spéciales** (`data/cvnights/enhancedcelestials/lunar/event/`) : Lune Bleue (x2 shiny),
  Lune des Sages (+50% XP), Lune Boréale (+75% rare/ultra-rare), Lune Bénie (+5 IV mini), Lune du
  Chasseur (+15% capture). Teinte de ciel/lune discrète, fréquence ~1 nuit sur 10 au total, jamais
  deux fois la même à moins de 9 nuits d'écart.
- **3 entrées `pokemon_lunar_event`** qui pilotent shiny/XP/IV via le pont déjà présent dans Cobblemon
  Integrations.
- **Nuits d'origine d'Enhanced Celestials désactivées** (Lune de Sang, sa propre Lune Bleue, Lune des
  Moissons, et les 4 versions "Super") : écrasées avec un `dimension_chances` vide, donc seules vos 5
  nuits peuvent se produire.
- Recettes de craft pour la montre et le bonbon.

### Multi-dimension : un seul état global, pas 3 tirages indépendants
Le tirage/suivi de la nuit spéciale reste uniquement dans l'Overworld (c'est la seule dimension
qu'Enhanced Celestials suit nativement). Mais **tout le code du mod ignore volontairement la
dimension appelante** et consulte toujours l'état de l'Overworld :
- `NightState` (le point d'entrée unique) n'a plus de paramètre "dimension" — il regarde toujours
  l'Overworld, peu importe où se trouve le joueur qui consulte l'info.
- `CaptureBoostListener` et `RarityBoostListener` (les effets de capture/rareté) s'appliquent donc
  identiquement au Nether et dans l'End, au même moment que dans l'Overworld.
- La montre (`NightWatchItem`) affiche la même info partout.
- **`NightAnnouncer`** (nouveau) : Enhanced Celestials ne notifie nativement que les joueurs présents
  dans l'Overworld quand une nuit démarre/se termine. Cette classe surveille l'état toutes les ~10
  secondes et envoie un message à **tous les joueurs connectés**, peu importe leur dimension, dès que
  la nuit change. Résultat : même nuit, même effet, même message, en simultané sur les 3 dimensions.

(Léger doublon possible pour un joueur qui serait dans l'Overworld pile au moment du changement : il
recevra à la fois le message natif d'Enhanced Celestials et celui de `NightAnnouncer`. Sans
conséquence, juste deux lignes de chat au lieu d'une — dites-moi si vous préférez que je supprime le
message natif d'EC pour n'avoir que le nôtre.)

### Code Java (`src/main/java/com/cvnights/`)
- `NightState` : état global (nuit active / prochaine nuit spéciale), basé sur l'Overworld uniquement.
- `CaptureBoostListener` : +15% capture pendant la Lune du Chasseur, partout.
- `RarityBoostListener` : +75% de poids rare/ultra-rare pendant la Lune Boréale, partout.
- `NightAnnouncer` : diffuse le changement de nuit à tous les joueurs, peu importe leur dimension.
- `XpBoostManager` + `FocusCandyItem` : bonbon de concentration — buff XP temporaire (x1,75, 25 min),
  cooldown 90 min, un seul actif à la fois, pas de stack.
- `NightWatchItem` : la montre — clic droit affiche la nuit du soir et la prochaine programmée.
- `CvNightsConfig` : génère `config/cvnights.json` au premier lancement (tout réglable sans recompiler).

Les dépendances (Cobblemon, Enhanced Celestials, Cobblemon Integrations, Data Anchor, Fabric Language
Kotlin) sont toutes téléchargées automatiquement à la compilation depuis le dépôt Maven officiel de
Modrinth — rien à placer soi-même.

---

## Compiler SANS RIEN INSTALLER — via GitHub Actions

Un fichier `.github/workflows/build.yml` est déjà inclus : il compile automatiquement le mod dans le
cloud dès que le projet est poussé sur GitHub.

1. Compte GitHub gratuit → **New repository** (nom libre, ne coche aucune case d'initialisation).
2. Dézippe ce zip. Sur la page du repo vide, **uploading an existing file**, glisse-dépose tout le
   **contenu** du dossier `cvnights/` (pas le dossier lui-même). Affiche les fichiers/dossiers cachés
   dans ton explorateur (`.github/`) avant de glisser, sinon ils sont ignorés.
   - Si un dossier ne passe pas au glisser-déposer (ça arrive avec `.github/` ou les chemins profonds
     type `data/cvnights/enhancedcelestials/...`), utilise **Add file → Create new file** et tape le
     chemin complet avec des `/` dans le champ nom : GitHub crée les dossiers intermédiaires tout seul.
3. **Commit changes**.
4. Onglet **Actions** : l'exécution "Build mod" démarre automatiquement (~2-3 min). Si rien ne se
   passe, clique sur "Build mod" à gauche puis **Run workflow**.
5. Une fois vert, clique dessus → section **Artifacts** en bas de page → télécharge `cvnights-jar`.
6. Dézippe, récupère `cvnights-0.1.0.jar` (pas `-sources.jar`).

## Installer dans le modpack

1. Copie le `.jar` dans `mods/` de ton instance CurseForge (les 3 points `...` sur l'instance →
   **Open Folder** → `mods/`).
2. Fais pareil sur le PC de ta copine (même modpack, LAN).
3. Lance le jeu. Message attendu dans les logs : `[cvnights] Nuits spéciales chargées : [...]`.
4. Craft la montre (horloge + 4 amethyst shard + 4 gold ingot) et le bonbon (carotte dorée + sucre +
   poudre de glowstone).
5. En jeu (OP) : `/lunarForecast` pour voir la prévision brute d'Enhanced Celestials, `/lunarForecast
   recompute` pour forcer un recalcul si besoin (utile juste après une mise à jour du mod).

## Réglages

- `.minecraft/config/cvnights.json` (généré au 1er lancement) : bonus de rareté, de capture,
  multiplicateur/durée/cooldown du bonbon.
- `data/cvnights/enhancedcelestials/lunar/event/*.json` : fréquence (`chance`), espacement minimum
  entre deux occurrences de la même nuit (`min_number_of_nights_between`), couleurs (hex 6 chiffres).
  Modifiable après compilation en rouvrant le `.jar` avec un outil zip, sans recompiler.
- Textures montre/bonbon : `night_watch` utilise `minecraft:item/recovery_compass` (icône propre, pas
  de bug de texture manquante), `focus_candy` utilise `minecraft:item/sugar`, en attendant vos propres
  textures dans `assets/cvnights/textures/item/`.

## Si la compilation échoue

Le message d'erreur exact apparaît dans l'onglet Actions (log rouge). Causes déjà rencontrées et
corrigées dans ce zip : version Gradle mal quotée dans le YAML, mémoire Gradle insuffisante, mauvais
package pour `RegistryEntry` (Yarn 1.21.1 : `net.minecraft.registry.entry`), méthode
`Item.Settings#registryKey` inexistante en 1.21.1, dépendance manquante `Data Anchor` (requise par
Enhanced Celestials), icône de la montre (texture d'horloge animée non utilisable telle quelle).
