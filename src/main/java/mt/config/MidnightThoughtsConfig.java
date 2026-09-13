package mt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mt.cache.ServerConfigCache;
import mt.network.packet.SyncConfigPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MidnightThoughtsConfig {
    public static final String HUD_POSITION_LEFT = "left";
    public static final String HUD_POSITION_BAR = "bar";
    public static final String HUD_POSITION_RIGHT = "right";

    public static final String ATTRIBUTE_MOVEMENT_SPEED = "minecraft:generic.movement_speed";
    public static final String ATTRIBUTE_ATTACK_DAMAGE = "minecraft:generic.attack_damage";
    public static final String ATTRIBUTE_ATTACK_SPEED = "minecraft:generic.attack_speed";
    public static final String ATTRIBUTE_MAX_HEALTH = "minecraft:generic.max_health";
    public static final String ATTRIBUTE_BLOCK_BREAK_SPEED = "minecraft:player.block_break_speed";

    public static final String OPERATION_ADD_VALUE = "add_value";
    public static final String OPERATION_MULTIPLIED_BASE = "add_multiplied_base";

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
            MidnightThoughtsConfig config = null;
            try {
                config = GSON.fromJson(Files.readString(configPath), MidnightThoughtsConfig.class);
            } catch (IOException e) {
                LOGGER.error("Failed to read configuration: {}", e.getMessage());
                backupBrokenConfig(configPath);
            } catch (RuntimeException e) {
                LOGGER.error("Configuration is malformed and will be regenerated: {}", e.getMessage());
                backupBrokenConfig(configPath);
            }
            if (config != null) {
                config.validate();
                config.save();
                return config;
            }
        }
        MidnightThoughtsConfig config = new MidnightThoughtsConfig();
        config.save();
        return config;
    }

    private static void backupBrokenConfig(Path configPath) {
        try {
            Path backup = configPath.resolveSibling("midnightthoughts.json.broken");
            Files.move(configPath, backup, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("Previous configuration saved as {}", backup.getFileName());
        } catch (IOException e) {
            LOGGER.error("Failed to back up broken configuration: {}", e.getMessage());
        }
    }

    public void save() {
        Path configPath = getConfigPath();
        try {
            mt.common.AtomicFiles.writeString(configPath, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    private void validate() {
        if (sleepOverlay == null) sleepOverlay = new SleepOverlaySettings();
        if (wellRested == null) wellRested = new WellRestedSettings();
        if (mvp == null) mvp = new MvpSettings();
        if (comfort == null) comfort = new ComfortSettings();
        if (comfort.weights == null) comfort.weights = new ComfortWeights();
        if (server == null) server = new ServerSettings();
        if (ui == null) ui = new UISettings();

        MidnightThoughtsConfigValidation.apply(this);
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

    public String getWellRestedHudPosition() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.wellRestedHudPosition() : ui.wellRestedHudPosition;
    }

    public int getFadeInDurationMs() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.fadeInDurationMs() : sleepOverlay.fadeInDurationMs;
    }

    public int getFadeOutDurationMs() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.fadeOutDurationMs() : sleepOverlay.fadeOutDurationMs;
    }

    public float getOverlayOpacity() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.overlayOpacity() : sleepOverlay.overlayOpacity;
    }

    public float getTextOpacity() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.textOpacity() : sleepOverlay.textOpacity;
    }

    public float getImageOpacity() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.imageOpacity() : sleepOverlay.imageOpacity;
    }

    public float getSpecialSlideChance() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.specialSlideChance() : sleepOverlay.specialSlideChance;
    }

    public boolean isEnableOverlay() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.enableOverlay() : sleepOverlay.enableOverlay;
    }

    public boolean isEnableImage() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.enableImage() : sleepOverlay.enableImage;
    }

    public boolean isEnableDailySummaryScreen() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.enableDailySummaryScreen() : sleepOverlay.enableDailySummaryScreen;
    }

    public boolean isUseFactsApi() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.useFactsApi() : sleepOverlay.useFactsApi;
    }

    public boolean isUserContentReplaces() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.userContentReplaces() : sleepOverlay.userContentReplaces;
    }

    public boolean isHideChatWhenSleeping() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.hideChatWhenSleeping() : sleepOverlay.hideChatWhenSleeping;
    }

    public boolean isHideHudMessagesWhenSleeping() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.hideHudMessagesWhenSleeping() : sleepOverlay.hideHudMessagesWhenSleeping;
    }

    public float getOverlayTextScale() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.overlayTextScale() : sleepOverlay.textScale;
    }

    public float getOverlayImageScale() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.overlayImageScale() : sleepOverlay.imageScale;
    }

    public boolean isHideWellRestedHud() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.hideWellRestedHud() : ui.hideWellRestedHud;
    }

    public boolean isHideSleepingPlayersHud() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.hideSleepingPlayersHud() : ui.hideSleepingPlayersHud;
    }

    public boolean isHideThemeSwitchButton() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.hideThemeSwitchButton() : ui.hideThemeSwitchButton;
    }

    public boolean isEnableStarDust() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.enableStarDust() : sleepOverlay.enableStarDust;
    }

    public boolean isShowSlideProgress() {
        SyncConfigPacket s = ServerConfigCache.get();
        return s != null ? s.showSlideProgress() : sleepOverlay.showSlideProgress;
    }

    public int getRandomSlideDisplayTime() {
        SyncConfigPacket s = ServerConfigCache.get();
        int min = s != null ? s.minSlideDisplayTimeMs() : sleepOverlay.minSlideDisplayTimeMs;
        int max = s != null ? s.maxSlideDisplayTimeMs() : sleepOverlay.maxSlideDisplayTimeMs;
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static class SleepOverlaySettings {
        public int minSlideDisplayTimeMs = 6000;
        public int maxSlideDisplayTimeMs = 8000;
        public int fadeInDurationMs = 300;
        public int fadeOutDurationMs = 300;
        public float overlayOpacity = 0.4f;
        public float textOpacity = 1.0f;
        public float imageOpacity = 0.6f;
        public float specialSlideChance = 0.05f;
        public boolean enableStarDust = true;
        public boolean showSlideProgress = true;
        public boolean enableOverlay = true;
        public boolean enableImage = true;
        public boolean enableDailySummaryScreen = true;
        public boolean useFactsApi = false;
        public boolean userContentReplaces = false;
        public boolean hideChatWhenSleeping = true;
        public boolean hideHudMessagesWhenSleeping = true;
        public float textScale = 1.0f;
        public float imageScale = 1.0f;
    }

    public static class WellRestedSettings {
        public boolean enabled = true;
        public Map<String, WellRestedLevel> levels = new HashMap<>();

        public WellRestedSettings() {
            levels.put("level1", defaultLevel(3,  0.01f, 0.08f, 0.04f, 0.02f, 0.25f, 0.25f, 0.25f, 0.07f, 0.04f, 0.02f, 0.02f, 0.01f, 0.01f,  2.0f));
            levels.put("level2", defaultLevel(5,  0.02f, 0.12f, 0.06f, 0.03f, 0.50f, 0.25f, 0.25f, 0.11f, 0.06f, 0.03f, 0.04f, 0.02f, 0.01f,  4.0f));
            levels.put("level3", defaultLevel(7,  0.03f, 0.16f, 0.09f, 0.04f, 0.75f, 0.50f, 0.25f, 0.15f, 0.08f, 0.04f, 0.05f, 0.03f, 0.01f,  6.0f));
            levels.put("level4", defaultLevel(10, 0.04f, 0.20f, 0.12f, 0.06f, 1.00f, 0.75f, 0.50f, 0.20f, 0.11f, 0.06f, 0.06f, 0.03f, 0.02f,  8.0f));
            levels.put("level5", defaultLevel(15, 0.05f, 0.25f, 0.15f, 0.08f, 1.50f, 1.00f, 0.50f, 0.25f, 0.14f, 0.07f, 0.07f, 0.04f, 0.02f, 10.0f));
        }

        private static WellRestedLevel defaultLevel(int durationMinutes, float regenBonus,
                                                    float speed1, float speed2, float speed3,
                                                    float strength1, float strength2, float strength3,
                                                    float haste1, float haste2, float haste3,
                                                    float attackSpeed1, float attackSpeed2, float attackSpeed3,
                                                    float health) {
            WellRestedLevel level = new WellRestedLevel(durationMinutes, regenBonus);
            level.attributes.add(attributeBonus(ATTRIBUTE_MOVEMENT_SPEED, OPERATION_MULTIPLIED_BASE, speed1, speed2, speed3));
            level.attributes.add(attributeBonus(ATTRIBUTE_ATTACK_DAMAGE, OPERATION_ADD_VALUE, strength1, strength2, strength3));
            level.attributes.add(attributeBonus(ATTRIBUTE_BLOCK_BREAK_SPEED, OPERATION_MULTIPLIED_BASE, haste1, haste2, haste3));
            level.attributes.add(attributeBonus(ATTRIBUTE_ATTACK_SPEED, OPERATION_MULTIPLIED_BASE, attackSpeed1, attackSpeed2, attackSpeed3));
            level.attributes.add(attributeBonus(ATTRIBUTE_MAX_HEALTH, OPERATION_ADD_VALUE, health, health, health));
            return level;
        }

        public WellRestedLevel getLevel(int level) {
            return levels.getOrDefault("level" + level, levels.get("level1"));
        }
    }

    public static AttributeBonus attributeBonus(String id, String operation, float phase1, float phase2, float phase3) {
        AttributeBonus bonus = new AttributeBonus();
        bonus.id = id;
        bonus.operation = operation;
        bonus.phase1 = phase1;
        bonus.phase2 = phase2;
        bonus.phase3 = phase3;
        return bonus;
    }

    public static class WellRestedLevel {
        public int durationMinutes;
        public float regenBonus;
        public List<AttributeBonus> attributes;
        public List<EffectBonus> effects;

        public Float speedPhase1, speedPhase2, speedPhase3;
        public Float strengthPhase1, strengthPhase2, strengthPhase3;
        public Float hastePhase1, hastePhase2, hastePhase3;
        public Float attackSpeedPhase1, attackSpeedPhase2, attackSpeedPhase3;
        public Float healthBonus;

        public WellRestedLevel(int durationMinutes, float regenBonus) {
            this.durationMinutes = durationMinutes;
            this.regenBonus = regenBonus;
            this.attributes = new ArrayList<>();
            this.effects = new ArrayList<>();
        }
    }

    public static class AttributeBonus {
        public String id = "";
        public String operation = "add_value";
        public float phase1 = 0.0f;
        public float phase2 = 0.0f;
        public float phase3 = 0.0f;
    }

    public static class EffectBonus {
        public String id = "";
        public int phase1 = -1;
        public int phase2 = -1;
        public int phase3 = -1;
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
        public ComfortWeights weights = new ComfortWeights();
        public int nightmareThreshold = -1;
        public int sleepBlockThreshold = -2;
        public boolean nightmareEnabled = true;
        public boolean sleepBlockEnabled = true;
    }

    public static class ComfortWeights {
        public int lighting = 1;
        public int carpet = 1;
        public int furniture = 1;
        public int decoration = 1;
        public int structure = 1;
        public int macabre = -1;
        public int hostile = -1;
        public int dark = -1;
    }

    public static class ServerSettings {
        public boolean resetPhantomTimerForNonSleepers = true;
        public boolean suppressVanillaSleepMessages = true;
    }

    public static class UISettings {
        public String theme = "classic";
        public String wellRestedHudPosition = HUD_POSITION_LEFT;
        public boolean hideWellRestedHud = false;
        public boolean hideSleepingPlayersHud = false;
        public boolean hideThemeSwitchButton = false;
    }

    public static void reload() {
        MidnightThoughtsConfig fresh = load();
        if (instance == null) {
            instance = fresh;
        } else {
            instance.copyFrom(fresh);
        }
    }

    private void copyFrom(MidnightThoughtsConfig other) {
        this.sleepOverlay = other.sleepOverlay;
        this.wellRested = other.wellRested;
        this.mvp = other.mvp;
        this.comfort = other.comfort;
        this.server = other.server;
        this.ui = other.ui;
    }
}
