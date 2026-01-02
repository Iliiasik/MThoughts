package mt;

import mt.network.NetworkHandler;
import mt.server.DailyStatsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughts implements ModInitializer {
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Midnight Thoughts Server...");

        NetworkHandler.registerPackets();
        DailyStatsManager.initialize();

        ServerTickEvents.END_SERVER_TICK.register(DailyStatsManager::tick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DailyStatsManager.onPlayerJoin(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            DailyStatsManager.onPlayerLeave(handler.player);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(DailyStatsManager::onServerStop);

        LOGGER.info("Midnight Thoughts Server initialized successfully!");
    }
}

