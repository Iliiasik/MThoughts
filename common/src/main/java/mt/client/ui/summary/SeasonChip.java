package mt.client.ui.summary;

import mt.integration.sereneseasons.SeasonsCompat;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SeasonChip {

    private SeasonChip() {}

    public static List<String> tooltipLines() {
        SeasonsCompat.SeasonInfo info = SeasonsCompat.current();
        if (info == null) return List.of();

        List<String> lines = new ArrayList<>(2);
        lines.add(Component.translatable(info.nameKey()).getString());
        lines.add(Component.translatable(SeasonsCompat.DAY_KEY, info.day(), info.totalDays()).getString());
        return lines;
    }
}
