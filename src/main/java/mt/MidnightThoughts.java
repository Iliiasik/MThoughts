package mt;

import mt.network.NetworkHandler;
import mt.network.packet.WellRestedPacket;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.SleepTracker;
import mt.server.WellRestedEffect;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
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
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitialize() {
        WellRestedEffect.register();
        NetworkHandler.registerPackets();
        DailyStatsManager.initialize();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DailyStatsManager.tick(server);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
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

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayerEntity serverPlayer)) return;
            MinecraftServer srv = ((net.minecraft.server.world.ServerWorld) serverPlayer.getEntityWorld()).getServer();
            if (srv != null) {
                SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
                if (tracker != null) tracker.markPlayerSlept(serverPlayer.getUuid());
                srv.execute(() -> {
                    int comfortLevel = ComfortCalculator.calculateComfortLevel(serverPlayer);
                    WellRestedEffect.applyToPlayer(serverPlayer, comfortLevel);
                });
            }
        });

        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return null;
            if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
                serverPlayer.sendMessage(
                        Text.translatable("midnightthoughts.sleep.nightmare_blocked"),
                        true
                );
                return PlayerEntity.SleepFailureReason.OTHER_PROBLEM;
            }
            return null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DailyStatsManager.onPlayerJoin(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            DailyStatsManager.onPlayerLeave(handler.player);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(DailyStatsManager::onServerStop);

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }
}