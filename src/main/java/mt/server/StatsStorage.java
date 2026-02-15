package mt.server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final String STATS_FILE = "midnightthoughts_stats.json";
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(SavedPlayerStats.class, new SafeStatsDeserializer())
        .create();

    public static void savePlayerStats(MinecraftServer server, UUID playerUuid, SavedPlayerStats stats) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) {
            return;
        }

        Map<String, SavedPlayerStats> allStats = loadAllStats(savePath);
        allStats.put(playerUuid.toString(), stats);

        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                GSON.toJson(allStats, writer);
            }
        } catch (IOException e) {
            LOGGER.error("[StatsStorage] Failed to save stats", e);
        }
    }

    public static SavedPlayerStats loadPlayerStats(MinecraftServer server, UUID playerUuid) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) {
            return null;
        }

        Map<String, SavedPlayerStats> allStats = loadAllStats(savePath);
        return allStats.get(playerUuid.toString());
    }

    private static Path getStatsFilePath(MinecraftServer server) {
        if (server == null) return null;
        try {
            Path worldDir = server.getWorldPath(LevelResource.ROOT);
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

    private static class SafeStatsDeserializer implements JsonDeserializer<SavedPlayerStats> {
        @Override
        public SavedPlayerStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            SavedPlayerStats stats = new SavedPlayerStats();

            stats.baseBlocksDestroyed = safeGetInt(obj, "baseBlocksDestroyed");
            stats.baseDistanceWalked = safeGetInt(obj, "baseDistanceWalked");
            stats.baseMobsKilled = safeGetInt(obj, "baseMobsKilled");
            stats.baseDeaths = safeGetInt(obj, "baseDeaths");
            stats.baseJumps = safeGetInt(obj, "baseJumps");
            stats.recordBlocks = safeGetInt(obj, "recordBlocks");
            stats.recordDistance = safeGetInt(obj, "recordDistance");
            stats.recordMobs = safeGetInt(obj, "recordMobs");
            stats.totalSleeps = safeGetInt(obj, "totalSleeps");

            return stats;
        }

        private int safeGetInt(JsonObject obj, String field) {
            if (!obj.has(field)) {
                return 0;
            }

            try {
                JsonElement element = obj.get(field);
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isNumber()) {
                        long value = primitive.getAsLong();
                        return clampToSafeInt(value);
                    }
                }
                return 0;
            } catch (Exception e) {
                LOGGER.warn("[StatsStorage] Failed to parse field {}, defaulting to 0", field);
                return 0;
            }
        }

        private int clampToSafeInt(long value) {
            if (value < 0) {
                return 0;
            }
            if (value > Integer.MAX_VALUE / 2) {
                return Integer.MAX_VALUE / 2;
            }
            return (int) value;
        }
    }
}