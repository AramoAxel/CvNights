package com.cvnights;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Config JSON basique, générée automatiquement au premier lancement dans
 * .minecraft/config/cvnights.json (ou run/config/cvnights.json en dev).
 * Tout est réglable sans recompiler.
 */
public class CvNightsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path;
    public static CvNightsConfig INSTANCE = new CvNightsConfig();

    // --- Calendrier des nuits spéciales (NightScheduler) ---
    public int minGapNights = 6;   // écart minimum entre deux nuits spéciales
    public int maxGapNights = 10;  // écart MAXIMUM garanti (jamais plus que ça sans nuit spéciale)

    // --- Lune Benie : IV minimum garanti a la capture ---
    public int blessedMoonMinIVsNormal = 2;      // Pokemon normal : au moins 2 IV a 31
    public int blessedMoonMinIVsLegendary = 4;   // Legendaire/mythique/ultra-beast : au moins 4 IV a 31

    // --- Lune Boréale : rareté ---
    public float rareSpawnWeightBonus = 0.75f;      // +75%
    public float ultraRareSpawnWeightBonus = 2.0f;   // +200%

    // --- Lune du Chasseur : capture ---
    public float huntersMoonCatchRateBonus = 0.15f;  // +15%

    // --- Bonbon de concentration (XpBoostManager) ---
    public float candyXpMultiplier = 2.0f;           // x2 XP pendant l'effet
    public int candyDurationMinutes = 30;            // durée de l'effet
    public int candyCooldownMinutes = 60;            // cooldown avant de pouvoir en reprendre un
    public boolean candyAppliesPerPokemonInParty = false; // false = ne booste que le Pokémon envoyé au combat

    public static void load() {
        path = FabricLoader.getInstance().getConfigDir().resolve("cvnights.json");
        try {
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    CvNightsConfig loaded = GSON.fromJson(reader, CvNightsConfig.class);
                    if (loaded != null) INSTANCE = loaded;
                }
            } else {
                save();
            }
        } catch (IOException e) {
            CvNights.LOGGER.error("Impossible de charger cvnights.json, valeurs par défaut utilisées", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            CvNights.LOGGER.error("Impossible d'écrire cvnights.json", e);
        }
    }
}
