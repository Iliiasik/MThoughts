package mt.server;

import mt.network.NetworkHandler;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DailyStatsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Map<UUID, DailyPlayerStats> dailyStats = new ConcurrentHashMap<>();
    private static final Map<MinecraftServer, SleepTracker> sleepTrackers = new ConcurrentHashMap<>();
    private static MinecraftServer currentServer = null;

    public static void initialize() {
        LOGGER.info("[DailyStatsManager] Initialized");
    }

    public static void tick(MinecraftServer server) {
        currentServer = server;
        SleepTracker tracker = sleepTrackers.computeIfAbsent(server, SleepTracker::new);
        tracker.tick();
    }

    public static DailyPlayerStats getOrCreateStats(UUID playerUuid) {
        return dailyStats.computeIfAbsent(playerUuid, DailyPlayerStats::new);
    }

    public static void resetDailyStats(MinecraftServer server) {
        LOGGER.info("[DailyStatsManager] Resetting daily stats for all players");
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID uuid = player.getUuid();
            DailyPlayerStats stats = getOrCreateStats(uuid);
            stats.captureCurrentStats(player);
            stats.saveToStorage(server);
        }
        LOGGER.info("[DailyStatsManager] Daily stats reset complete");
    }

    public static void showDailySummary(MinecraftServer server) {
        LOGGER.info("[DailyStatsManager] Building daily summary...");

        List<AchievementCalculator.PlayerSummaryData> playerDataList = new ArrayList<>();
        Map<String, DailyPlayerStats.DailyDelta> deltaMap = new HashMap<>();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID uuid = player.getUuid();
            DailyPlayerStats stats = getOrCreateStats(uuid);
            DailyPlayerStats.DailyDelta delta = stats.calculateDelta(player);
            String playerName = player.getGameProfile().getName();

            deltaMap.put(playerName, delta);
            playerDataList.add(new AchievementCalculator.PlayerSummaryData(
                playerName,
                delta.blocksDestroyed(),
                delta.distanceWalked(),
                delta.mobsKilled(),
                delta.deaths(),
                delta.jumps()
            ));

            LOGGER.info("[DailyStatsManager] Player {} stats: blocks={}, distance={}, mobs={}, deaths={}, jumps={}",
                playerName, delta.blocksDestroyed(), delta.distanceWalked(), delta.mobsKilled(), delta.deaths(), delta.jumps());
        }

        String mvpName = AchievementCalculator.determineMvp(playerDataList);
        if (mvpName != null) {
            LOGGER.info("[DailyStatsManager] MVP determined: {}", mvpName);
        }

        List<DailySummaryPacket.PlayerDailySummary> summaries = new ArrayList<>();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            String playerName = player.getGameProfile().getName();
            DailyPlayerStats.DailyDelta delta = deltaMap.get(playerName);

            List<String> achievements = AchievementCalculator.calculateAchievements(player, delta, server);
            boolean isMvp = playerName.equals(mvpName);

            if (!achievements.isEmpty()) {
                LOGGER.info("[DailyStatsManager] Player {} achievements: {}", playerName, achievements);
            }

            summaries.add(new DailySummaryPacket.PlayerDailySummary(
                playerName,
                delta.blocksDestroyed(),
                delta.distanceWalked(),
                delta.mobsKilled(),
                delta.deaths(),
                delta.jumps(),
                isMvp,
                achievements
            ));
        }

        if (!summaries.isEmpty()) {
            LOGGER.info("[DailyStatsManager] Sending daily summary packet with {} player summaries", summaries.size());
            DailySummaryPacket packet = new DailySummaryPacket(summaries);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                NetworkHandler.sendToClient(player, packet);
                LOGGER.info("[DailyStatsManager] Sent summary to player {}", player.getGameProfile().getName());

                int comfortLevel = ComfortCalculator.calculateComfortLevel(player);
                WellRestedEffect.applyToPlayer(player, comfortLevel);
            }
        } else {
            LOGGER.warn("[DailyStatsManager] No player summaries to send");
        }
    }

    public static void showDailySummaryAndReset(MinecraftServer server) {
        showDailySummary(server);
        resetDailyStats(server);
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        LOGGER.info("[DailyStatsManager] Player joined: {}", player.getGameProfile().getName());
        MinecraftServer server = player.getServer();
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
        LOGGER.info("[DailyStatsManager] Player leaving: {}", player.getGameProfile().getName());
        MinecraftServer server = player.getServer();
        DailyPlayerStats stats = dailyStats.get(player.getUuid());

        if (stats != null && server != null) {
            stats.saveToStorage(server);
            LOGGER.info("[DailyStatsManager] Saved stats for player {} on disconnect", player.getGameProfile().getName());
        }
    }

    public static void onServerStop(MinecraftServer server) {
        LOGGER.info("[DailyStatsManager] Server stopping, saving all player stats");

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            DailyPlayerStats stats = dailyStats.get(player.getUuid());
            if (stats != null) {
                stats.saveToStorage(server);
            }
        }

        dailyStats.clear();
        sleepTrackers.remove(server);
        currentServer = null;
        LOGGER.info("[DailyStatsManager] Server stop complete");
    }
}

