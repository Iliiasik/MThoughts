package mt.integration.sereneseasons;

import mt.platform.MTPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Locale;

public final class SeasonsCompat {
    public record SeasonInfo(String nameKey, int day, int totalDays) {}

    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final String MOD_ID = "sereneseasons";
    private static final String HELPER_CLASS = "sereneseasons.api.season.SeasonHelper";
    private static final String NAME_KEY_PREFIX = "desc.sereneseasons.";
    private static final String DAY_KEY = "desc.sereneseasons.day_counter";

    private static boolean resolved;
    private static boolean available;
    private static Method getSeasonState;
    private static Method usesTropicalSeasons;
    private static Method getSubSeason;
    private static Method getTropicalSeason;
    private static Method getSeasonCycleTicks;
    private static Method getDayDuration;
    private static Method getSubSeasonDuration;

    private SeasonsCompat() {}

    public static String dayKey() {
        return DAY_KEY;
    }

    public static SeasonInfo current() {
        if (!resolved) resolve();
        if (!available) return null;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null) return null;

        try {
            Object state = getSeasonState.invoke(null, level);
            if (state == null) return null;

            int dayLength = (Integer) getDayDuration.invoke(state);
            int subLength = (Integer) getSubSeasonDuration.invoke(state);
            int ticks = (Integer) getSeasonCycleTicks.invoke(state);
            if (dayLength <= 0 || subLength <= 0) return null;

            int totalDays = Math.max(1, subLength / dayLength);
            int day = (ticks % subLength) / dayLength + 1;

            Holder<?> biome = level.getBiome(player.blockPosition());
            boolean tropical = Boolean.TRUE.equals(usesTropicalSeasons.invoke(null, biome));

            Object season = tropical
                    ? getTropicalSeason.invoke(state)
                    : getSubSeason.invoke(state);
            if (!(season instanceof Enum<?> value)) return null;

            return new SeasonInfo(
                    NAME_KEY_PREFIX + value.name().toLowerCase(Locale.ROOT),
                    day,
                    totalDays);
        } catch (Exception | LinkageError error) {
            disable(error);
            return null;
        }
    }

    private static void resolve() {
        resolved = true;
        if (!MTPlatform.isModLoaded(MOD_ID)) return;

        try {
            Class<?> helper = Class.forName(HELPER_CLASS);
            getSeasonState = helper.getMethod("getSeasonState", Level.class);
            usesTropicalSeasons = helper.getMethod("usesTropicalSeasons", Holder.class);

            Class<?> state = getSeasonState.getReturnType();
            getSubSeason = state.getMethod("getSubSeason");
            getTropicalSeason = state.getMethod("getTropicalSeason");
            getSeasonCycleTicks = state.getMethod("getSeasonCycleTicks");
            getDayDuration = state.getMethod("getDayDuration");
            getSubSeasonDuration = state.getMethod("getSubSeasonDuration");

            available = true;
            LOGGER.info("Serene Seasons detected, the summary will show the current season");
        } catch (Exception | LinkageError error) {
            disable(error);
        }
    }

    private static void disable(Throwable error) {
        available = false;
        getSeasonState = null;
        usesTropicalSeasons = null;
        getSubSeason = null;
        getTropicalSeason = null;
        getSeasonCycleTicks = null;
        getDayDuration = null;
        getSubSeasonDuration = null;
        LOGGER.warn("Serene Seasons is installed but its season API changed, the season badge is hidden: {}",
                error.toString());
    }
}
