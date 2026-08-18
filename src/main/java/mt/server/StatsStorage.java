package mt.server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StatsStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final String STATS_FILE = "midnightthoughts_stats.json";
    private static final Type STATS_TYPE = new TypeToken<Map<String, SavedPlayerStats>>() {}.getType();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(SavedPlayerStats.class, new StatsAdapter())
            .create();

    private static Path loadedFrom;
    private static Map<String, SavedPlayerStats> stats = new HashMap<>();
    private static boolean dirty;

    public static void savePlayerStats(MinecraftServer server, UUID playerUuid, SavedPlayerStats playerStats) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) {
            LOGGER.error("[StatsStorage] Save path is null, cannot save stats for UUID {}.", playerUuid);
            return;
        }
        ensureLoaded(savePath);
        stats.put(playerUuid.toString(), playerStats);
        dirty = true;
    }

    public static SavedPlayerStats loadPlayerStats(MinecraftServer server, UUID playerUuid) {
        Path savePath = getStatsFilePath(server);
        if (savePath == null) return null;
        ensureLoaded(savePath);
        return stats.get(playerUuid.toString());
    }

    public static void flush() {
        if (!dirty || loadedFrom == null) return;
        writeAtomically(loadedFrom);
    }

    public static void unload() {
        flush();
        loadedFrom = null;
        stats = new HashMap<>();
        dirty = false;
    }

    private static void ensureLoaded(Path savePath) {
        if (savePath.equals(loadedFrom)) return;
        if (dirty && loadedFrom != null) writeAtomically(loadedFrom);
        stats = readAll(savePath);
        loadedFrom = savePath;
        dirty = false;
    }

    private static void writeAtomically(Path savePath) {
        try {
            mt.common.AtomicFiles.writeString(savePath, GSON.toJson(stats, STATS_TYPE));
            dirty = false;
        } catch (IOException e) {
            LOGGER.error("[StatsStorage] Failed to save stats", e);
        }
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

    private static Map<String, SavedPlayerStats> readAll(Path savePath) {
        if (savePath == null || !Files.exists(savePath)) return new HashMap<>();

        try (Reader reader = Files.newBufferedReader(savePath)) {
            Map<String, SavedPlayerStats> result = GSON.fromJson(reader, STATS_TYPE);
            return result != null ? result : new HashMap<>();
        } catch (IOException e) {
            LOGGER.error("[StatsStorage] Failed to load stats", e);
        } catch (RuntimeException e) {
            LOGGER.error("[StatsStorage] Stats file is malformed and will be regenerated: {}", e.getMessage());
            backupBrokenFile(savePath);
        }
        return new HashMap<>();
    }

    private static void backupBrokenFile(Path savePath) {
        try {
            Path backup = savePath.resolveSibling(STATS_FILE + ".broken");
            Files.move(savePath, backup, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("[StatsStorage] Previous stats file saved as {}", backup.getFileName());
        } catch (IOException e) {
            LOGGER.error("[StatsStorage] Failed to back up the broken stats file: {}", e.getMessage());
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
            if (!obj.has(field)) {
                return 0;
            }
            try {
                JsonElement element = obj.get(field);
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    return clampToSafeInt(element.getAsJsonPrimitive().getAsLong());
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
