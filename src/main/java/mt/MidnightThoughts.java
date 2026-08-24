package mt;

import mt.command.MidnightThoughtsCommand;
import mt.config.MidnightThoughtsConfig;
import mt.network.NetworkHandler;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughts implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    @SuppressWarnings("resource")
    public void onInitialize() {
        MidnightThoughtsConfig.getInstance();
        NetworkHandler.registerPackets();
        AchievementLoader.load();
        UserContentInitializer.writeDefaultFiles();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            NetworkHandler.setServer(server);
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
            if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("midnightthoughts.sleep.nightmare_blocked"), true);
                return Player.BedSleepingProblem.OTHER_PROBLEM;
            }

            MinecraftServer srv = serverPlayer.level().getServer();
            SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
            if (tracker != null) tracker.markPlayerSleeping(serverPlayer.getUUID());
            if (ComfortCalculator.isNightmareMode(serverPlayer)) {
                mt.api.event.NightmareCallback.EVENT.invoker().onNightmare(
                        serverPlayer, ComfortCalculator.calculateComfortLevel(serverPlayer));
            }
            return null;
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayer serverPlayer)) return;
            MinecraftServer srv = serverPlayer.level().getServer();
            SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
            if (tracker != null) tracker.markPlayerWoke(serverPlayer.getUUID());
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                WellRestedEffect.clear(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            DailyStatsManager.onPlayerJoin(player);
            NetworkHandler.sendUserContent(player, UserContentInitializer.buildPacket());
            NetworkHandler.sendAchievements(player, new SyncAchievementsPacket(AchievementLoader.load()));
            NetworkHandler.sendConfig(player, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ComfortCalculator.invalidateCache(handler.player);
            DailyStatsManager.onPlayerLeave(handler.player);
            WellRestedSync.clear(handler.player.getUUID());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DailyStatsManager.onServerStop(server);
            UserContentInitializer.invalidateCache();
            NetworkHandler.setServer(null);
            LOGGER.info("Midnight Thoughts server stopping");
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                MidnightThoughtsCommand.register(dispatcher));

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }
}
