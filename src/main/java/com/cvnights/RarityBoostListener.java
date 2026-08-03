package com.cvnights;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.entity.SpawnBucketChosenEvent;
import com.cobblemon.mod.common.api.spawning.SpawnBucket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Lune Boréale : +75% (configurable) de poids pour les buckets "rare" et "ultra-rare".
 * S'applique partout (Overworld, Nether, End) puisque basé sur l'état global de NightState.
 */
public final class RarityBoostListener {

    private static final Random RANDOM = new Random();

    private RarityBoostListener() {}

    public static void register() {
        CobblemonEvents.SPAWN_BUCKET_CHOSEN.subscribe(RarityBoostListener::onBucketChosen);
    }

    private static void onBucketChosen(SpawnBucketChosenEvent event) {
        if (!NightState.isActive(NightState.BOREAL_MOON)) return;

        Map<SpawnBucket, Float> weights = event.getBucketWeights();
        if (weights.isEmpty()) return;

        float bonusRare = CvNightsConfig.INSTANCE.rareSpawnWeightBonus;
        float bonusUltra = CvNightsConfig.INSTANCE.ultraRareSpawnWeightBonus;

        Map<SpawnBucket, Float> boosted = new HashMap<>();
        float total = 0f;
        for (Map.Entry<SpawnBucket, Float> entry : weights.entrySet()) {
            String name = entry.getKey().getName();
            float weight = entry.getValue();
            if ("rare".equals(name)) {
                weight *= (1.0f + bonusRare);
            } else if ("ultra-rare".equals(name)) {
                weight *= (1.0f + bonusUltra);
            }
            boosted.put(entry.getKey(), weight);
            total += weight;
        }

        if (total <= 0f) return;

        float roll = RANDOM.nextFloat() * total;
        float cumulative = 0f;
        for (Map.Entry<SpawnBucket, Float> entry : boosted.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                event.setBucket(entry.getKey());
                return;
            }
        }
    }
}
