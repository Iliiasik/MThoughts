package mt.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MidnightThoughtsConfigValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final Set<String> VALID_THEMES = Set.of("classic", "magic", "tech", "vanilla");
    private static final Set<String> VALID_OPERATIONS = Set.of(
            "add_value", "addition",
            "add_multiplied_base", "multiply_base",
            "add_multiplied_total", "multiply_total");
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
        o.textScale = clampFloat(o.textScale, 0.5f, 1.5f, 1.0f, "sleepOverlay.textScale");
        o.imageScale = clampFloat(o.imageScale, 0.5f, 1.5f, 1.0f, "sleepOverlay.imageScale");
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
        migrateLegacyBonuses(l, p);
        l.durationMinutes = clampInt(l.durationMinutes, 1, 1440, p + "durationMinutes");
        l.regenBonus = clampFloat(l.regenBonus, 0f, 1f, 0f, p + "regenBonus");
        l.attributes = validateAttributeBonuses(l.attributes, p);
        l.effects = validateEffectBonuses(l.effects, p);
    }

    private static void migrateLegacyBonuses(MidnightThoughtsConfig.WellRestedLevel l, String p) {
        boolean hasLegacy = l.speedPhase1 != null || l.speedPhase2 != null || l.speedPhase3 != null
                || l.strengthPhase1 != null || l.strengthPhase2 != null || l.strengthPhase3 != null
                || l.hastePhase1 != null || l.hastePhase2 != null || l.hastePhase3 != null
                || l.attackSpeedPhase1 != null || l.attackSpeedPhase2 != null || l.attackSpeedPhase3 != null
                || l.healthBonus != null;

        if (hasLegacy && l.attributes == null) {
            List<MidnightThoughtsConfig.AttributeBonus> migrated = new ArrayList<>();
            migrated.add(MidnightThoughtsConfig.attributeBonus(
                    MidnightThoughtsConfig.ATTRIBUTE_MOVEMENT_SPEED, MidnightThoughtsConfig.OPERATION_MULTIPLIED_BASE,
                    legacy(l.speedPhase1, -5f, 5f), legacy(l.speedPhase2, -5f, 5f), legacy(l.speedPhase3, -5f, 5f)));
            migrated.add(MidnightThoughtsConfig.attributeBonus(
                    MidnightThoughtsConfig.ATTRIBUTE_ATTACK_DAMAGE, MidnightThoughtsConfig.OPERATION_ADD_VALUE,
                    legacy(l.strengthPhase1, -5f, 5f), legacy(l.strengthPhase2, -5f, 5f), legacy(l.strengthPhase3, -5f, 5f)));
            migrated.add(MidnightThoughtsConfig.attributeBonus(
                    MidnightThoughtsConfig.ATTRIBUTE_BLOCK_BREAK_SPEED, MidnightThoughtsConfig.OPERATION_MULTIPLIED_BASE,
                    legacy(l.hastePhase1, -5f, 5f), legacy(l.hastePhase2, -5f, 5f), legacy(l.hastePhase3, -5f, 5f)));
            migrated.add(MidnightThoughtsConfig.attributeBonus(
                    MidnightThoughtsConfig.ATTRIBUTE_ATTACK_SPEED, MidnightThoughtsConfig.OPERATION_MULTIPLIED_BASE,
                    legacy(l.attackSpeedPhase1, -5f, 5f), legacy(l.attackSpeedPhase2, -5f, 5f), legacy(l.attackSpeedPhase3, -5f, 5f)));
            float health = legacy(l.healthBonus, 0f, 200f);
            migrated.add(MidnightThoughtsConfig.attributeBonus(
                    MidnightThoughtsConfig.ATTRIBUTE_MAX_HEALTH, MidnightThoughtsConfig.OPERATION_ADD_VALUE,
                    health, health, health));
            l.attributes = migrated;
            LOGGER.info("Config section {} was migrated to the attribute list", p);
        }

        l.speedPhase1 = null; l.speedPhase2 = null; l.speedPhase3 = null;
        l.strengthPhase1 = null; l.strengthPhase2 = null; l.strengthPhase3 = null;
        l.hastePhase1 = null; l.hastePhase2 = null; l.hastePhase3 = null;
        l.attackSpeedPhase1 = null; l.attackSpeedPhase2 = null; l.attackSpeedPhase3 = null;
        l.healthBonus = null;
    }

    private static float legacy(Float value, float min, float max) {
        if (value == null || !Float.isFinite(value)) return 0f;
        return Math.clamp(value, min, max);
    }

    private static List<MidnightThoughtsConfig.AttributeBonus> validateAttributeBonuses(
            List<MidnightThoughtsConfig.AttributeBonus> bonuses, String p) {
        List<MidnightThoughtsConfig.AttributeBonus> result = new ArrayList<>();
        if (bonuses == null) return result;
        for (MidnightThoughtsConfig.AttributeBonus bonus : bonuses) {
            if (bonus == null || bonus.id == null || bonus.id.isBlank()) {
                LOGGER.warn("Config entry {}attributes had no id and was dropped", p);
                continue;
            }
            String operation = bonus.operation == null ? "" : bonus.operation.trim().toLowerCase(Locale.ROOT);
            if (!VALID_OPERATIONS.contains(operation)) {
                LOGGER.warn("Config value {}attributes[{}].operation was '{}', reset to 'add_value'",
                        p, bonus.id, bonus.operation);
                operation = "add_value";
            }
            bonus.operation = operation;
            String field = p + "attributes[" + bonus.id + "].";
            bonus.phase1 = clampFloat(bonus.phase1, -10000f, 10000f, 0f, field + "phase1");
            bonus.phase2 = clampFloat(bonus.phase2, -10000f, 10000f, 0f, field + "phase2");
            bonus.phase3 = clampFloat(bonus.phase3, -10000f, 10000f, 0f, field + "phase3");
            result.add(bonus);
        }
        return result;
    }

    private static List<MidnightThoughtsConfig.EffectBonus> validateEffectBonuses(
            List<MidnightThoughtsConfig.EffectBonus> bonuses, String p) {
        List<MidnightThoughtsConfig.EffectBonus> result = new ArrayList<>();
        if (bonuses == null) return result;
        for (MidnightThoughtsConfig.EffectBonus bonus : bonuses) {
            if (bonus == null || bonus.id == null || bonus.id.isBlank()) {
                LOGGER.warn("Config entry {}effects had no id and was dropped", p);
                continue;
            }
            String field = p + "effects[" + bonus.id + "].";
            bonus.phase1 = clampInt(bonus.phase1, -1, 255, field + "phase1");
            bonus.phase2 = clampInt(bonus.phase2, -1, 255, field + "phase2");
            bonus.phase3 = clampInt(bonus.phase3, -1, 255, field + "phase3");
            result.add(bonus);
        }
        return result;
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
        if (!isValidTheme(ui.theme)) {
            LOGGER.warn("Config value ui.theme was '{}', reset to 'classic'", ui.theme);
            ui.theme = "classic";
        }
        if (!isValidHudPosition(ui.wellRestedHudPosition)) {
            LOGGER.warn("Config value ui.wellRestedHudPosition was '{}', reset to '{}'",
                    ui.wellRestedHudPosition, MidnightThoughtsConfig.HUD_POSITION_LEFT);
            ui.wellRestedHudPosition = MidnightThoughtsConfig.HUD_POSITION_LEFT;
        }
    }
}
