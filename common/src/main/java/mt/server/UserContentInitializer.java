package mt.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mt.config.MidnightThoughtsConfig;
import mt.network.packet.UserContentPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserContentInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final String[] ALL_CATEGORIES = {"facts", "lore", "nightmares", "special", "surreal"};
    private static final List<String> ALL_LANGUAGES = mt.common.SupportedLanguages.codes();
    private static final String EMPTY_CONTENT = "{\n  \"entries\": []\n}\n";

    public static final int MAX_ENTRY_LENGTH = 4096;
    public static final int MAX_PAYLOAD_BYTES = 512 * 1024;

    private static UserContentPacket cachedPacket;

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
        UserContentPacket cached = cachedPacket;
        if (cached == null) {
            Map<String, List<String>> content = collectContent();
            cached = new UserContentPacket(content);
            cachedPacket = cached;
            int entries = content.values().stream().mapToInt(List::size).sum();
            if (entries > 0) {
                LOGGER.info("Prepared {} user content entries across {} categories", entries, content.size());
            }
        }
        return cached;
    }

    public static void invalidateCache() {
        cachedPacket = null;
    }

    private static Map<String, List<String>> collectContent() {
        Map<String, List<String>> content = new HashMap<>();
        int remaining = MAX_PAYLOAD_BYTES;

        for (String language : ALL_LANGUAGES) {
            for (String category : ALL_CATEGORIES) {
                List<String> entries = readEntries(language, category);
                if (entries.isEmpty()) continue;

                String key = language + ":" + category;
                remaining -= utf8Length(key);
                List<String> accepted = new ArrayList<>(entries.size());

                for (String entry : entries) {
                    int size = utf8Length(entry);
                    if (size > remaining) {
                        if (!accepted.isEmpty()) content.put(key, accepted);
                        LOGGER.warn("User content exceeds the {} KB budget and was truncated at {}/{}, "
                                        + "trim the files in config/midnightthoughts/dreams",
                                MAX_PAYLOAD_BYTES / 1024, language, category);
                        return content;
                    }
                    remaining -= size;
                    accepted.add(entry);
                }

                content.put(key, accepted);
            }
        }
        return content;
    }

    private static List<String> readEntries(String language, String category) {
        Path file = getDreamsDir().resolve(language).resolve(category + ".json");
        if (!Files.exists(file)) return List.of();
        try {
            String json = Files.readString(file);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has("entries")) return List.of();
            List<String> result = new ArrayList<>();
            int skipped = 0;
            for (JsonElement el : obj.getAsJsonArray("entries")) {
                if (!el.isJsonObject()) continue;
                JsonObject entry = el.getAsJsonObject();
                if (!entry.has("text")) continue;
                String text = entry.get("text").getAsString();
                if (text.isBlank()) continue;
                if (text.length() > MAX_ENTRY_LENGTH) {
                    skipped++;
                    continue;
                }
                result.add(text);
            }
            if (skipped > 0) {
                LOGGER.warn("Skipped {} entries longer than {} characters in dreams/{}/{}.json",
                        skipped, MAX_ENTRY_LENGTH, language, category);
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("Failed to read entries {}/{}: {}", language, category, e.getMessage());
            return List.of();
        }
    }

    private static int utf8Length(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }

    private static Path getDreamsDir() {
        return MidnightThoughtsConfig.getConfigDir().resolve("dreams");
    }
}
