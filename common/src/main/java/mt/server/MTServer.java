package mt.server;

import mt.config.MidnightThoughtsConfig;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.platform.MTNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class MTServer {

    private MTServer() {}

    public static void init() {
        MidnightThoughtsConfig.getInstance();
        AchievementLoader.load();
        UserContentInitializer.writeDefaultFiles();
    }

    public static void tick(MinecraftServer server) {
        DailyStatsManager.tick(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            WellRestedEffect.tick(player);
            WellRestedSync.sync(player);
        }
    }

    public static Player.BedSleepingProblem trySleep(ServerPlayer player, BlockPos bedPos) {
        int comfortLevel = ComfortCalculator.calculateComfortLevel(player, bedPos);
        if (ComfortCalculator.blocksSleep(comfortLevel)) {
            player.displayClientMessage(Component.translatable("midnightthoughts.sleep.nightmare_blocked"), true);
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        }
        SleepTracker tracker = DailyStatsManager.getSleepTracker(player.server);
        if (tracker != null) tracker.markSleepAttempt(player.getUUID(), comfortLevel);
        return null;
    }

    public static void onWokeUp(ServerPlayer player) {
        SleepTracker tracker = DailyStatsManager.getSleepTracker(player.server);
        if (tracker != null) tracker.markPlayerWoke(player);
    }

    public static void onDeath(ServerPlayer player) {
        WellRestedEffect.clear(player);
    }

    public static void onJoin(ServerPlayer player) {
        DailyStatsManager.onPlayerJoin(player);
        MTNetwork.sendToPlayer(player, UserContentInitializer.buildPacket());
        MTNetwork.sendToPlayer(player, new SyncAchievementsPacket(AchievementLoader.load()));
        MTNetwork.sendToPlayer(player, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
    }

    public static void onLeave(ServerPlayer player) {
        ComfortCalculator.invalidateCache(player);
        DailyStatsManager.onPlayerLeave(player);
        WellRestedSync.clear(player.getUUID());
    }

    public static void onStopping(MinecraftServer server) {
        DailyStatsManager.onServerStop(server);
        UserContentInitializer.invalidateCache();
    }

    public static void sendMoonPhase(ServerPlayer player) {
        MTNetwork.sendToPlayer(player, MoonPhasePacket.current(player.server));
    }

    public static void syncConfig(MinecraftServer server) {
        ComfortCalculator.clearCache();
        MTNetwork.sendToAll(server, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
    }
}
