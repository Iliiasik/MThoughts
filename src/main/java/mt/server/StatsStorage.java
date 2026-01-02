package mt.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final String STATS_FILE = "midnightthoughts_stats.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void savePlayerStats(MinecraftServer server, UUID playerUuid, SavedPlayerStats stats) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) {
            LOGGER.warn("[StatsStorage] Save path is null, cannot save stats for player {}", playerUuid);
            return;
        }

        LOGGER.info("[StatsStorage] Saving stats to path: {}", savePath);
        LOGGER.info("[StatsStorage] Stats to save: recordDistance={}, recordBlocks={}, recordMobs={}, totalSleeps={}",
            stats.recordDistance, stats.recordBlocks, stats.recordMobs, stats.totalSleeps);

        Map<String, SavedPlayerStats> allStats = loadAllStats(savePath);
        allStats.put(playerUuid.toString(), stats);

        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                GSON.toJson(allStats, writer);
            }
            LOGGER.info("[StatsStorage] Saved stats for player {} successfully", playerUuid);
        } catch (IOException e) {
            LOGGER.error("[StatsStorage] Failed to save stats", e);
        }
    }

    public static SavedPlayerStats loadPlayerStats(MinecraftServer server, UUID playerUuid) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) {
            LOGGER.warn("[StatsStorage] Save path is null for player {}", playerUuid);
            return null;
        }

        LOGGER.info("[StatsStorage] Loading stats from path: {}", savePath);
        Map<String, SavedPlayerStats> allStats = loadAllStats(savePath);
        LOGGER.info("[StatsStorage] Loaded {} total player entries", allStats.size());

        SavedPlayerStats stats = allStats.get(playerUuid.toString());
        if (stats != null) {
            LOGGER.info("[StatsStorage] Loaded stats for player {}: recordDistance={}, recordBlocks={}, recordMobs={}, totalSleeps={}",
                playerUuid, stats.recordDistance, stats.recordBlocks, stats.recordMobs, stats.totalSleeps);
        } else {
            LOGGER.info("[StatsStorage] No saved stats found for player {}", playerUuid);
        }
        return stats;
    }

    public static void removePlayerStats(MinecraftServer server, UUID playerUuid) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) return;

        Map<String, SavedPlayerStats> allStats = loadAllStats(savePath);
        if (allStats.remove(playerUuid.toString()) != null) {
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                GSON.toJson(allStats, writer);
            } catch (IOException e) {
                LOGGER.error("[StatsStorage] Failed to save stats after removal", e);
            }
        }
    }

    private static Path getStatsFilePath(MinecraftServer server) {
        if (server == null) return null;
        try {
            Path worldDir = server.getSavePath(WorldSavePath.ROOT);
            return worldDir.resolve(STATS_FILE);
        } catch (Exception e) {
            LOGGER.error("[StatsStorage] Failed to get save path", e);
            return null;
        }
    }

    private static Map<String, SavedPlayerStats> loadAllStats(Path savePath) {
        if (savePath == null || !Files.exists(savePath)) {
            return new HashMap<>();
        }

        try (Reader reader = Files.newBufferedReader(savePath)) {
            Type type = new TypeToken<Map<String, SavedPlayerStats>>() {}.getType();
            Map<String, SavedPlayerStats> result = GSON.fromJson(reader, type);
            return result != null ? result : new HashMap<>();
        } catch (IOException e) {
            LOGGER.error("[StatsStorage] Failed to load stats", e);
            return new HashMap<>();
        }
    }

    public static class SavedPlayerStats {
        public int baseBlocksDestroyed;
        public int baseDistanceWalked;
        public int baseMobsKilled;
        public int baseDeaths;
        public int baseJumps;

        public int recordBlocks;
        public int recordDistance;
        public int recordMobs;
        public int totalSleeps;

        public SavedPlayerStats() {}

        public SavedPlayerStats(int baseBlocksDestroyed, int baseDistanceWalked, int baseMobsKilled, int baseDeaths, int baseJumps) {
            this.baseBlocksDestroyed = baseBlocksDestroyed;
            this.baseDistanceWalked = baseDistanceWalked;
            this.baseMobsKilled = baseMobsKilled;
            this.baseDeaths = baseDeaths;
            this.baseJumps = baseJumps;
        }
    }
}

