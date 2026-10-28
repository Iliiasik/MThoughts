package mt.client.ui.summary;

import mt.client.ui.engine.RenderHelper;
import mt.client.util.NumberFormatter;
import mt.client.util.Plurals;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class StatsPanelRenderer {

    private static final String[] KEYS = {
            "midnightthoughts.summary.stat.blocks",
            "midnightthoughts.summary.stat.distance",
            "midnightthoughts.summary.stat.mobs",
            "midnightthoughts.summary.stat.deaths",
            "midnightthoughts.summary.stat.jumps",
            "midnightthoughts.summary.stat.damage"
    };

    private static final String[] UNITS = {
            "midnightthoughts.summary.unit.blocks",
            "midnightthoughts.summary.unit.blocks",
            "midnightthoughts.summary.unit.mobs",
            "midnightthoughts.summary.unit.times",
            "midnightthoughts.summary.unit.times",
            "midnightthoughts.summary.unit.damage"
    };

    private StatsPanelRenderer() {}

    public static void render(GuiGraphics context, DailySummaryPacket.PlayerDailySummary player,
                              SummaryLayout layout, float fadeAlpha, List<HoverArea> areas) {
        int x = layout.panelX();
        int y = layout.columnsY();
        int width = layout.panelWidth();
        int height = layout.panelHeight();

        RenderHelper.blitTexture(context, SummaryConstants.getStatsPanelTexture(),
                x, y, width, height, fadeAlpha);

        areas.add(new HoverArea(x, y, width, height, lines(player)));
    }

    private static List<String> lines(DailySummaryPacket.PlayerDailySummary player) {
        int[] values = {
                player.blocksDestroyed(),
                NumberFormatter.safeDivide(player.distanceWalked(), 100),
                player.mobsKilled(),
                player.deaths(),
                player.jumps(),
                NumberFormatter.safeDivide(player.damageDealt(), 10)
        };

        List<String> lines = new ArrayList<>(KEYS.length);
        for (int i = 0; i < KEYS.length; i++) {
            lines.add(Component.translatable(KEYS[i],
                    NumberFormatter.formatLargeNumber(values[i]),
                    Plurals.unit(UNITS[i], values[i])).getString());
        }
        return lines;
    }
}
