package com.cvnights;

import dev.corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData;
import dev.corgitaco.enhancedcelestials.lunarevent.LunarEventInstance;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * On pilote nous-mêmes le calendrier des nuits spéciales plutôt que de laisser Enhanced Celestials
 * tirer au hasard parmi TOUS les événements enregistrés (les nôtres + ses propres Blue Moon/Blood
 * Moon/etc.). Ça règle 3 problèmes observés en test :
 *  - les nuits d'origine d'EC apparaissaient encore malgré la tentative de désactivation par
 *    datapack (l'ordre de priorité entre mods pour ce type de fichier n'est pas fiable) ;
 *  - un simple "% de chance par nuit" ne garantit aucun écart maximum, seulement une moyenne ;
 *  - ça donne un vrai tirage "sac" propre, sans dépendre du système de probabilité d'EC.
 */
public final class NightScheduler {

    private static final Identifier DEFAULT_EVENT = Identifier.of("enhancedcelestials", "default");
    private static final int CHECK_INTERVAL_TICKS = 200; // ~10 secondes

    private static final Random RANDOM = new Random();
    private static final List<Identifier> bag = new ArrayList<>();
    private static Identifier lastScheduled = null;
    private static int tickCounter = 0;

    private NightScheduler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(NightScheduler::onTick);
    }

    private static void onTick(MinecraftServer server) {
        if (++tickCounter < CHECK_INTERVAL_TICKS) return;
        tickCounter = 0;

        NightState.rawForecastData().ifPresent(NightScheduler::enforce);
    }

    private static void enforce(EnhancedCelestialsLunarForecastWorldData data) {
        // 1) Purge : ne garder dans la prévision que nos 5 nuits + la nuit par défaut.
        data.removeFromForecastIf(instance -> !isAllowed(instance.getLunarEventKey().getValue()));

        // 2) Si la nuit ACTIVE ce soir n'est ni une des nôtres ni la nuit normale, on la coupe.
        var currentHolder = data.currentLunarEventHolder();
        Identifier currentId = null;
        if (currentHolder != null) {
            currentId = currentHolder.getKey().map(RegistryKey::getValue).orElse(null);
            if (currentId != null && !isAllowed(currentId)) {
                data.setLunarEvent(RegistryKey.of(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY, DEFAULT_EVENT));
                currentId = DEFAULT_EVENT;
            }
        }

        // 3) S'assurer qu'une des nôtres est programmée dans les "maxGapNights" prochains jours.
        long currentDay = data.getCurrentDay();
        int maxGap = CvNightsConfig.INSTANCE.maxGapNights;

        boolean alreadyScheduled = data.getForecast().stream()
                .filter(instance -> !instance.passed(currentDay))
                .anyMatch(instance -> NightState.ALL_SPECIAL_NIGHTS.contains(instance.getLunarEventKey().getValue())
                        && instance.getDaysUntil(currentDay) <= maxGap);

        boolean activeNow = currentId != null && NightState.ALL_SPECIAL_NIGHTS.contains(currentId);

        if (!alreadyScheduled && !activeNow) {
            Identifier next = drawFromBag();
            int minGap = Math.max(1, CvNightsConfig.INSTANCE.minGapNights);
            int span = Math.max(1, maxGap - minGap + 1);
            int gap = minGap + RANDOM.nextInt(span);

            data.addEventToForecast(new LunarEventInstance(
                    RegistryKey.of(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY, next),
                    currentDay + gap,
                    true
            ));
            lastScheduled = next;
        }
    }

    private static boolean isAllowed(Identifier id) {
        return NightState.ALL_SPECIAL_NIGHTS.contains(id) || id.equals(DEFAULT_EVENT);
    }

    private static Identifier drawFromBag() {
        if (bag.isEmpty()) {
            bag.addAll(NightState.ALL_SPECIAL_NIGHTS);
            Collections.shuffle(bag, RANDOM);
            // évite de tirer deux fois de suite la même nuit à la jonction entre deux sacs
            if (lastScheduled != null && bag.get(0).equals(lastScheduled) && bag.size() > 1) {
                Collections.swap(bag, 0, 1);
            }
        }
        return bag.remove(0);
    }
}
