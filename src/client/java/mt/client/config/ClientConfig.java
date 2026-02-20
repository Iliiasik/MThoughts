package mt.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mt.config.MidnightThoughtsConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CLIENT_CONFIG_FILE_NAME = "midnightthoughts-client.json";

    private static ClientConfig instance;

    private String playerThemeOverride = null;

    private ClientConfig() {
    }

    public static ClientConfig getInstance() {
        if (instance == null) {
            instance = loadClientConfig();
        }
        return instance;
    }

    private static ClientConfig loadClientConfig() {
        Path configPath = getClientConfigPath();

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
        config.saveClientConfig();
        return config;
    }

    public void saveClientConfig() {
        Path configPath = getClientConfigPath();

        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            LOGGER.info("Client configuration saved to {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to save client configuration: {}", e.getMessage());
        }
    }

    private static Path getClientConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CLIENT_CONFIG_FILE_NAME);
    }

    public String getEffectiveTheme() {
        if (playerThemeOverride != null && !playerThemeOverride.isEmpty()) {
            return playerThemeOverride;
        }

        String serverTheme = MidnightThoughtsConfig.getInstance().getUi().theme;
        if (serverTheme != null && !serverTheme.isEmpty()) {
            return serverTheme;
        }

        return "magic";
    }

    public void setPlayerThemeOverride(String theme) {
        this.playerThemeOverride = theme;
        saveClientConfig();
    }

    public void cycleTheme() {
        String currentTheme = getEffectiveTheme();
        String nextTheme = switch (currentTheme) {
            case "magic" -> "classic";
            case "classic" -> "tech";
            case "tech" -> "magic";
            default -> "magic";
        };
        setPlayerThemeOverride(nextTheme);
    }
}
