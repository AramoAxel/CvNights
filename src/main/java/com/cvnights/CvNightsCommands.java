package com.cvnights;

import com.mojang.brigadier.CommandDispatcher;
import dev.corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Commandes de debug pour ce mod :
 *  - /cvnights forecast, /cvnights recompute : diagnostic (voir plus haut dans la conversation).
 *  - /cvnights blue|sage|boreal|blessed|hunter : force la nuit CE SOIR, immédiatement, pour tester
 *    un effet sans attendre le tirage aléatoire. Nécessite d'être OP (niveau 2).
 *  - /cvnights normal : annule une nuit forcée, retour à une nuit normale.
 */
public final class CvNightsCommands {

    private CvNightsCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("cvnights")
                .then(literal("forecast").executes(ctx -> forecast(ctx.getSource())))
                .then(literal("recompute").requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> recompute(ctx.getSource())))
                .then(literal("blue").requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> forceNight(ctx.getSource(), NightState.BLUE_MOON)))
                .then(literal("sage").requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> forceNight(ctx.getSource(), NightState.SAGE_MOON)))
                .then(literal("boreal").requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> forceNight(ctx.getSource(), NightState.BOREAL_MOON)))
                .then(literal("blessed").requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> forceNight(ctx.getSource(), NightState.BLESSED_MOON)))
                .then(literal("hunter").requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> forceNight(ctx.getSource(), NightState.HUNTER_MOON)))
                .then(literal("normal").requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> forceNight(ctx.getSource(), Identifier.of("enhancedcelestials", "default")))));
    }

    private static int forecast(ServerCommandSource source) {
        Optional<EnhancedCelestialsLunarForecastWorldData> data = NightState.rawForecastData();
        if (data.isEmpty()) {
            source.sendError(Text.literal("Aucune donnée de forecast disponible (Enhanced Celestials pas encore initialisé sur ce monde ?)"));
            return 0;
        }
        source.sendFeedback(() -> data.get().getForecastComponent(), false);
        return 1;
    }

    private static int recompute(ServerCommandSource source) {
        Optional<EnhancedCelestialsLunarForecastWorldData> data = NightState.rawForecastData();
        if (data.isEmpty()) {
            source.sendError(Text.literal("Aucune donnée de forecast disponible."));
            return 0;
        }
        data.get().recomputeForecast();
        source.sendFeedback(() -> Text.literal("Forecast recalculé."), true);
        return 1;
    }

    private static int forceNight(ServerCommandSource source, Identifier nightId) {
        Optional<EnhancedCelestialsLunarForecastWorldData> data = NightState.rawForecastData();
        if (data.isEmpty()) {
            source.sendError(Text.literal("Aucune donnée de forecast disponible."));
            return 0;
        }
        data.get().setLunarEvent(RegistryKey.of(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY, nightId));
        source.sendFeedback(() -> Text.literal("Nuit forcée : " + nightId), true);
        return 1;
    }
}
