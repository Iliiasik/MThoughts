package mt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mt.client.config.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class MidnightThoughtsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static MidnightThoughtsConfig instance;

    private SleepOverlaySettings sleepOverlay = new SleepOverlaySettings();
    private WellRestedSettings wellRested = new WellRestedSettings();
    private MvpSettings mvp = new MvpSettings();
    private ComfortSettings comfort = new ComfortSettings();
    private ServerSettings server = new ServerSettings();
    private UISettings ui = new UISettings();

    private MidnightThoughtsConfig() {}

    public static MidnightThoughtsConfig getInstance() {
        if (instance == null) instance = load();
        return instance;
    }

    public static MidnightThoughtsConfig load() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                MidnightThoughtsConfig config = GSON.fromJson(json, MidnightThoughtsConfig.class);
                if (config != null) {
                    config.validate();
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
            Files.writeString(configPath, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    private void validate() {
        if (sleepOverlay == null) sleepOverlay = new SleepOverlaySettings();
        if (wellRested == null) wellRested = new WellRestedSettings();
        if (mvp == null) mvp = new MvpSettings();
        if (comfort == null) comfort = new ComfortSettings();
        if (server == null) server = new ServerSettings();
        if (ui == null) ui = new UISettings();
    }

    public static Path getConfigDir() {
        return Paths.get(System.getProperty("user.dir"), "config", "midnightthoughts");
    }

    private static Path getConfigPath() {
        return getConfigDir().resolve("midnightthoughts.json");
    }

    public SleepOverlaySettings getSleepOverlay() { return sleepOverlay; }
    public WellRestedSettings getWellRested() { return wellRested; }
    public MvpSettings getMvp() { return mvp; }
    public ComfortSettings getComfort() { return comfort; }
    public ServerSettings getServer() { return server; }
    public UISettings getUi() { return ui; }

    public String getUiTheme() {
        return ClientConfig.getInstance().getEffectiveTheme();
    }

    public float getOverlayOpacity() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().overlayOpacity();
        return sleepOverlay.overlayOpacity;
    }

    public float getSpecialSlideChance() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().specialSlideChance();
        return sleepOverlay.specialSlideChance;
    }

    public boolean isEnableOverlay() {
        if (mt.cache.ServerConfigCache.has()) return !mt.cache.ServerConfigCache.get().enableOverlay();
        return !sleepOverlay.enableOverlay;
    }

    public boolean isEnableImage() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().enableImage();
        return sleepOverlay.enableImage;
    }

    public boolean isEnableDailySummaryScreen() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().enableDailySummaryScreen();
        return sleepOverlay.enableDailySummaryScreen;
    }

    public boolean isUseFactsApi() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().useFactsApi();
        return sleepOverlay.useFactsApi;
    }

    public boolean isUserContentReplaces() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().userContentReplaces();
        return sleepOverlay.userContentReplaces;
    }

    public boolean isHideChatWhenSleeping() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().hideChatWhenSleeping();
        return sleepOverlay.hideChatWhenSleeping;
    }

    public boolean isHideWellRestedHud() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().hideWellRestedHud();
        return ui.hideWellRestedHud;
    }

    public boolean isHideThemeSwitchButton() {
        if (mt.cache.ServerConfigCache.has()) return mt.cache.ServerConfigCache.get().hideThemeSwitchButton();
        return ui.hideThemeSwitchButton;
    }

    public int getRandomSlideDisplayTime() {
        int min = mt.cache.ServerConfigCache.has()
                ? mt.cache.ServerConfigCache.get().minSlideDisplayTimeMs()
                : sleepOverlay.minSlideDisplayTimeMs;
        int max = mt.cache.ServerConfigCache.has()
                ? mt.cache.ServerConfigCache.get().maxSlideDisplayTimeMs()
                : sleepOverlay.maxSlideDisplayTimeMs;
        if (min >= max) return min;
        return min + (int) (Math.random() * (max - min));
    }

    public static class SleepOverlaySettings {
        public int minSlideDisplayTimeMs = 6000;
        public int maxSlideDisplayTimeMs = 8000;
        public float overlayOpacity = 0.4f;
        public float specialSlideChance = 0.05f;
        public boolean enableOverlay = true;
        public boolean enableImage = true;
        public boolean enableDailySummaryScreen = true;
        public boolean useFactsApi = true;
        public boolean userContentReplaces = false;
        public boolean hideChatWhenSleeping = true;
    }

    public static class WellRestedSettings {
        public Map<String, WellRestedLevel> levels = new HashMap<>();

        public WellRestedSettings() {
            levels.put("level1", new WellRestedLevel(3,  0.16f, 0.08f, 0.04f,  0.08f, 0.04f, 0.02f,  0.10f, 0.05f, 0.02f,  0.04f, 0.02f, 0.01f,  2.0f, 0.02f));
            levels.put("level2", new WellRestedLevel(5,  0.24f, 0.12f, 0.06f,  0.12f, 0.06f, 0.03f,  0.16f, 0.08f, 0.04f,  0.06f, 0.03f, 0.01f,  4.0f, 0.04f));
            levels.put("level3", new WellRestedLevel(7,  0.32f, 0.18f, 0.08f,  0.16f, 0.08f, 0.04f,  0.22f, 0.12f, 0.06f,  0.08f, 0.04f, 0.02f,  6.0f, 0.06f));
            levels.put("level4", new WellRestedLevel(10, 0.40f, 0.24f, 0.12f,  0.20f, 0.12f, 0.06f,  0.28f, 0.16f, 0.08f,  0.10f, 0.05f, 0.02f,  8.0f, 0.08f));
            levels.put("level5", new WellRestedLevel(15, 0.50f, 0.30f, 0.16f,  0.24f, 0.16f, 0.08f,  0.35f, 0.20f, 0.10f,  0.12f, 0.06f, 0.03f, 10.0f, 0.10f));
        }

        public WellRestedLevel getLevel(int level) {
            return levels.getOrDefault("level" + level, levels.get("level1"));
        }
    }

    public static class WellRestedLevel {
        public int durationMinutes;
        public float speedPhase1, speedPhase2, speedPhase3;
        public float strengthPhase1, strengthPhase2, strengthPhase3;
        public float hastePhase1, hastePhase2, hastePhase3;
        public float attackSpeedPhase1, attackSpeedPhase2, attackSpeedPhase3;
        public float healthBonus;
        public float regenBonus;

        public WellRestedLevel(int durationMinutes,
                               float speedPhase1, float speedPhase2, float speedPhase3,
                               float strengthPhase1, float strengthPhase2, float strengthPhase3,
                               float hastePhase1, float hastePhase2, float hastePhase3,
                               float attackSpeedPhase1, float attackSpeedPhase2, float attackSpeedPhase3,
                               float healthBonus, float regenBonus) {
            this.durationMinutes = durationMinutes;
            this.speedPhase1 = speedPhase1; this.speedPhase2 = speedPhase2; this.speedPhase3 = speedPhase3;
            this.strengthPhase1 = strengthPhase1; this.strengthPhase2 = strengthPhase2; this.strengthPhase3 = strengthPhase3;
            this.hastePhase1 = hastePhase1; this.hastePhase2 = hastePhase2; this.hastePhase3 = hastePhase3;
            this.attackSpeedPhase1 = attackSpeedPhase1; this.attackSpeedPhase2 = attackSpeedPhase2; this.attackSpeedPhase3 = attackSpeedPhase3;
            this.healthBonus = healthBonus;
            this.regenBonus = regenBonus;
        }
    }

    public static class MvpSettings {
        public boolean enabled = true;
        public int minScoreRequired = 10;
        public int pointsPerDistance100 = 2;
        public int pointsPerBlock = 3;
        public int pointsPerMob = 15;
        public int pointsPerJump10 = 1;
        public int penaltyPerDeath = 30;
        public int mvpWellRestedDurationMinutes = 21;
    }

    public static class ComfortSettings {
        public boolean enabled = true;
        public int scanRadius = 5;
    }

    public static class ServerSettings {
        public boolean resetPhantomTimerForNonSleepers = true;
    }

    public static class UISettings {
        public String theme = "classic";
        public boolean hideWellRestedHud = false;
        public boolean hideThemeSwitchButton = false;
    }
}