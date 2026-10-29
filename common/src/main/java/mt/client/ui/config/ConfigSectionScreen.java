package mt.client.ui.config;

import mt.config.MidnightThoughtsConfig;
import mt.server.MTServer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ConfigSectionScreen extends OptionsSubScreen {

    private static final String OPTION_PREFIX = "option.midnightthoughts.";
    private static final String TOOLTIP_SUFFIX = ".tooltip";
    private static final String GENERIC_VALUE_KEY = "options.generic_value";

    private static final String PERCENT_KEY = OPTION_PREFIX + "percent";
    private static final String SECONDS_KEY = OPTION_PREFIX + "seconds";
    private static final String MILLIS_KEY = OPTION_PREFIX + "milliseconds";
    private static final String MINUTES_KEY = OPTION_PREFIX + "minutes";
    private static final String BLOCKS_KEY = OPTION_PREFIX + "blocks";

    private static final String THEME_PREFIX = "midnightthoughts.theme.";
    private static final String HUD_POSITION_PREFIX = OPTION_PREFIX + "hud_position.";

    private final ConfigSection section;

    public ConfigSectionScreen(Screen parent, ConfigSection section) {
        super(parent, Minecraft.getInstance().options, section.title().withStyle(ChatFormatting.BOLD));
        this.section = section;
    }

    @Override
    protected void addOptions() {
        if (list == null) return;
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        switch (section) {
            case SLEEP_OVERLAY -> addSleepOverlay(list, config.getSleepOverlay());
            case INTERFACE -> addInterface(list, config.getUi());
            case MVP -> addMvp(list, config.getMvp());
            case COMFORT -> addComfort(list, config.getComfort());
            case SERVER -> addServer(list, config.getServer());
        }
    }

    private static void addSleepOverlay(OptionsList options, MidnightThoughtsConfig.SleepOverlaySettings o) {
        options.addBig(flag("enable_overlay", o.enableOverlay, value -> o.enableOverlay = value));
        options.addSmall(
                flag("enable_image", o.enableImage, value -> o.enableImage = value),
                flag("enable_daily_summary", o.enableDailySummaryScreen, value -> o.enableDailySummaryScreen = value));
        options.addSmall(
                flag("enable_star_dust", o.enableStarDust, value -> o.enableStarDust = value),
                flag("enable_space_objects", o.enableSpaceObjects, value -> o.enableSpaceObjects = value));
        options.addSmall(
                flag("show_slide_progress", o.showSlideProgress, value -> o.showSlideProgress = value),
                flag("use_facts_api", o.useFactsApi, value -> o.useFactsApi = value));
        options.addSmall(
                flag("user_content_replaces", o.userContentReplaces, value -> o.userContentReplaces = value),
                flag("hide_chat_when_sleeping", o.hideChatWhenSleeping, value -> o.hideChatWhenSleeping = value));
        options.addSmall(
                flag("hide_hud_messages", o.hideHudMessagesWhenSleeping, value -> o.hideHudMessagesWhenSleeping = value),
                number("min_slide_time", SECONDS_KEY, 1, 60, o.minSlideDisplayTimeMs / 1000,
                        value -> o.minSlideDisplayTimeMs = value * 1000));
        options.addSmall(
                number("max_slide_time", SECONDS_KEY, 1, 60, o.maxSlideDisplayTimeMs / 1000,
                        value -> o.maxSlideDisplayTimeMs = value * 1000),
                number("fade_in", MILLIS_KEY, 0, 3000, o.fadeInDurationMs, value -> o.fadeInDurationMs = value));
        options.addSmall(
                number("fade_out", MILLIS_KEY, 0, 3000, o.fadeOutDurationMs, value -> o.fadeOutDurationMs = value),
                percent("overlay_opacity", 0, 100, o.overlayOpacity, value -> o.overlayOpacity = value));
        options.addSmall(
                percent("text_opacity", 0, 100, o.textOpacity, value -> o.textOpacity = value),
                percent("image_opacity", 0, 100, o.imageOpacity, value -> o.imageOpacity = value));
        options.addSmall(
                percent("text_scale", 50, 150, o.textScale, value -> o.textScale = value),
                percent("image_scale", 50, 150, o.imageScale, value -> o.imageScale = value));
        options.addBig(percent("special_slide_chance", 0, 100, o.specialSlideChance,
                value -> o.specialSlideChance = value));
    }

    private static void addInterface(OptionsList options, MidnightThoughtsConfig.UISettings ui) {
        options.addBig(choice("theme", MidnightThoughtsConfig.THEMES, THEME_PREFIX, ui.theme, value -> ui.theme = value));
        options.addSmall(
                choice("hud_position", MidnightThoughtsConfig.HUD_POSITIONS, HUD_POSITION_PREFIX, ui.wellRestedHudPosition,
                        value -> ui.wellRestedHudPosition = value),
                flag("hide_well_rested_hud", ui.hideWellRestedHud, value -> ui.hideWellRestedHud = value));
        options.addBig(
                flag("hide_sleeping_players_hud", ui.hideSleepingPlayersHud, value -> ui.hideSleepingPlayersHud = value));
    }

    private static void addMvp(OptionsList options, MidnightThoughtsConfig.MvpSettings mvp) {
        options.addBig(flag("mvp_enabled", mvp.enabled, value -> mvp.enabled = value));
        options.addSmall(
                number("mvp_min_score", null, 0, 500, mvp.minScoreRequired, value -> mvp.minScoreRequired = value),
                number("mvp_points_distance", null, 0, 50, mvp.pointsPerDistance100,
                        value -> mvp.pointsPerDistance100 = value));
        options.addSmall(
                number("mvp_points_block", null, 0, 50, mvp.pointsPerBlock, value -> mvp.pointsPerBlock = value),
                number("mvp_points_mob", null, 0, 100, mvp.pointsPerMob, value -> mvp.pointsPerMob = value));
        options.addSmall(
                number("mvp_points_jump", null, 0, 50, mvp.pointsPerJump10, value -> mvp.pointsPerJump10 = value),
                number("mvp_death_penalty", null, 0, 500, mvp.penaltyPerDeath, value -> mvp.penaltyPerDeath = value));
        options.addBig(number("mvp_well_rested_duration", MINUTES_KEY, 1, 240, mvp.mvpWellRestedDurationMinutes,
                value -> mvp.mvpWellRestedDurationMinutes = value));
    }

    private static void addComfort(OptionsList options, MidnightThoughtsConfig.ComfortSettings comfort) {
        MidnightThoughtsConfig.ComfortWeights w = comfort.weights;
        options.addBig(flag("comfort_enabled", comfort.enabled, value -> comfort.enabled = value));
        options.addBig(number("comfort_scan_radius", BLOCKS_KEY, 0, 8, comfort.scanRadius,
                value -> comfort.scanRadius = value));
        options.addSmall(
                flag("nightmare_enabled", comfort.nightmareEnabled, value -> comfort.nightmareEnabled = value),
                number("nightmare_threshold", null, -20, 20, comfort.nightmareThreshold,
                        value -> comfort.nightmareThreshold = value));
        options.addSmall(
                flag("sleep_block_enabled", comfort.sleepBlockEnabled, value -> comfort.sleepBlockEnabled = value),
                number("sleep_block_threshold", null, -20, 20, comfort.sleepBlockThreshold,
                        value -> comfort.sleepBlockThreshold = value));
        options.addSmall(
                number("weight_lighting", null, -10, 10, w.lighting, value -> w.lighting = value),
                number("weight_carpet", null, -10, 10, w.carpet, value -> w.carpet = value));
        options.addSmall(
                number("weight_furniture", null, -10, 10, w.furniture, value -> w.furniture = value),
                number("weight_decoration", null, -10, 10, w.decoration, value -> w.decoration = value));
        options.addSmall(
                number("weight_structure", null, -10, 10, w.structure, value -> w.structure = value),
                number("weight_macabre", null, -10, 10, w.macabre, value -> w.macabre = value));
        options.addSmall(
                number("weight_hostile", null, -10, 10, w.hostile, value -> w.hostile = value),
                number("weight_dark", null, -10, 10, w.dark, value -> w.dark = value));
    }

    private static void addServer(OptionsList options, MidnightThoughtsConfig.ServerSettings server) {
        options.addBig(flag("reset_phantom_timer", server.resetPhantomTimerForNonSleepers,
                value -> server.resetPhantomTimerForNonSleepers = value));
        options.addBig(flag("suppress_vanilla_messages", server.suppressVanillaSleepMessages,
                value -> server.suppressVanillaSleepMessages = value));
    }

    private static <T> OptionInstance.TooltipSupplier<T> tooltip(String key) {
        return OptionInstance.cachedConstantTooltip(Component.translatable(key + TOOLTIP_SUFFIX));
    }

    private static Component valueLabel(Component caption, Component value) {
        return Component.translatable(GENERIC_VALUE_KEY, caption, value);
    }

    private static OptionInstance<Boolean> flag(String name, boolean current, Consumer<Boolean> setter) {
        String key = OPTION_PREFIX + name;
        return OptionInstance.createBoolean(key, tooltip(key), current, setter);
    }

    private static OptionInstance<Integer> number(String name, String unitKey, int min, int max,
                                                  int current, IntConsumer setter) {
        String key = OPTION_PREFIX + name;
        return new OptionInstance<>(
                key,
                tooltip(key),
                (caption, value) -> valueLabel(caption, unitKey == null
                        ? Component.literal(String.valueOf(value))
                        : Component.translatable(unitKey, value)),
                new OptionInstance.IntRange(min, max),
                Math.clamp(current, min, max),
                setter::accept);
    }

    private static OptionInstance<Integer> percent(String name, int min, int max,
                                                   float current, FloatSetter setter) {
        String key = OPTION_PREFIX + name;
        return new OptionInstance<>(
                key,
                tooltip(key),
                (caption, value) -> valueLabel(caption, Component.translatable(PERCENT_KEY, value)),
                new OptionInstance.IntRange(min, max),
                Math.clamp(Math.round(current * 100f), min, max),
                value -> setter.set(value / 100f));
    }

    private static OptionInstance<String> choice(String name, List<String> values, String valuePrefix,
                                                 String current, Consumer<String> setter) {
        String key = OPTION_PREFIX + name;
        return new OptionInstance<>(
                key,
                tooltip(key),
                (caption, value) -> Component.translatable(valuePrefix + value),
                new OptionInstance.Enum<>(values, Codec.STRING),
                values.contains(current) ? current : values.getFirst(),
                setter);
    }

    @Override
    public void removed() {
        super.removed();
        MidnightThoughtsConfig.getInstance().save();
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> MTServer.syncConfig(server));
        }
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }
}
