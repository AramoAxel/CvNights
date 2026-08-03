package com.cvnights;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;

/**
 * Enhanced Celestials ne notifie nativement que les joueurs présents dans l'Overworld au moment où
 * une nuit change (puisque c'est la seule dimension suivie). Cette classe comble ça : elle surveille
 * l'état global (NightState) et diffuse un message à TOUS les joueurs connectés dès qu'il change,
 * qu'ils soient dans l'Overworld, au Nether ou dans l'End.
 */
public final class NightAnnouncer {

    private static final int CHECK_INTERVAL_TICKS = 200; // ~10 secondes, largement suffisant (les nuits durent des milliers de ticks)

    private static Identifier lastAnnounced = null; // null = nuit normale
    private static boolean initialized = false;
    private static int tickCounter = 0;

    private NightAnnouncer() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(NightAnnouncer::onTick);
    }

    private static void onTick(MinecraftServer server) {
        if (++tickCounter < CHECK_INTERVAL_TICKS) return;
        tickCounter = 0;

        Optional<Identifier> current = NightState.currentSpecialNight();
        Identifier currentId = current.orElse(null);

        if (!initialized) {
            // Au tout premier check après le démarrage, on mémorise l'état sans l'annoncer
            // (sinon on spam un message "nuit normale" à chaque redémarrage du serveur).
            lastAnnounced = currentId;
            initialized = true;
            return;
        }

        if (!Objects.equals(currentId, lastAnnounced)) {
            Text message = currentId != null
                    ? Text.translatable("cvnights.announce.rise", nightDisplayName(currentId))
                    : Text.translatable("cvnights.announce.set");

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.sendMessage(message, false);
            }
            lastAnnounced = currentId;
        }
    }

    private static Text nightDisplayName(Identifier nightId) {
        return Text.translatable("enhancedcelestials.name." + nightId.getPath());
    }
}
