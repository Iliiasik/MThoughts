package mt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class MidnightThoughtsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "midnightthoughts.json";

    private static MidnightThoughtsConfig instance;

    private SleepOverlaySettings sleepOverlay = new SleepOverlaySettings();
    private WellRestedSettings wellRested = new WellRestedSettings();
    private AchievementsSettings achievements = new AchievementsSettings();
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
            Files.writeString(configPath, GSON.toJson(this));
            LOGGER.info("Configuration saved to {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    private void validate() {
        if (sleepOverlay == null) sleepOverlay = new SleepOverlaySettings();
        if (wellRested == null) wellRested = new WellRestedSettings();
        if (achievements == null) achievements = new AchievementsSettings();
        if (mvp == null) mvp = new MvpSettings();
        if (comfort == null) comfort = new ComfortSettings();
        if (server == null) server = new ServerSettings();
        if (ui == null) ui = new UISettings();
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }

    public SleepOverlaySettings getSleepOverlay() { return sleepOverlay; }
    public WellRestedSettings getWellRested() { return wellRested; }
    public AchievementsSettings getAchievements() { return achievements; }
    public MvpSettings getMvp() { return mvp; }
    public ComfortSettings getComfort() { return comfort; }
    public ServerSettings getServer() { return server; }
    public UISettings getUi() { return ui; }

    public int getFadeInDurationMs() { return sleepOverlay.fadeInDurationMs; }
    public int getFadeOutDurationMs() { return sleepOverlay.fadeOutDurationMs; }
    public float getOverlayOpacity() { return sleepOverlay.overlayOpacity; }
    public float getTextOpacity() { return sleepOverlay.textOpacity; }
    public float getSpecialSlideChance() { return sleepOverlay.specialSlideChance; }
    public boolean isEnableOverlay() { return sleepOverlay.enableOverlay; }
    public boolean isEnableImage() { return sleepOverlay.enableImage; }
    public boolean isEnableDailySummaryScreen() { return sleepOverlay.enableDailySummaryScreen; }

    public int getRandomSlideDisplayTime() {
        return sleepOverlay.minSlideDisplayTimeMs + (int)(Math.random() * (sleepOverlay.maxSlideDisplayTimeMs - sleepOverlay.minSlideDisplayTimeMs));
    }

    public static class SleepOverlaySettings {
        public int minSlideDisplayTimeMs = 2500;
        public int maxSlideDisplayTimeMs = 4000;
        public int fadeInDurationMs = 300;
        public int fadeOutDurationMs = 300;
        public float overlayOpacity = 0.4f;
        public float textOpacity = 1.0f;
        public float imageOpacity = 0.6f;
        public float specialSlideChance = 0.05f;
        public boolean enableOverlay = true;
        public boolean enableImage = true;
        public boolean enableDailySummaryScreen = true;
    }

    public static class WellRestedSettings {
        public Map<String, WellRestedLevel> levels = new HashMap<>();

        public WellRestedSettings() {
            levels.put("level1", new WellRestedLevel(3,  0.16f, 0.08f, 0.04f,  0.08f, 0.04f, 0.02f,  0.04f, 0.02f, 0.01f,  2.0f, 0.02f));
            levels.put("level2", new WellRestedLevel(5,  0.24f, 0.12f, 0.06f,  0.12f, 0.06f, 0.03f,  0.06f, 0.03f, 0.01f,  4.0f, 0.04f));
            levels.put("level3", new WellRestedLevel(7,  0.32f, 0.18f, 0.08f,  0.16f, 0.08f, 0.04f,  0.08f, 0.04f, 0.02f,  6.0f, 0.06f));
            levels.put("level4", new WellRestedLevel(10, 0.40f, 0.24f, 0.12f,  0.20f, 0.12f, 0.06f,  0.10f, 0.05f, 0.02f,  8.0f, 0.08f));
            levels.put("level5", new WellRestedLevel(15, 0.50f, 0.30f, 0.16f,  0.24f, 0.16f, 0.08f,  0.12f, 0.06f, 0.03f, 10.0f, 0.10f));
        }

        public WellRestedLevel getLevel(int level) {
            return levels.getOrDefault("level" + level, levels.get("level1"));
        }
    }

    public static class WellRestedLevel {
        public int durationMinutes;
        public float speedPhase1, speedPhase2, speedPhase3;
        public float strengthPhase1, strengthPhase2, strengthPhase3;
        public float attackSpeedPhase1, attackSpeedPhase2, attackSpeedPhase3;
        public float healthBonus;
        public float regenBonus;

        public WellRestedLevel() {}

        public WellRestedLevel(int durationMinutes,
                               float speedPhase1, float speedPhase2, float speedPhase3,
                               float strengthPhase1, float strengthPhase2, float strengthPhase3,
                               float attackSpeedPhase1, float attackSpeedPhase2, float attackSpeedPhase3,
                               float healthBonus, float regenBonus) {
            this.durationMinutes = durationMinutes;
            this.speedPhase1 = speedPhase1; this.speedPhase2 = speedPhase2; this.speedPhase3 = speedPhase3;
            this.strengthPhase1 = strengthPhase1; this.strengthPhase2 = strengthPhase2; this.strengthPhase3 = strengthPhase3;
            this.attackSpeedPhase1 = attackSpeedPhase1; this.attackSpeedPhase2 = attackSpeedPhase2; this.attackSpeedPhase3 = attackSpeedPhase3;
            this.healthBonus = healthBonus;
            this.regenBonus = regenBonus;
        }
    }

    public static class AchievementsSettings {
        public Map<String, AchievementRequirement> requirements = new HashMap<>();

        public AchievementsSettings() {
            requirements.put("flawless", new AchievementRequirement(0, 10, null, 50, null));
            requirements.put("pacifist", new AchievementRequirement(null, null, 0, null, 500000));
            requirements.put("juggernaut", new AchievementRequirement(null, 50, null, null, null, 1));
            requirements.put("marathoner", new AchievementRequirement(null, null, null, null, 3000000));
            requirements.put("hyperactive", new AchievementRequirement(null, null, null, null, null, null, 500));
            requirements.put("demolition_maniac", new AchievementRequirement(null, null, null, 1000, null, null, 200));
            requirements.put("explorer", new AchievementRequirement(null, null, null, 100, 1500000));
            requirements.put("survivor", new AchievementRequirement(null, 10, null, null, null, 1));
            requirements.put("combo_master", new AchievementRequirement(null, 20, null, 200, 1000000));
            requirements.put("iron_will", new AchievementRequirement(0, null, null, 200, 2000000));
        }

        public AchievementRequirement getRequirement(String achievementId) {
            return requirements.getOrDefault(achievementId, new AchievementRequirement());
        }
    }

    public static class AchievementRequirement {
        public Integer deaths, mobsMin, mobsMax, blocksMin, distanceMin, deathsMax, jumpsMin;

        public AchievementRequirement() {}

        public AchievementRequirement(Integer deaths, Integer mobsMin, Integer mobsMax, Integer blocksMin, Integer distanceMin) {
            this.deaths = deaths; this.mobsMin = mobsMin; this.mobsMax = mobsMax;
            this.blocksMin = blocksMin; this.distanceMin = distanceMin;
        }

        public AchievementRequirement(Integer deaths, Integer mobsMin, Integer mobsMax, Integer blocksMin, Integer distanceMin, Integer deathsMax) {
            this(deaths, mobsMin, mobsMax, blocksMin, distanceMin);
            this.deathsMax = deathsMax;
        }

        public AchievementRequirement(Integer deaths, Integer mobsMin, Integer mobsMax, Integer blocksMin, Integer distanceMin, Integer deathsMax, Integer jumpsMin) {
            this(deaths, mobsMin, mobsMax, blocksMin, distanceMin, deathsMax);
            this.jumpsMin = jumpsMin;
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
        public String theme = "magic";
    }
}