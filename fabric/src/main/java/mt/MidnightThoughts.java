package mt;

import mt.command.MidnightThoughtsCommand;
import mt.config.MidnightThoughtsConfig;
import mt.network.NetworkHandler;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.platform.FabricBridges;
import mt.platform.MTNetwork;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.SleepTracker;
import mt.server.UserContentInitializer;
import mt.server.WellRestedEffect;
import mt.server.WellRestedSync;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughts implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitialize() {
        FabricBridges.install();

        MidnightThoughtsConfig.getInstance();
        NetworkHandler.registerPackets();
        AchievementLoader.load();
        UserContentInitializer.writeDefaultFiles();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DailyStatsManager.initialize();
            LOGGER.info("Midnight Thoughts server started");
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DailyStatsManager.tick(server);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                WellRestedEffect.tick(player);
                WellRestedSync.sync(player);
            }
        });

        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return null;

            int comfortLevel = ComfortCalculator.calculateComfortLevel(serverPlayer, sleepingPos);

            if (ComfortCalculator.isSleepBlocked(serverPlayer, sleepingPos)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("midnightthoughts.sleep.nightmare_blocked"),
                        true
                );
                return Player.BedSleepingProblem.OTHER_PROBLEM;
            }

            SleepTracker tracker = DailyStatsManager.getSleepTracker(serverPlayer.server);
            if (tracker != null) tracker.markSleepAttempt(serverPlayer.getUUID(), comfortLevel);
            return null;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                WellRestedEffect.clear(player);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                MidnightThoughtsCommand.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            DailyStatsManager.onPlayerJoin(player);
            MTNetwork.sendToPlayer(player, UserContentInitializer.buildPacket());
            MTNetwork.sendToPlayer(player, new SyncAchievementsPacket(AchievementLoader.load()));
            MTNetwork.sendToPlayer(player, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            ComfortCalculator.invalidateCache(player);
            DailyStatsManager.onPlayerLeave(player);
            WellRestedSync.clear(player.getUUID());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DailyStatsManager.onServerStop(server);
            UserContentInitializer.invalidateCache();
            LOGGER.info("Midnight Thoughts server stopping");
        });

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }
}
