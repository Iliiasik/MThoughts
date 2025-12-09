package mt.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MidnightThoughtsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "midnightthoughts.json";

    private static MidnightThoughtsConfig instance;

    private int minSlideDisplayTimeMs = 2500;
    private int maxSlideDisplayTimeMs = 4000;
    private int fadeInDurationMs = 300;
    private int fadeOutDurationMs = 300;
    private float overlayOpacity = 0.4f;
    private float textOpacity = 1.0f;
    private int statsSlideFrequency = 3;
    private float specialSlideChance = 0.05f;
    private boolean enableOverlay = true;
    private boolean enableImage = true;

    private MidnightThoughtsConfig() {
    }

    public static MidnightThoughtsConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static MidnightThoughtsConfig load() {
        Path configPath = getConfigPath();

        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                MidnightThoughtsConfig config = GSON.fromJson(json, MidnightThoughtsConfig.class);
                if (config != null) {
                    LOGGER.info("Configuration loaded from {}", configPath);
                    return config;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load configuration: {}", e.getMessage());
            }
        }

        MidnightThoughtsConfig config = new MidnightThoughtsConfig();
        config.save();
        return config;
    }

    public void save() {
        Path configPath = getConfigPath();

        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            LOGGER.info("Configuration saved to {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }


    public int getFadeInDurationMs() {
        return fadeInDurationMs;
    }

    public int getFadeOutDurationMs() {
        return fadeOutDurationMs;
    }

    public float getOverlayOpacity() {
        return overlayOpacity;
    }

    public float getTextOpacity() {
        return textOpacity;
    }

    public int getStatsSlideFrequency() {
        return statsSlideFrequency;
    }

    public float getSpecialSlideChance() {
        return specialSlideChance;
    }

    public boolean isEnableOverlay() {
        return enableOverlay;
    }

    public boolean isEnableImage() {
        return enableImage;
    }


    public int getRandomSlideDisplayTime() {
        return minSlideDisplayTimeMs + (int) (Math.random() * (maxSlideDisplayTimeMs - minSlideDisplayTimeMs));
    }
}

