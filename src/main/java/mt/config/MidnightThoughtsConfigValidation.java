package mt.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public final class MidnightThoughtsConfigValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final Set<String> VALID_THEMES = Set.of("classic", "magic", "tech", "vanilla");

    private static final Set<String> VALID_HUD_POSITIONS = Set.of(
            MidnightThoughtsConfig.HUD_POSITION_LEFT,
            MidnightThoughtsConfig.HUD_POSITION_BAR,
            MidnightThoughtsConfig.HUD_POSITION_RIGHT);

    private static final String CLAMPED_TO_MINIMUM = "Config value {} was {}, clamped to minimum {}";
    private static final String CLAMPED_TO_MAXIMUM = "Config value {} was {}, clamped to maximum {}";
    private static final String NOT_A_NUMBER = "Config value {} was not a valid number, reset to {}";

    private MidnightThoughtsConfigValidation() {}

    public static boolean isValidTheme(String theme) {
        return theme != null && VALID_THEMES.contains(theme);
    }

    public static boolean isValidHudPosition(String position) {
        return position != null && VALID_HUD_POSITIONS.contains(position);
    }

    public static void apply(MidnightThoughtsConfig config) {
        validateSleepOverlay(config.getSleepOverlay());
        validateWellRested(config.getWellRested());
        validateMvp(config.getMvp());
        validateComfort(config.getComfort());
        validateUi(config.getUi());
    }

    private static <T extends Comparable<T>> T clamp(T value, T min, T max, String field) {
        if (value.compareTo(min) < 0) {
            LOGGER.warn(CLAMPED_TO_MINIMUM, field, value, min);
            return min;
        }
        if (value.compareTo(max) > 0) {
            LOGGER.warn(CLAMPED_TO_MAXIMUM, field, value, max);
            return max;
        }
        return value;
    }

    private static int clampInt(int value, int min, int max, String field) {
        return clamp(value, min, max, field);
    }

    private static float clampFloat(float value, float min, float max, float fallback, String field) {
        if (!Float.isFinite(value)) {
            LOGGER.warn(NOT_A_NUMBER, field, fallback);
            return fallback;
        }
        return clamp(value, min, max, field);
    }

    private static void validateSleepOverlay(MidnightThoughtsConfig.SleepOverlaySettings o) {
        o.minSlideDisplayTimeMs = clampInt(o.minSlideDisplayTimeMs, 500, 120000, "sleepOverlay.minSlideDisplayTimeMs");
        o.maxSlideDisplayTimeMs = clampInt(o.maxSlideDisplayTimeMs, 500, 120000, "sleepOverlay.maxSlideDisplayTimeMs");
        if (o.maxSlideDisplayTimeMs < o.minSlideDisplayTimeMs) {
            LOGGER.warn("Config value sleepOverlay.maxSlideDisplayTimeMs was below the minimum, raised to {}", o.minSlideDisplayTimeMs);
            o.maxSlideDisplayTimeMs = o.minSlideDisplayTimeMs;
        }
        o.fadeInDurationMs = clampInt(o.fadeInDurationMs, 0, 10000, "sleepOverlay.fadeInDurationMs");
        o.fadeOutDurationMs = clampInt(o.fadeOutDurationMs, 0, 10000, "sleepOverlay.fadeOutDurationMs");
        o.overlayOpacity = clampFloat(o.overlayOpacity, 0f, 1f, 0.4f, "sleepOverlay.overlayOpacity");
        o.textOpacity = clampFloat(o.textOpacity, 0f, 1f, 1.0f, "sleepOverlay.textOpacity");
        o.imageOpacity = clampFloat(o.imageOpacity, 0f, 1f, 0.6f, "sleepOverlay.imageOpacity");
        o.specialSlideChance = clampFloat(o.specialSlideChance, 0f, 1f, 0.05f, "sleepOverlay.specialSlideChance");
    }

    private static void validateWellRested(MidnightThoughtsConfig.WellRestedSettings wellRested) {
        if (wellRested.levels == null) {
            LOGGER.warn("Config section wellRested.levels was missing, restored to defaults");
            wellRested.levels = new MidnightThoughtsConfig.WellRestedSettings().levels;
        }
        Map<String, MidnightThoughtsConfig.WellRestedLevel> defaults =
                new MidnightThoughtsConfig.WellRestedSettings().levels;
        for (int i = 1; i <= 5; i++) {
            String key = "level" + i;
            MidnightThoughtsConfig.WellRestedLevel level = wellRested.levels.get(key);
            if (level == null) {
                LOGGER.warn("Config entry wellRested.levels.{} was missing, restored to default", key);
                wellRested.levels.put(key, defaults.get(key));
                continue;
            }
            validateLevel(level, key);
        }
    }

    private static void validateLevel(MidnightThoughtsConfig.WellRestedLevel l, String key) {
        String p = "wellRested.levels." + key + ".";
        l.durationMinutes = clampInt(l.durationMinutes, 1, 1440, p + "durationMinutes");
        l.speedPhase1 = clampFloat(l.speedPhase1, -5f, 5f, 0f, p + "speedPhase1");
        l.speedPhase2 = clampFloat(l.speedPhase2, -5f, 5f, 0f, p + "speedPhase2");
        l.speedPhase3 = clampFloat(l.speedPhase3, -5f, 5f, 0f, p + "speedPhase3");
        l.strengthPhase1 = clampFloat(l.strengthPhase1, -5f, 5f, 0f, p + "strengthPhase1");
        l.strengthPhase2 = clampFloat(l.strengthPhase2, -5f, 5f, 0f, p + "strengthPhase2");
        l.strengthPhase3 = clampFloat(l.strengthPhase3, -5f, 5f, 0f, p + "strengthPhase3");
        l.attackSpeedPhase1 = clampFloat(l.attackSpeedPhase1, -5f, 5f, 0f, p + "attackSpeedPhase1");
        l.attackSpeedPhase2 = clampFloat(l.attackSpeedPhase2, -5f, 5f, 0f, p + "attackSpeedPhase2");
        l.attackSpeedPhase3 = clampFloat(l.attackSpeedPhase3, -5f, 5f, 0f, p + "attackSpeedPhase3");
        l.healthBonus = clampFloat(l.healthBonus, 0f, 200f, 0f, p + "healthBonus");
        l.regenBonus = clampFloat(l.regenBonus, 0f, 1f, 0f, p + "regenBonus");
    }

    private static void validateMvp(MidnightThoughtsConfig.MvpSettings mvp) {
        mvp.minScoreRequired = clampInt(mvp.minScoreRequired, 0, 1000000, "mvp.minScoreRequired");
        mvp.pointsPerDistance100 = clampInt(mvp.pointsPerDistance100, 0, 100000, "mvp.pointsPerDistance100");
        mvp.pointsPerBlock = clampInt(mvp.pointsPerBlock, 0, 100000, "mvp.pointsPerBlock");
        mvp.pointsPerMob = clampInt(mvp.pointsPerMob, 0, 100000, "mvp.pointsPerMob");
        mvp.pointsPerJump10 = clampInt(mvp.pointsPerJump10, 0, 100000, "mvp.pointsPerJump10");
        mvp.penaltyPerDeath = clampInt(mvp.penaltyPerDeath, 0, 100000, "mvp.penaltyPerDeath");
        mvp.mvpWellRestedDurationMinutes = clampInt(mvp.mvpWellRestedDurationMinutes, 1, 1440, "mvp.mvpWellRestedDurationMinutes");
    }

    private static void validateComfort(MidnightThoughtsConfig.ComfortSettings comfort) {
        comfort.scanRadius = clampInt(comfort.scanRadius, 0, 8, "comfort.scanRadius");
        comfort.nightmareThreshold = clampInt(comfort.nightmareThreshold, -100, 100, "comfort.nightmareThreshold");
        comfort.sleepBlockThreshold = clampInt(comfort.sleepBlockThreshold, -100, 100, "comfort.sleepBlockThreshold");
        MidnightThoughtsConfig.ComfortWeights w = comfort.weights;
        w.lighting = clampInt(w.lighting, -100, 100, "comfort.weights.lighting");
        w.carpet = clampInt(w.carpet, -100, 100, "comfort.weights.carpet");
        w.furniture = clampInt(w.furniture, -100, 100, "comfort.weights.furniture");
        w.decoration = clampInt(w.decoration, -100, 100, "comfort.weights.decoration");
        w.structure = clampInt(w.structure, -100, 100, "comfort.weights.structure");
        w.macabre = clampInt(w.macabre, -100, 100, "comfort.weights.macabre");
        w.hostile = clampInt(w.hostile, -100, 100, "comfort.weights.hostile");
        w.dark = clampInt(w.dark, -100, 100, "comfort.weights.dark");
    }

    private static void validateUi(MidnightThoughtsConfig.UISettings ui) {
        if (!isValidHudPosition(ui.wellRestedHudPosition)) {
            LOGGER.warn("Config value ui.wellRestedHudPosition was '{}', reset to '{}'",
                    ui.wellRestedHudPosition, MidnightThoughtsConfig.HUD_POSITION_LEFT);
            ui.wellRestedHudPosition = MidnightThoughtsConfig.HUD_POSITION_LEFT;
        }

        if (!isValidTheme(ui.theme)) {
            LOGGER.warn("Config value ui.theme was '{}', reset to 'classic'", ui.theme);
            ui.theme = "classic";
        }
    }
}
