package com.cvnights;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * Point d'entrée unique pour savoir "quelle nuit spéciale est active ce soir / demain".
 *
 * IMPORTANT : la nuit est un état GLOBAL, basé uniquement sur le forecast de l'Overworld, même si le
 * joueur consultant l'info (montre, effets) se trouve au Nether ou dans l'End. C'est volontaire : une
 * seule nuit active à la fois, les mêmes effets partout, au même moment, pas de tirage indépendant
 * par dimension.
 */
public final class NightState {

    private NightState() {}

    public static final Identifier BLUE_MOON = Identifier.of("cvnights", "blue_moon");
    public static final Identifier SAGE_MOON = Identifier.of("cvnights", "sage_moon");
    public static final Identifier BOREAL_MOON = Identifier.of("cvnights", "boreal_moon");
    public static final Identifier BLESSED_MOON = Identifier.of("cvnights", "blessed_moon");
    public static final Identifier HUNTER_MOON = Identifier.of("cvnights", "hunter_moon");

    public static final List<Identifier> ALL_SPECIAL_NIGHTS = List.of(
            BLUE_MOON, SAGE_MOON, BOREAL_MOON, BLESSED_MOON, HUNTER_MOON
    );

    private static Optional<EnhancedCelestialsLunarForecastWorldData> forecast() {
        MinecraftServer server = CvNights.getServer();
        if (server == null) return Optional.empty();
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) return Optional.empty();
        return EnhancedCelestials.lunarForecastWorldData(overworld);
    }

    /** Retourne l'ID de la nuit spéciale active ce soir (état global), ou vide si nuit normale. */
    public static Optional<Identifier> currentSpecialNight() {
        return forecast().flatMap(data -> {
            // 1) Une des nôtres est-elle littéralement en train de se produire (nuit déjà tombée) ?
            RegistryEntry<LunarEvent> current = data.currentLunarEventHolder();
            if (current != null) {
                Optional<Identifier> activeId = current.getKey()
                        .map(RegistryKey::getValue)
                        .filter(ALL_SPECIAL_NIGHTS::contains);
                if (activeId.isPresent()) return activeId;
            }

            // 2) Sinon, une des nôtres est-elle programmée pour AUJOURD'HUI (jour J, avant la tombée
            //    de la nuit) ? On l'annonce déjà comme "ce soir" plutôt que d'afficher "normal" jusqu'au
            //    dernier moment.
            long currentDay = data.getCurrentDay();
            return data.getForecast().stream()
                    .filter(instance -> instance.getDaysUntil(currentDay) == 0)
                    .map(instance -> instance.getLunarEventKey().getValue())
                    .filter(ALL_SPECIAL_NIGHTS::contains)
                    .findFirst();
        });
    }

    /**
     * true si la nuit spéciale donnée est active ce soir, quelle que soit la dimension appelante.
     *
     * IMPORTANT : on n'utilise PAS currentLunarEventHolder() d'Enhanced Celestials ici, car sa
     * logique interne coupe aussi l'événement s'il pleut/orage (requires_clear_skies), ce qui n'a
     * de sens que pour les effets cosmétiques natifs d'EC — pas pour nos mécaniques Pokémon. On
     * vérifie donc nous-mêmes : la nuit est-elle programmée pour aujourd'hui, et fait-il
     * effectivement nuit (sans tenir compte de la météo) ?
     */
    public static boolean isActive(Identifier nightId) {
        return forecast().map(data -> {
            MinecraftServer server = CvNights.getServer();
            if (server == null) return false;
            ServerWorld overworld = server.getOverworld();
            if (overworld == null) return false;

            long timeOfDay = overworld.getTimeOfDay() % 24000L;
            boolean isNight = timeOfDay >= 13000L && timeOfDay < 23000L;
            if (!isNight) return false;

            long currentDay = data.getCurrentDay();
            return data.getForecast().stream()
                    .filter(instance -> instance.getDaysUntil(currentDay) == 0)
                    .anyMatch(instance -> instance.getLunarEventKey().getValue().equals(nightId));
        }).orElse(false);
    }

    /** Prochaine nuit spéciale programmée (parmi nos 5) APRÈS aujourd'hui, avec le nombre de nuits d'attente. */
    public static Optional<NextNight> nextSpecialNight() {
        return forecast().flatMap(data -> {
            long currentDay = data.getCurrentDay();
            return data.getForecast().stream()
                    .filter(instance -> instance.getDaysUntil(currentDay) >= 1)
                    .filter(instance -> ALL_SPECIAL_NIGHTS.contains(instance.getLunarEventKey().getValue()))
                    .min((a, b) -> Long.compare(a.getDaysUntil(currentDay), b.getDaysUntil(currentDay)))
                    .map(instance -> new NextNight(instance.getLunarEventKey().getValue(), instance.getDaysUntil(currentDay)));
        });
    }

    public record NextNight(Identifier nightId, long daysUntil) {}

    /** Pour la commande de debug /cvnights : accès brut aux données de forecast (peut être vide). */
    public static Optional<EnhancedCelestialsLunarForecastWorldData> rawForecastData() {
        return forecast();
    }
}
