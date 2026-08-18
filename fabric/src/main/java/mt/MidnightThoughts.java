package mt;

import mt.command.MidnightThoughtsCommand;
import mt.config.MidnightThoughtsConfig;
import mt.network.NetworkHandler;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.platform.FabricBridges;
import mt.platform.MTEvents;
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
    @SuppressWarnings("resource")
    public void onInitialize() {
        FabricBridges.install();

        AchievementLoader.load();
        NetworkHandler.registerPackets();
        DailyStatsManager.initialize();
        UserContentInitializer.writeDefaultFiles();

        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) ->
                MidnightThoughtsCommand.register(dispatcher));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DailyStatsManager.tick(server);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                WellRestedEffect.tick(player);
                WellRestedSync.sync(player);
            }
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, _) -> {
            if (!(entity instanceof ServerPlayer serverPlayer)) return;
            SleepTracker tracker = DailyStatsManager.getSleepTracker(serverPlayer.level().getServer());
            if (tracker != null) {
                tracker.markPlayerWoke(serverPlayer.getUUID());
            }
        });

        EntitySleepEvents.ALLOW_SLEEPING.register((player, _) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return null;
            if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("midnightthoughts.sleep.nightmare_blocked"),
                        true
                );
                return Player.BedSleepingProblem.OTHER_PROBLEM;
            }

            SleepTracker tracker = DailyStatsManager.getSleepTracker(serverPlayer.level().getServer());
            if (tracker != null) {
                tracker.markPlayerSleeping(serverPlayer.getUUID());
            }
            if (ComfortCalculator.isNightmareMode(serverPlayer)) {
                MTEvents.nightmare(serverPlayer, ComfortCalculator.calculateComfortLevel(serverPlayer));
            }
            return null;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, _) -> {
            if (entity instanceof ServerPlayer player) {
                WellRestedEffect.clear(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            DailyStatsManager.onPlayerJoin(handler.player);
            WellRestedEffect.onPlayerJoin(handler.player);
            MTNetwork.sendToPlayer(handler.player, UserContentInitializer.buildPacket());
            MTNetwork.sendToPlayer(handler.player, new SyncAchievementsPacket(AchievementLoader.load()));
            MTNetwork.sendToPlayer(handler.player, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) -> {
            ComfortCalculator.invalidateCache(handler.player);
            DailyStatsManager.onPlayerLeave(handler.player);
            WellRestedSync.clear(handler.player.getUUID());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DailyStatsManager.onServerStop(server);
            UserContentInitializer.invalidateCache();
        });

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }
}
