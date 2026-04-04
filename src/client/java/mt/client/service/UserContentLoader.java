package mt.client.service;

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

public class UserContentLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().create();

    private static final String[] ALL_CATEGORIES = {"facts", "lore", "nightmares", "special", "surreal"};
    private static final String[] ALL_LANGUAGES = {"en_us", "de_de"};

    private static final String EMPTY_CONTENT = "{\n  \"entries\": []\n}\n";

    public List<String> loadEntries(String language, String category) {
        Path file = getFilePath(language, category);

        if (!Files.exists(file)) {
            return Collections.emptyList();
        }

        try {
            String json = Files.readString(file);
            EntriesFile parsed = GSON.fromJson(json, EntriesFile.class);
            if (parsed != null && parsed.entries != null) {
                return parsed.entries.stream()
                        .filter(e -> e != null && e.text != null && !e.text.isBlank())
                        .map(e -> e.text)
                        .toList();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load user content {}/{}: {}", language, category, e.getMessage());
        }

        return Collections.emptyList();
    }

    public boolean hasEntries(String language, String category) {
        return Files.exists(getFilePath(language, category));
    }

    public void writeDefaultFiles() {
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

    private Path getFilePath(String language, String category) {
        return getDreamsDir().resolve(language).resolve(category + ".json");
    }

    private Path getDreamsDir() {
        return MidnightThoughtsConfig.getConfigDir().resolve("dreams");
    }

    private static class EntriesFile {
        @SerializedName("entries")
        List<Entry> entries;
    }

    private static class Entry {
        @SerializedName("text")
        String text;
        @SerializedName("rarity")
        float rarity = 1.0f;
    }
}