package mt.server;

import mt.network.NetworkHandler;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

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
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!sleepingPlayers.contains(player.getUuid())) continue;
            UUID uuid = player.getUuid();
            DailyPlayerStats stats = getOrCreateStats(uuid);
            stats.captureCurrentStats(player);
            stats.saveToStorage(server);
        }
    }

    public static void showDailySummary(MinecraftServer server, Set<UUID> sleepingPlayers) {
        List<ServerPlayerEntity> sleptPlayers = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (sleepingPlayers.contains(player.getUuid())) {
                sleptPlayers.add(player);
            }
        }
        if (sleptPlayers.isEmpty()) return;

        List<AchievementCalculator.PlayerSummaryData> playerDataList = new ArrayList<>();
        Map<String, DailyPlayerStats.DailyDelta> deltaMap = new HashMap<>();
        Map<String, StatsStorage.SavedPlayerStats> savedStatsMap = new HashMap<>();

        for (ServerPlayerEntity player : sleptPlayers) {
            UUID uuid = player.getUuid();
            DailyPlayerStats stats = getOrCreateStats(uuid);
            DailyPlayerStats.DailyDelta delta = stats.calculateDelta(player);
            String playerName = player.getGameProfile().getName();

            StatsStorage.SavedPlayerStats savedStats = StatsStorage.loadPlayerStats(server, uuid);
            if (savedStats == null) {
                savedStats = new StatsStorage.SavedPlayerStats();
            }

            deltaMap.put(playerName, delta);
            savedStatsMap.put(playerName, savedStats);

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

        for (ServerPlayerEntity player : sleptPlayers) {
            String playerName = player.getGameProfile().getName();
            DailyPlayerStats.DailyDelta delta = deltaMap.get(playerName);
            StatsStorage.SavedPlayerStats savedStats = savedStatsMap.get(playerName);

            List<String> achievements = AchievementCalculator.calculateAchievements(delta, savedStats);
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

            StatsStorage.savePlayerStats(server, player.getUuid(), savedStats);
        }

        DailySummaryPacket packet = new DailySummaryPacket(summaries);
        for (ServerPlayerEntity player : sleptPlayers) {
            NetworkHandler.sendDailySummary(player, packet);
            boolean isMvp = player.getGameProfile().getName().equals(mvpName);
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

    public static void onPlayerJoin(ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        DailyPlayerStats stats = getOrCreateStats(player.getUuid());

        if (server != null) {
            stats.loadFromStorage(server);
        }

        StatsStorage.SavedPlayerStats saved = server != null ?
                StatsStorage.loadPlayerStats(server, player.getUuid()) : null;

        if (saved == null) {
            stats.captureCurrentStats(player);
            if (server != null) {
                stats.saveToStorage(server);
            }
        }
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        DailyPlayerStats stats = dailyStats.get(player.getUuid());

        if (stats != null && server != null) {
            stats.saveToStorage(server);
        }
    }

    public static void onServerStop(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            DailyPlayerStats stats = dailyStats.get(player.getUuid());
            if (stats != null) {
                stats.saveToStorage(server);
            }
        }
        dailyStats.clear();
        sleepTrackers.remove(server);
    }
}