package com.cvnights;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedEvent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Bonbon de Concentration : buff temporaire d'XP de combat, basé sur de vrais effets de statut
 * (voir ModEffects) pour survivre à une déconnexion/reconnexion — essentiel en LAN, où quitter la
 * partie éteint le serveur hébergé et remettrait à zéro un simple minuteur en mémoire.
 */
public final class XpBoostManager {

    private static final int TICKS_PER_MINUTE = 20 * 60;

    private XpBoostManager() {}

    public static void register() {
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(XpBoostManager::onExperienceGained);
    }

    private static void onExperienceGained(ExperienceGainedEvent.Pre event) {
        var owner = event.getPokemon().getOwnerPlayer();
        if (owner == null) return;

        if (owner.hasStatusEffect(ModEffects.FOCUS_BOOST)) {
            float multiplier = CvNightsConfig.INSTANCE.candyXpMultiplier;
            event.setExperience(Math.round(event.getExperience() * multiplier));
        }
    }

    /**
     * Tente d'appliquer le bonbon à ce joueur.
     * @return true si le buff a été appliqué, false si déjà actif ou en cooldown (message envoyé au joueur dans ce cas).
     */
    public static boolean tryApplyCandy(ServerPlayerEntity player) {
        if (player.hasStatusEffect(ModEffects.FOCUS_BOOST)) {
            player.sendMessage(Text.translatable("cvnights.focus_candy.already_active", player.getName(),
                    minutesLeft(player, ModEffects.FOCUS_BOOST)), true);
            return false;
        }

        if (player.hasStatusEffect(ModEffects.FOCUS_FATIGUE)) {
            player.sendMessage(Text.translatable("cvnights.focus_candy.cooldown", player.getName(),
                    minutesLeft(player, ModEffects.FOCUS_FATIGUE)), true);
            return false;
        }

        int durationTicks = CvNightsConfig.INSTANCE.candyDurationMinutes * TICKS_PER_MINUTE;
        int totalTicks = (CvNightsConfig.INSTANCE.candyDurationMinutes + CvNightsConfig.INSTANCE.candyCooldownMinutes) * TICKS_PER_MINUTE;

        // ambient=false, showParticles=false (aucun effet visuel sur le joueur), showIcon=true (visible en inventaire)
        player.addStatusEffect(new StatusEffectInstance(ModEffects.FOCUS_BOOST, durationTicks, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(ModEffects.FOCUS_FATIGUE, totalTicks, 0, false, false, true));

        player.sendMessage(Text.translatable("cvnights.focus_candy.applied", player.getName(), CvNightsConfig.INSTANCE.candyDurationMinutes), false);
        return true;
    }

    private static long minutesLeft(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance instance = player.getStatusEffect(effect);
        if (instance == null) return 0;
        return instance.getDuration() / TICKS_PER_MINUTE + 1;
    }
}
