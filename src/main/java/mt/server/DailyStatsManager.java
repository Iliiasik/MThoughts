package mt.server;

import mt.network.NetworkHandler;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DailyStatsManager {
    private static final Map<UUID, DailyPlayerStats> dailyStats = new ConcurrentHashMap<>();
    private static final Map<MinecraftServer, SleepTracker> sleepTrackers = new ConcurrentHashMap<>();

    public static void initialize() {
    }

    public static void tick(MinecraftServer server) {
        SleepTracker tracker = sleepTrackers.computeIfAbsent(server, SleepTracker::new);
        tracker.tick();
    }

    public static SleepTracker getSleepTracker(MinecraftServer server) {
        return sleepTrackers.get(server);
    }

    public static DailyPlayerStats getOrCreateStats(UUID playerUuid) {
        return dailyStats.computeIfAbsent(playerUuid, DailyPlayerStats::new);
    }

    public static void resetDailyStats(MinecraftServer server, Set<UUID> sleepingPlayers) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!sleepingPlayers.contains(player.getUUID())) continue;
            UUID uuid = player.getUUID();
            DailyPlayerStats stats = getOrCreateStats(uuid);
            stats.captureCurrentStats(player);
            stats.saveToStorage(server);
        }
    }

    public static void showDailySummary(MinecraftServer server, Set<UUID> sleepingPlayers) {
        List<ServerPlayer> sleptPlayers = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (sleepingPlayers.contains(player.getUUID())) {
                sleptPlayers.add(player);
            }
        }
        if (sleptPlayers.isEmpty()) return;

        List<AchievementCalculator.PlayerSummaryData> playerDataList = new ArrayList<>();
        Map<String, DailyPlayerStats.DailyDelta> deltaMap = new HashMap<>();

        for (ServerPlayer player : sleptPlayers) {
            UUID uuid = player.getUUID();
            DailyPlayerStats stats = getOrCreateStats(uuid);
            DailyPlayerStats.DailyDelta delta = stats.calculateDelta(player);
            String playerName = player.getName().getString();

            deltaMap.put(playerName, delta);
            playerDataList.add(new AchievementCalculator.PlayerSummaryData(
                    playerName,
                    delta.blocksDestroyed(),
                    delta.distanceWalked(),
                    delta.mobsKilled(),
                    delta.deaths(),
                    delta.jumps()
            ));
        }

        String mvpName = AchievementCalculator.determineMvp(playerDataList);

        List<DailySummaryPacket.PlayerDailySummary> summaries = new ArrayList<>();

        for (ServerPlayer player : sleptPlayers) {
            String playerName = player.getName().getString();
            DailyPlayerStats.DailyDelta delta = deltaMap.get(playerName);

            List<String> achievements = AchievementCalculator.calculateAchievements(player, delta, server);
            boolean isMvp = playerName.equals(mvpName);

            summaries.add(new DailySummaryPacket.PlayerDailySummary(
                    playerName,
                    delta.blocksDestroyed(),
                    delta.distanceWalked(),
                    delta.mobsKilled(),
                    delta.deaths(),
                    delta.jumps(),
                    delta.damageDealt(),
                    isMvp,
                    achievements
            ));
        }

        DailySummaryPacket packet = new DailySummaryPacket(summaries);
        for (ServerPlayer player : sleptPlayers) {
            NetworkHandler.sendDailySummary(player, packet);
            boolean isMvp = player.getName().getString().equals(mvpName);
            if (isMvp) {
                WellRestedEffect.applyMvpToPlayer(player);
            } else {
                int comfortLevel = ComfortCalculator.calculateComfortLevel(player);
                WellRestedEffect.applyToPlayer(player, comfortLevel);
            }
        }
    }

    public static void showDailySummaryAndReset(MinecraftServer server, Set<UUID> sleepingPlayers) {
        showDailySummary(server, sleepingPlayers);
        resetDailyStats(server, sleepingPlayers);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        MinecraftServer server = player.server;
        DailyPlayerStats stats = getOrCreateStats(player.getUUID());
        if (server != null) {
            stats.loadFromStorage(server);
        }
    }

    public static void onPlayerLeave(ServerPlayer player) {
        MinecraftServer server = player.server;
        DailyPlayerStats stats = dailyStats.get(player.getUUID());
        if (stats != null && server != null) {
            stats.saveToStorage(server);
        }
    }

    public static void onServerStop(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            DailyPlayerStats stats = dailyStats.get(player.getUUID());
            if (stats != null) {
                stats.saveToStorage(server);
            }
        }
        dailyStats.clear();
        sleepTrackers.remove(server);
    }
}