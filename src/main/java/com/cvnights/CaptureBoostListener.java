package com.cvnights;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;

/**
 * Lune du Chasseur : +15% (configurable) de taux de capture.
 * S'applique partout (Overworld, Nether, End) puisque basé sur l'état global de NightState.
 */
public final class CaptureBoostListener {

    private CaptureBoostListener() {}

    public static void register() {
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe(CaptureBoostListener::onCatchRate);
    }

    private static void onCatchRate(PokemonCatchRateEvent event) {
        if (NightState.isActive(NightState.HUNTER_MOON)) {
            float bonus = CvNightsConfig.INSTANCE.huntersMoonCatchRateBonus;
            event.setCatchRate(event.getCatchRate() * (1.0f + bonus));
        }
    }
}
