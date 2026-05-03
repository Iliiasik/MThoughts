package mt.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mt.config.MidnightThoughtsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ClientConfig instance;
    private String theme = null;

    private ClientConfig() {}

    public static ClientConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static ClientConfig load() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                ClientConfig config = GSON.fromJson(json, ClientConfig.class);
                if (config != null) {
                    LOGGER.info("Client configuration loaded from {}", configPath);
                    return config;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load client configuration: {}", e.getMessage());
            }
        }
        ClientConfig config = new ClientConfig();
        config.save();
        return config;
    }

    public void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            LOGGER.info("Client configuration saved to {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to save client configuration: {}", e.getMessage());
        }
    }

    private static Path getConfigPath() {
        return MidnightThoughtsConfig.getConfigDir().resolve("midnightthoughts-client.json");
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
        save();
    }

    public String getEffectiveTheme() {
        if (theme != null && !theme.isEmpty()) {
            return theme;
        }
        if (mt.cache.ServerConfigCache.has()) {
            return mt.cache.ServerConfigCache.get().theme();
        }
        return MidnightThoughtsConfig.getInstance().getUi().theme;
    }

    public void cycleTheme() {
        String currentTheme = getEffectiveTheme();
        String nextTheme = switch (currentTheme) {
            case "vanilla" -> "magic";
            case "magic" -> "classic";
            case "classic" -> "tech";
            case "tech" -> "vanilla";
            default -> "vanilla";
        };
        setTheme(nextTheme);
    }
}