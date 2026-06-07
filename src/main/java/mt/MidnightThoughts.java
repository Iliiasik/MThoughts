package mt;

import mt.network.NetworkHandler;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.WellRestedPacket;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.SleepTracker;
import mt.server.UserContentInitializer;
import mt.server.WellRestedEffect;
import mt.config.MidnightThoughtsConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughts implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitialize() {
        AchievementLoader.load();
        WellRestedEffect.register();
        NetworkHandler.registerPackets();
        DailyStatsManager.initialize();
        UserContentInitializer.writeDefaultFiles();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DailyStatsManager.tick(server);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                WellRestedEffect.tick(player);
                boolean active = WellRestedEffect.hasEffect(player);
                int level = WellRestedEffect.getLevel(player);
                int ticksRemaining = WellRestedEffect.getTicksRemaining(player);
                int totalTicks = active ? WellRestedEffect.getTotalDurationTicksForPlayer(player) : 0;
                int phase = WellRestedEffect.getCurrentPhase(player);
                boolean nightmare = player.isSleeping() && ComfortCalculator.isNightmareMode(player);
                boolean mvp = WellRestedEffect.isMvp(player);
                NetworkHandler.sendWellRested(player, new WellRestedPacket(active, level, ticksRemaining, totalTicks, phase, nightmare, mvp));
            }
        });

        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayerEntity serverPlayer)) return;
            MinecraftServer srv = serverPlayer.getEntityWorld().getServer();
            SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
            if (tracker != null) tracker.markPlayerSleeping(serverPlayer.getUuid());
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayerEntity serverPlayer)) return;
            MinecraftServer srv = serverPlayer.getEntityWorld().getServer();
            SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
            if (tracker != null) tracker.markPlayerWoke(serverPlayer.getUuid());
        });

        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return null;
            if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
                serverPlayer.sendMessage(
                        Text.translatable("midnightthoughts.sleep.nightmare_blocked"),
                        true
                );
                return PlayerEntity.SleepFailureReason.OTHER;
            }
            return null;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                WellRestedEffect.removeFromPlayer(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DailyStatsManager.onPlayerJoin(handler.player);
            NetworkHandler.sendUserContent(handler.player, UserContentInitializer.buildPacket());
            NetworkHandler.sendAchievements(handler.player, new SyncAchievementsPacket(AchievementLoader.load()));
            MidnightThoughtsConfig cfg = MidnightThoughtsConfig.getInstance();
            NetworkHandler.sendConfig(handler.player, new SyncConfigPacket(
                    cfg.getSleepOverlay().minSlideDisplayTimeMs,
                    cfg.getSleepOverlay().maxSlideDisplayTimeMs,
                    cfg.getSleepOverlay().fadeInDurationMs,
                    cfg.getSleepOverlay().fadeOutDurationMs,
                    cfg.getSleepOverlay().overlayOpacity,
                    cfg.getSleepOverlay().textOpacity,
                    cfg.getSleepOverlay().imageOpacity,
                    cfg.getSleepOverlay().specialSlideChance,
                    cfg.getSleepOverlay().enableOverlay,
                    cfg.getSleepOverlay().enableImage,
                    cfg.getSleepOverlay().enableDailySummaryScreen,
                    cfg.getSleepOverlay().useFactsApi,
                    cfg.getSleepOverlay().userContentReplaces,
                    cfg.getSleepOverlay().hideChatWhenSleeping,
                    cfg.getUi().theme,
                    cfg.getUi().hideWellRestedHud,
                    cfg.getUi().hideThemeSwitchButton
            ));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ComfortCalculator.invalidateCache(handler.player);
            DailyStatsManager.onPlayerLeave(handler.player);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(DailyStatsManager::onServerStop);

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }
}