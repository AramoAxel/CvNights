package com.cvnights;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lune Bénie : garantit un nombre minimum d'IV parfaites (31) à la capture.
 *
 * Remplace le champ "min_perf_ivs" du pont Cobblemon Integrations (mis à 0 dans
 * blessed_moon.json) car celui-ci applique un seuil unique sans distinction
 * légendaire/normal. Ici :
 *  - Pokémon normal  : garantit blessedMoonMinIVsNormal (défaut 2)
 *  - Légendaire / Mythique / Ultra-Beast (déjà 3 IV garanties nativement par
 *    Cobblemon) : garantit blessedMoonMinIVsLegendary (défaut 4), soit +1
 *    par rapport à leur plancher naturel plutôt que de les ignorer.
 *
 * On ne touche jamais à des IV déjà à 31 ; on complète seulement les stats
 * les plus faibles, choisies au hasard, jusqu'à atteindre la cible.
 */
public final class IVBoostListener {

    private static final Stat[] ALL_STATS = {
            Stats.HP, Stats.ATTACK, Stats.DEFENCE,
            Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
    };
    private static final Random RANDOM = new Random();

    private IVBoostListener() {}

    public static void register() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(IVBoostListener::onCaptured);
    }

    private static void onCaptured(PokemonCapturedEvent event) {
        if (!NightState.isActive(NightState.BLESSED_MOON)) return;

        Pokemon pokemon = event.getPokemon();
        boolean isLegendaryLike = pokemon.isLegendary() || pokemon.isMythical() || pokemon.isUltraBeast();
        int target = isLegendaryLike
                ? CvNightsConfig.INSTANCE.blessedMoonMinIVsLegendary
                : CvNightsConfig.INSTANCE.blessedMoonMinIVsNormal;

        List<Stat> notPerfect = new ArrayList<>();
        int perfectCount = 0;
        for (Stat stat : ALL_STATS) {
            Integer value = pokemon.getIvs().get(stat);
            if (value != null && value >= 31) {
                perfectCount++;
            } else {
                notPerfect.add(stat);
            }
        }

        int needed = target - perfectCount;
        if (needed <= 0) return;

        Collections.shuffle(notPerfect, RANDOM);
        for (int i = 0; i < needed && i < notPerfect.size(); i++) {
            pokemon.setIV(notPerfect.get(i), 31);
        }
    }
}
