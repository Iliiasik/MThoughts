package mt.client.ui.summary;

import mt.config.MidnightThoughtsConfig;
import mt.integration.eclipticseasons.EclipticSeasonsCompat;
import mt.integration.sereneseasons.SeasonsCompat;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SeasonChip {

    private static final String DAY_KEY = "midnightthoughts.summary.season_day";

    private SeasonChip() {}

    public static List<String> tooltipLines() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        List<String> lines = new ArrayList<>(4);

        SeasonsCompat.SeasonInfo season = config.isSereneSeasonsIntegration() ? SeasonsCompat.current() : null;
        if (season != null) {
            lines.add(Component.translatable(season.nameKey()).getString());
            lines.add(Component.translatable(SeasonsCompat.DAY_KEY, season.day(), season.totalDays()).getString());
        }

        EclipticSeasonsCompat.TermInfo term = config.isEclipticSeasonsIntegration() ? EclipticSeasonsCompat.current() : null;
        if (term != null) {
            lines.add(term.name());
            lines.add(Component.translatable(DAY_KEY, term.day(), term.totalDays()).getString());
        }

        return lines;
    }
}
