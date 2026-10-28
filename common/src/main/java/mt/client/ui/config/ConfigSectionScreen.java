package mt.client.ui.config;

import mt.config.MidnightThoughtsConfig;
import mt.network.packet.SyncConfigPacket;
import mt.platform.MTNetwork;
import mt.server.ComfortCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
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

    private static final List<String> THEMES = List.of("vanilla", "magic", "classic", "tech");
    private static final List<String> HUD_POSITIONS = List.of(
            MidnightThoughtsConfig.HUD_POSITION_LEFT,
            MidnightThoughtsConfig.HUD_POSITION_BAR,
            MidnightThoughtsConfig.HUD_POSITION_RIGHT
    );

    private final ConfigSection section;

    public ConfigSectionScreen(Screen parent, ConfigSection section) {
        super(parent, Minecraft.getInstance().options, section.title().withStyle(ChatFormatting.BOLD));
        this.section = section;
    }

    @Override
    protected void addOptions() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        switch (section) {
            case SLEEP_OVERLAY -> addSleepOverlay(config.getSleepOverlay());
            case INTERFACE -> addInterface(config.getUi());
            case MVP -> addMvp(config.getMvp());
            case COMFORT -> addComfort(config.getComfort());
            case SERVER -> addServer(config.getServer());
        }
    }

    private void addSleepOverlay(MidnightThoughtsConfig.SleepOverlaySettings o) {
        if (list == null) return;
        list.addBig(flag("enable_overlay", o.enableOverlay, value -> o.enableOverlay = value));
        list.addSmall(
                flag("enable_image", o.enableImage, value -> o.enableImage = value),
                flag("enable_daily_summary", o.enableDailySummaryScreen, value -> o.enableDailySummaryScreen = value));
        list.addSmall(
                flag("enable_star_dust", o.enableStarDust, value -> o.enableStarDust = value),
                flag("enable_space_objects", o.enableSpaceObjects, value -> o.enableSpaceObjects = value));
        list.addSmall(
                flag("show_slide_progress", o.showSlideProgress, value -> o.showSlideProgress = value),
                flag("use_facts_api", o.useFactsApi, value -> o.useFactsApi = value));
        list.addSmall(
                flag("user_content_replaces", o.userContentReplaces, value -> o.userContentReplaces = value),
                flag("hide_chat_when_sleeping", o.hideChatWhenSleeping, value -> o.hideChatWhenSleeping = value));
        list.addSmall(
                flag("hide_hud_messages", o.hideHudMessagesWhenSleeping, value -> o.hideHudMessagesWhenSleeping = value),
                number("min_slide_time", SECONDS_KEY, 1, 60, o.minSlideDisplayTimeMs / 1000,
                        value -> o.minSlideDisplayTimeMs = value * 1000));
        list.addSmall(
                number("max_slide_time", SECONDS_KEY, 1, 60, o.maxSlideDisplayTimeMs / 1000,
                        value -> o.maxSlideDisplayTimeMs = value * 1000),
                number("fade_in", MILLIS_KEY, 0, 3000, o.fadeInDurationMs, value -> o.fadeInDurationMs = value));
        list.addSmall(
                number("fade_out", MILLIS_KEY, 0, 3000, o.fadeOutDurationMs, value -> o.fadeOutDurationMs = value),
                percent("overlay_opacity", 0, 100, o.overlayOpacity, value -> o.overlayOpacity = value));
        list.addSmall(
                percent("text_opacity", 0, 100, o.textOpacity, value -> o.textOpacity = value),
                percent("image_opacity", 0, 100, o.imageOpacity, value -> o.imageOpacity = value));
        list.addSmall(
                percent("text_scale", 50, 150, o.textScale, value -> o.textScale = value),
                percent("image_scale", 50, 150, o.imageScale, value -> o.imageScale = value));
        list.addBig(percent("special_slide_chance", 0, 100, o.specialSlideChance,
                value -> o.specialSlideChance = value));
    }

    private void addInterface(MidnightThoughtsConfig.UISettings ui) {
        if (list == null) return;
        list.addBig(choice("theme", THEMES, THEME_PREFIX, ui.theme, value -> ui.theme = value));
        list.addSmall(
                choice("hud_position", HUD_POSITIONS, HUD_POSITION_PREFIX, ui.wellRestedHudPosition,
                        value -> ui.wellRestedHudPosition = value),
                flag("hide_well_rested_hud", ui.hideWellRestedHud, value -> ui.hideWellRestedHud = value));
        list.addSmall(
                flag("hide_sleeping_players_hud", ui.hideSleepingPlayersHud, value -> ui.hideSleepingPlayersHud = value));
    }

    private void addMvp(MidnightThoughtsConfig.MvpSettings mvp) {
        if (list == null) return;
        list.addBig(flag("mvp_enabled", mvp.enabled, value -> mvp.enabled = value));
        list.addSmall(
                number("mvp_min_score", null, 0, 500, mvp.minScoreRequired, value -> mvp.minScoreRequired = value),
                number("mvp_points_distance", null, 0, 50, mvp.pointsPerDistance100,
                        value -> mvp.pointsPerDistance100 = value));
        list.addSmall(
                number("mvp_points_block", null, 0, 50, mvp.pointsPerBlock, value -> mvp.pointsPerBlock = value),
                number("mvp_points_mob", null, 0, 100, mvp.pointsPerMob, value -> mvp.pointsPerMob = value));
        list.addSmall(
                number("mvp_points_jump", null, 0, 50, mvp.pointsPerJump10, value -> mvp.pointsPerJump10 = value),
                number("mvp_death_penalty", null, 0, 500, mvp.penaltyPerDeath, value -> mvp.penaltyPerDeath = value));
        list.addBig(number("mvp_well_rested_duration", MINUTES_KEY, 1, 240, mvp.mvpWellRestedDurationMinutes,
                value -> mvp.mvpWellRestedDurationMinutes = value));
    }

    private void addComfort(MidnightThoughtsConfig.ComfortSettings comfort) {
        if (list == null) return;
        MidnightThoughtsConfig.ComfortWeights w = comfort.weights;
        list.addBig(flag("comfort_enabled", comfort.enabled, value -> comfort.enabled = value));
        list.addBig(number("comfort_scan_radius", BLOCKS_KEY, 0, 8, comfort.scanRadius,
                value -> comfort.scanRadius = value));
        list.addSmall(
                flag("nightmare_enabled", comfort.nightmareEnabled, value -> comfort.nightmareEnabled = value),
                number("nightmare_threshold", null, -20, 20, comfort.nightmareThreshold,
                        value -> comfort.nightmareThreshold = value));
        list.addSmall(
                flag("sleep_block_enabled", comfort.sleepBlockEnabled, value -> comfort.sleepBlockEnabled = value),
                number("sleep_block_threshold", null, -20, 20, comfort.sleepBlockThreshold,
                        value -> comfort.sleepBlockThreshold = value));
        list.addSmall(
                number("weight_lighting", null, -10, 10, w.lighting, value -> w.lighting = value),
                number("weight_carpet", null, -10, 10, w.carpet, value -> w.carpet = value));
        list.addSmall(
                number("weight_furniture", null, -10, 10, w.furniture, value -> w.furniture = value),
                number("weight_decoration", null, -10, 10, w.decoration, value -> w.decoration = value));
        list.addSmall(
                number("weight_structure", null, -10, 10, w.structure, value -> w.structure = value),
                number("weight_macabre", null, -10, 10, w.macabre, value -> w.macabre = value));
        list.addSmall(
                number("weight_hostile", null, -10, 10, w.hostile, value -> w.hostile = value),
                number("weight_dark", null, -10, 10, w.dark, value -> w.dark = value));
    }

    private void addServer(MidnightThoughtsConfig.ServerSettings server) {
        if (list == null) return;
        list.addSmall(
                flag("reset_phantom_timer", server.resetPhantomTimerForNonSleepers,
                        value -> server.resetPhantomTimerForNonSleepers = value),
                flag("suppress_vanilla_messages", server.suppressVanillaSleepMessages,
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
                clamp(current, min, max),
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
                clamp(Math.round(current * 100f), min, max),
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

    private static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    @Override
    public void removed() {
        super.removed();
        MidnightThoughtsConfig.getInstance().save();
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> {
                ComfortCalculator.clearCache();
                MTNetwork.sendToAll(server, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
            });
        }
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }
}
