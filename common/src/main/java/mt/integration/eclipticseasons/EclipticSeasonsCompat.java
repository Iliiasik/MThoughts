package mt.integration.eclipticseasons;

import mt.platform.MTPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public final class EclipticSeasonsCompat {
    public record TermInfo(String name, int day, int totalDays) {}

    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final String MOD_ID = "eclipticseasons";
    private static final String API_CLASS = "com.teamtea.eclipticseasons.api.EclipticSeasonsApi";
    private static final String NO_TERM = "NONE";

    private static boolean resolved;
    private static boolean available;
    private static Object api;
    private static Method isSeasonEnabled;
    private static Method getSolarTerm;
    private static Method getTranslation;
    private static Method getDayInTerm;
    private static Method getLastingDaysOfEachTerm;

    private EclipticSeasonsCompat() {}

    public static TermInfo current() {
        if (!resolved) resolve();
        if (!available) return null;

        Level level = Minecraft.getInstance().level;
        if (level == null) return null;

        try {
            if (!Boolean.TRUE.equals(isSeasonEnabled.invoke(api, level))) return null;

            Object term = getSolarTerm.invoke(api, level);
            if (!(term instanceof Enum<?> value) || NO_TERM.equals(value.name())) return null;
            if (!(getTranslation.invoke(term) instanceof Component name)) return null;

            int totalDays = Math.max(1, (Integer) getLastingDaysOfEachTerm.invoke(api, level));
            int day = Mth.clamp((Integer) getDayInTerm.invoke(api, level) + 1, 1, totalDays);

            return new TermInfo(name.getString(), day, totalDays);
        } catch (Exception | LinkageError error) {
            disable(error);
            return null;
        }
    }

    private static void resolve() {
        resolved = true;
        if (!MTPlatform.isModLoaded(MOD_ID)) return;

        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            api = apiClass.getMethod("getInstance").invoke(null);
            isSeasonEnabled = apiClass.getMethod("isSeasonEnabled", Level.class);
            getSolarTerm = apiClass.getMethod("getSolarTerm", Level.class);
            getTranslation = getSolarTerm.getReturnType().getMethod("getTranslation");
            getDayInTerm = apiClass.getMethod("getDayInTerm", Level.class);
            getLastingDaysOfEachTerm = apiClass.getMethod("getLastingDaysOfEachTerm", Level.class);

            available = api != null;
            if (available) LOGGER.info("Ecliptic Seasons detected, the summary will show the current solar term");
        } catch (Exception | LinkageError error) {
            disable(error);
        }
    }

    private static void disable(Throwable error) {
        available = false;
        LOGGER.warn("Ecliptic Seasons is installed but its API changed, the solar term is hidden: {}",
                error.toString());
    }
}
