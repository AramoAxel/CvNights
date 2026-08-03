package com.cvnights;

import com.cvnights.items.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CvNights implements ModInitializer {

    public static final String MOD_ID = "cvnights";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        CvNightsConfig.load();

        ModItems.register();
        ModEffects.register();

        CaptureBoostListener.register();
        RarityBoostListener.register();
        XpBoostManager.register();
        NightAnnouncer.register();
        NightScheduler.register();
        IVBoostListener.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CvNightsCommands.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            CvNightsConfig.save();
            server = null;
        });

        LOGGER.info("[cvnights] Nuits spéciales chargées : {}", NightState.ALL_SPECIAL_NIGHTS);
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
