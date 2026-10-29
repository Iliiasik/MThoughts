package mt;

import mt.command.MidnightThoughtsCommand;
import mt.network.NetworkHandler;
import mt.platform.FabricBridges;
import mt.server.MTServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughts implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitialize() {
        FabricBridges.install();
        MTServer.init();
        NetworkHandler.registerPackets();

        if (System.getProperty("fabric-api.gametest") != null) {
            mt.gametest.FabricGameTestHooks.register();
        }

        ServerLifecycleEvents.SERVER_STARTED.register(server -> LOGGER.info("Midnight Thoughts server started"));
        ServerTickEvents.END_SERVER_TICK.register(MTServer::tick);

        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) ->
                player instanceof ServerPlayer serverPlayer ? MTServer.trySleep(serverPlayer, sleepingPos) : null);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) MTServer.onDeath(player);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                MidnightThoughtsCommand.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> MTServer.onJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> MTServer.onLeave(handler.player));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            MTServer.onStopping(server);
            LOGGER.info("Midnight Thoughts server stopping");
        });

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }
}
