package mt.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import mt.config.MidnightThoughtsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class AchievementLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static List<AchievementDefinition> cached;

    public static List<AchievementDefinition> load() {
        if (cached != null) return cached;

        Path file = getFilePath();

        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            LOGGER.warn("Failed to create achievements directory: {}", e.getMessage());
            cached = Collections.emptyList();
            return cached;
        }

        if (!Files.exists(file)) {
            writeEmpty(file);
        }

        try {
            String json = Files.readString(file);
            AchievementsFile parsed = GSON.fromJson(json, AchievementsFile.class);
            if (parsed != null && parsed.achievements != null) {
                cached = parsed.achievements.stream()
                        .filter(a -> a != null && a.id != null && !a.id.isBlank() && a.name != null)
                        .toList();
                LOGGER.info("Loaded {} custom achievements", cached.size());
                return cached;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load achievements: {}", e.getMessage());
        }

        cached = Collections.emptyList();
        return cached;
    }

    public static void invalidateCache() {
        cached = null;
    }

    private static void writeEmpty(Path file) {
        try {
            String json = GSON.toJson(new AchievementsFile());
            Files.writeString(file, json);
            LOGGER.info("Created achievements file at {}", file);
        } catch (IOException e) {
            LOGGER.warn("Failed to write achievements file: {}", e.getMessage());
        }
    }

    private static Path getFilePath() {
        return MidnightThoughtsConfig.getConfigDir().resolve("achievements.json");
    }

    private static class AchievementsFile {
        @SerializedName("achievements")
        List<AchievementDefinition> achievements = Collections.emptyList();
    }
}