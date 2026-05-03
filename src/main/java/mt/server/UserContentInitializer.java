package mt.server;

import mt.config.MidnightThoughtsConfig;
import mt.network.packet.UserContentPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserContentInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final String[] ALL_CATEGORIES = {"facts", "lore", "nightmares", "special", "surreal"};
    private static final String[] ALL_LANGUAGES = {"en_us", "de_de"};
    private static final String EMPTY_CONTENT = "{\n  \"entries\": []\n}\n";

    public static void writeDefaultFiles() {
        for (String language : ALL_LANGUAGES) {
            Path dir = getDreamsDir().resolve(language);
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                LOGGER.warn("Failed to create dreams directory {}: {}", dir, e.getMessage());
                continue;
            }
            for (String category : ALL_CATEGORIES) {
                Path file = dir.resolve(category + ".json");
                if (!Files.exists(file)) {
                    try {
                        Files.writeString(file, EMPTY_CONTENT);
                        LOGGER.info("Created content file at {}", file);
                    } catch (IOException e) {
                        LOGGER.warn("Failed to write file {}: {}", file, e.getMessage());
                    }
                }
            }
        }
    }

    public static UserContentPacket buildPacket() {
        Map<String, List<String>> content = new HashMap<>();
        for (String language : ALL_LANGUAGES) {
            for (String category : ALL_CATEGORIES) {
                String key = language + ":" + category;
                List<String> entries = readEntries(language, category);
                if (!entries.isEmpty()) {
                    content.put(key, entries);
                }
            }
        }
        return new UserContentPacket(content);
    }

    private static List<String> readEntries(String language, String category) {
        Path file = getDreamsDir().resolve(language).resolve(category + ".json");
        if (!Files.exists(file)) return List.of();
        try {
            String json = Files.readString(file);
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has("entries")) return List.of();
            List<String> result = new ArrayList<>();
            for (com.google.gson.JsonElement el : obj.getAsJsonArray("entries")) {
                if (el.isJsonObject()) {
                    com.google.gson.JsonObject entry = el.getAsJsonObject();
                    if (entry.has("text")) {
                        String text = entry.get("text").getAsString();
                        if (!text.isBlank()) result.add(text);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("Failed to read entries {}/{}: {}", language, category, e.getMessage());
            return List.of();
        }
    }

    private static Path getDreamsDir() {
        return MidnightThoughtsConfig.getConfigDir().resolve("dreams");
    }
}