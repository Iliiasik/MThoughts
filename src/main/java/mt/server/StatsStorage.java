package mt.server;

import com.google.gson.*;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StatsStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final String STATS_FILE = "midnightthoughts_stats.json";
    private static final int MAX_SAFE_VALUE = Integer.MAX_VALUE / 2;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(SavedPlayerStats.class, new StatsAdapter())
            .create();

    public static void savePlayerStats(MinecraftServer server, UUID playerUuid, SavedPlayerStats stats) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) {
            LOGGER.error("[StatsStorage] Save path is null, cannot save stats for UUID {}.", playerUuid);
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
        if (savePath == null) return null;

        Map<String, SavedPlayerStats> allStats = loadAllStats(savePath);
        return allStats.get(playerUuid.toString());
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
        if (savePath == null || !Files.exists(savePath)) return new HashMap<>();

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
        public int baseDamageDealt;

        public int recordBlocks;
        public int recordDistance;
        public int recordMobs;
        public int totalSleeps;

        public Set<String> unlockedAchievements = new HashSet<>();

        public SavedPlayerStats() {}

        public SavedPlayerStats(int baseBlocksDestroyed, int baseDistanceWalked, int baseMobsKilled,
                                int baseDeaths, int baseJumps, int baseDamageDealt) {
            this.baseBlocksDestroyed = baseBlocksDestroyed;
            this.baseDistanceWalked = baseDistanceWalked;
            this.baseMobsKilled = baseMobsKilled;
            this.baseDeaths = baseDeaths;
            this.baseJumps = baseJumps;
            this.baseDamageDealt = baseDamageDealt;
        }
    }

    private static class StatsAdapter implements JsonSerializer<SavedPlayerStats>, JsonDeserializer<SavedPlayerStats> {
        @Override
        public JsonElement serialize(SavedPlayerStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("baseBlocksDestroyed", src.baseBlocksDestroyed);
            obj.addProperty("baseDistanceWalked", src.baseDistanceWalked);
            obj.addProperty("baseMobsKilled", src.baseMobsKilled);
            obj.addProperty("baseDeaths", src.baseDeaths);
            obj.addProperty("baseJumps", src.baseJumps);
            obj.addProperty("baseDamageDealt", src.baseDamageDealt);
            obj.addProperty("recordBlocks", src.recordBlocks);
            obj.addProperty("recordDistance", src.recordDistance);
            obj.addProperty("recordMobs", src.recordMobs);
            obj.addProperty("totalSleeps", src.totalSleeps);
            obj.add("unlockedAchievements", context.serialize(src.unlockedAchievements));
            return obj;
        }

        @Override
        public SavedPlayerStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            SavedPlayerStats stats = new SavedPlayerStats();
            stats.baseBlocksDestroyed = safeGetInt(obj, "baseBlocksDestroyed");
            stats.baseDistanceWalked = safeGetInt(obj, "baseDistanceWalked");
            stats.baseMobsKilled = safeGetInt(obj, "baseMobsKilled");
            stats.baseDeaths = safeGetInt(obj, "baseDeaths");
            stats.baseJumps = safeGetInt(obj, "baseJumps");
            stats.baseDamageDealt = safeGetInt(obj, "baseDamageDealt");
            stats.recordBlocks = safeGetInt(obj, "recordBlocks");
            stats.recordDistance = safeGetInt(obj, "recordDistance");
            stats.recordMobs = safeGetInt(obj, "recordMobs");
            stats.totalSleeps = safeGetInt(obj, "totalSleeps");
            stats.unlockedAchievements = safeGetStringSet(obj);
            return stats;
        }

        private int safeGetInt(JsonObject obj, String field) {
            if (obj.has(field)) {
                JsonElement element = obj.get(field);
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    long value = element.getAsJsonPrimitive().getAsLong();
                    return value < 0 ? 0 : (int) Math.min(value, MAX_SAFE_VALUE);
                }
            }
            return 0;
        }

        private Set<String> safeGetStringSet(JsonObject obj) {
            Set<String> result = new HashSet<>();
            if (obj.has("unlockedAchievements") && obj.get("unlockedAchievements").isJsonArray()) {
                for (JsonElement item : obj.getAsJsonArray("unlockedAchievements")) {
                    if (item.isJsonPrimitive()) result.add(item.getAsString());
                }
            }
            return result;
        }
    }
}