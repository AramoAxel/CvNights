package com.cvnights;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Deux effets de statut pour le Bonbon de Concentration :
 * - FOCUS_BOOST : actif pendant candyDurationMinutes, c'est lui que XpBoostManager consulte pour
 *   savoir s'il faut multiplier l'XP gagnée.
 * - FOCUS_FATIGUE : couvre TOUTE la période (durée + cooldown) depuis la prise du bonbon ; sert
 *   uniquement à savoir si on peut en reprendre un (via sa durée restante), affiché comme un
 *   "debuff" neutre dans l'inventaire.
 *
 * Les deux sont de vrais StatusEffect vanilla : sauvegardés avec le joueur (survivent à une
 * déconnexion/reconnexion, contrairement à un minuteur gardé juste en mémoire), et affichés
 * nativement dans l'écran d'inventaire avec leur décompte. showParticles=false : aucun effet
 * visuel autour du joueur, seule l'icône + le timer apparaissent.
 */
public final class ModEffects {

    private ModEffects() {}

    public static RegistryEntry<StatusEffect> FOCUS_BOOST;
    public static RegistryEntry<StatusEffect> FOCUS_FATIGUE;

    public static void register() {
        RegistryKey<StatusEffect> boostKey = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of("cvnights", "focus_boost"));
        Registry.register(Registries.STATUS_EFFECT, boostKey, new SilentStatusEffect(StatusEffectCategory.BENEFICIAL, 0xE8C468));
        FOCUS_BOOST = Registries.STATUS_EFFECT.getEntry(boostKey).orElseThrow();

        RegistryKey<StatusEffect> fatigueKey = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of("cvnights", "focus_fatigue"));
        Registry.register(Registries.STATUS_EFFECT, fatigueKey, new SilentStatusEffect(StatusEffectCategory.NEUTRAL, 0x8899AA));
        FOCUS_FATIGUE = Registries.STATUS_EFFECT.getEntry(fatigueKey).orElseThrow();
    }

    /** StatusEffect minimal : ne fait rien tout seul (aucun tick/dégâts), sert uniquement de minuteur consultable. */
    private static class SilentStatusEffect extends StatusEffect {
        protected SilentStatusEffect(StatusEffectCategory category, int color) {
            super(category, color);
        }
    }
}
