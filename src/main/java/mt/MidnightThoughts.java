package mt;

import mt.network.NetworkHandler;
import mt.network.packet.WellRestedPacket;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.SleepTracker;
import mt.server.WellRestedEffect;
import net.fabricmc.api.ModInitializer;
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
    public void onInitialize() {
        AchievementLoader.load();
        WellRestedEffect.register();
        NetworkHandler.registerPackets();
        DailyStatsManager.initialize();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DailyStatsManager.tick(server);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                WellRestedEffect.tick(player);
                boolean active = WellRestedEffect.hasEffect(player);
                int level = WellRestedEffect.getLevel(player);
                int ticksRemaining = WellRestedEffect.getTicksRemaining(player);
                int totalTicks = active ? WellRestedEffect.getTotalDurationTicksForPlayer(player) : 0;
                int phase = WellRestedEffect.getCurrentPhase(player);
                boolean nightmare = ComfortCalculator.isNightmareMode(player);
                boolean mvp = WellRestedEffect.isMvp(player);
                NetworkHandler.sendWellRested(player, new WellRestedPacket(active, level, ticksRemaining, totalTicks, phase, nightmare, mvp));
            }
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, _) -> {
            if (!(entity instanceof ServerPlayer serverPlayer)) return;
            MinecraftServer srv = serverPlayer.level().getServer();
            SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
            if (tracker != null) tracker.markPlayerSlept(serverPlayer.getUUID());
        });

        EntitySleepEvents.ALLOW_SLEEPING.register((player, _) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return null;
            if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("midnightthoughts.sleep.nightmare_blocked")
                );
                return Player.BedSleepingProblem.OTHER_PROBLEM;
            }
            return null;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, _) -> {
            if (entity instanceof ServerPlayer player) {
                WellRestedEffect.removeFromPlayer(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> DailyStatsManager.onPlayerJoin(handler.player));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) -> DailyStatsManager.onPlayerLeave(handler.player));

        ServerLifecycleEvents.SERVER_STOPPING.register(DailyStatsManager::onServerStop);

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }
}