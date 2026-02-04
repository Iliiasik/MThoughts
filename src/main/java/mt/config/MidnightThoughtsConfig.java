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
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
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
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }

    public SleepOverlaySettings getSleepOverlay() {
        return sleepOverlay;
    }

    public WellRestedSettings getWellRested() {
        return wellRested;
    }

    public AchievementsSettings getAchievements() {
        return achievements;
    }

    public MvpSettings getMvp() {
        return mvp;
    }

    public ComfortSettings getComfort() {
        return comfort;
    }

    public int getFadeInDurationMs() {
        return sleepOverlay.fadeInDurationMs;
    }

    public int getFadeOutDurationMs() {
        return sleepOverlay.fadeOutDurationMs;
    }

    public float getOverlayOpacity() {
        return sleepOverlay.overlayOpacity;
    }

    public float getTextOpacity() {
        return sleepOverlay.textOpacity;
    }

    public float getSpecialSlideChance() {
        return sleepOverlay.specialSlideChance;
    }

    public boolean isEnableOverlay() {
        return sleepOverlay.enableOverlay;
    }

    public boolean isEnableImage() {
        return sleepOverlay.enableImage;
    }

    public int getRandomSlideDisplayTime() {
        return sleepOverlay.minSlideDisplayTimeMs + (int) (Math.random() * (sleepOverlay.maxSlideDisplayTimeMs - sleepOverlay.minSlideDisplayTimeMs));
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
    }

    public static class WellRestedSettings {
        public Map<String, WellRestedLevel> levels = new HashMap<>();

        public WellRestedSettings() {
            levels.put("level1", new WellRestedLevel(3, 2.0, 0.25, 0.005f, 2.0f));
            levels.put("level2", new WellRestedLevel(5, 2.0, 0.25, 0.01f, 2.0f));
            levels.put("level3", new WellRestedLevel(7, 2.0, 0.25, 0.015f, 4.0f));
            levels.put("level4", new WellRestedLevel(10, 2.0, 0.25, 0.02f, 4.0f));
            levels.put("level5", new WellRestedLevel(15, 2.0, 0.25, 0.025f, 4.0f));
        }

        public WellRestedLevel getLevel(int level) {
            return levels.getOrDefault("level" + level, levels.get("level1"));
        }
    }

    public static class WellRestedLevel {
        public int durationMinutes;
        public double healthBonus;
        public double luckBonus;
        public float exhaustionReduction;
        public float instantHeal;

        public WellRestedLevel() {
        }

        public WellRestedLevel(int durationMinutes, double healthBonus, double luckBonus, float exhaustionReduction, float instantHeal) {
            this.durationMinutes = durationMinutes;
            this.healthBonus = healthBonus;
            this.luckBonus = luckBonus;
            this.exhaustionReduction = exhaustionReduction;
            this.instantHeal = instantHeal;
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
        public Integer deaths;
        public Integer mobsMin;
        public Integer mobsMax;
        public Integer blocksMin;
        public Integer distanceMin;
        public Integer deathsMax;
        public Integer jumpsMin;

        public AchievementRequirement() {
        }

        public AchievementRequirement(Integer deaths, Integer mobsMin, Integer mobsMax, Integer blocksMin, Integer distanceMin) {
            this.deaths = deaths;
            this.mobsMin = mobsMin;
            this.mobsMax = mobsMax;
            this.blocksMin = blocksMin;
            this.distanceMin = distanceMin;
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
    }

    public static class ComfortSettings {
        public boolean enabled = true;
        public int scanRadius = 5;
    }
}

