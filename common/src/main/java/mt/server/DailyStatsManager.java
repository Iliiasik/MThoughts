package mt.server;

import mt.network.packet.DailySummaryPacket;
import mt.platform.MTEvents;
import mt.platform.MTNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DailyStatsManager {
    private static final Map<UUID, DailyPlayerStats> dailyStats = new ConcurrentHashMap<>();
    private static final Map<MinecraftServer, SleepTracker> sleepTrackers = new ConcurrentHashMap<>();

    private static int day(MinecraftServer server) {
        return (int) Math.min(server.overworld().getDayTime() / 24000L, Integer.MAX_VALUE);
    }

    public static void tick(MinecraftServer server) {
        sleepTrackers.computeIfAbsent(server, SleepTracker::new).tick();
    }

    @SuppressWarnings("resource")
    public static Integer sessionComfort(ServerPlayer player) {
        SleepTracker tracker = getSleepTracker(player.level().getServer());
        return tracker != null ? tracker.getSessionComfort(player.getUUID()) : null;
    }

    @SuppressWarnings("resource")
    public static int sleepComfort(ServerPlayer player) {
        SleepTracker tracker = getSleepTracker(player.level().getServer());
        Integer pinned = tracker != null ? tracker.getPinnedComfort(player.getUUID()) : null;
        return pinned != null ? pinned : ComfortCalculator.calculateComfortLevel(player);
    }

    public static SleepTracker getSleepTracker(MinecraftServer server) {
        return sleepTrackers.get(server);
    }

    public static DailyPlayerStats getOrCreateStats(UUID playerUuid) {
        return dailyStats.computeIfAbsent(playerUuid, DailyPlayerStats::new);
    }

    public static void showDailySummaryAndReset(MinecraftServer server, Set<UUID> sleepingPlayers) {
        List<Sleeper> sleepers = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (sleepingPlayers.contains(player.getUUID())) sleepers.add(Sleeper.of(server, player));
        }
        if (sleepers.isEmpty()) return;

        String mvpName = AchievementCalculator.determineMvp(sleepers.stream().map(Sleeper::mvpData).toList());

        int day = day(server);
        List<DailySummaryPacket.PlayerDailySummary> summaries = new ArrayList<>();
        for (Sleeper sleeper : sleepers) {
            List<String> achievements = AchievementCalculator.calculateAchievements(sleeper.delta(), sleeper.saved());
            StatsStorage.savePlayerStats(server, sleeper.player().getUUID(), sleeper.saved());
            summaries.add(sleeper.summary(sleeper.name().equals(mvpName), achievements, day));
        }

        DailySummaryPacket packet = new DailySummaryPacket(summaries);
        for (Sleeper sleeper : sleepers) {
            ServerPlayer player = sleeper.player();
            MTNetwork.sendToPlayer(player, packet);
            if (sleeper.name().equals(mvpName)) {
                WellRestedEffect.applyMvpToPlayer(player);
                MTEvents.mvpDetermined(player);
            } else {
                WellRestedEffect.applyToPlayer(player, sleepComfort(player));
            }
            DailyPlayerStats stats = getOrCreateStats(player.getUUID());
            stats.captureCurrentStats(player);
            stats.saveToStorage(server);
        }
        StatsStorage.flush();
    }

    private record Sleeper(ServerPlayer player, String name, DailyPlayerStats.DailyDelta delta,
                           StatsStorage.SavedPlayerStats saved) {

        static Sleeper of(MinecraftServer server, ServerPlayer player) {
            StatsStorage.SavedPlayerStats saved = StatsStorage.loadPlayerStats(server, player.getUUID());
            return new Sleeper(player, player.getName().getString(),
                    getOrCreateStats(player.getUUID()).calculateDelta(player),
                    saved != null ? saved : new StatsStorage.SavedPlayerStats());
        }

        AchievementCalculator.PlayerSummaryData mvpData() {
            return new AchievementCalculator.PlayerSummaryData(name, delta.blocksDestroyed(),
                    delta.distanceWalked(), delta.mobsKilled(), delta.deaths(), delta.jumps());
        }

        DailySummaryPacket.PlayerDailySummary summary(boolean mvp, List<String> achievements, int day) {
            return new DailySummaryPacket.PlayerDailySummary(name, delta.blocksDestroyed(),
                    delta.distanceWalked(), delta.mobsKilled(), delta.deaths(), delta.jumps(),
                    delta.damageDealt(), day, mvp, achievements);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        getOrCreateStats(player.getUUID()).loadFromStorage(player.server);
        markPlayerListChanged(player.server);
    }

    public static void onPlayerLeave(ServerPlayer player) {
        MinecraftServer server = player.server;
        DailyPlayerStats stats = dailyStats.get(player.getUUID());
        if (stats != null) {
            stats.saveToStorage(server);
            StatsStorage.flush();
        }
        SleepTracker tracker = getSleepTracker(server);
        if (tracker != null) tracker.forgetPlayer(player.getUUID());
        markPlayerListChanged(server);
    }

    private static void markPlayerListChanged(MinecraftServer server) {
        SleepTracker tracker = getSleepTracker(server);
        if (tracker != null) tracker.markPlayerListChanged();
    }

    public static void onServerStop(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            DailyPlayerStats stats = dailyStats.get(player.getUUID());
            if (stats != null) {
                stats.saveToStorage(server);
            }
        }
        StatsStorage.unload();
        ComfortCalculator.clearCache();
        WellRestedBuffs.clearCache();
        dailyStats.clear();
        sleepTrackers.remove(server);
    }
}
