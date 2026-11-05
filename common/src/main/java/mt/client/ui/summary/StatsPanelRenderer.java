package mt.client.ui.summary;

import mt.client.ui.engine.RenderHelper;
import mt.client.ui.engine.RenderUtils;
import mt.client.util.NumberFormatter;
import mt.client.util.Plurals;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.gui.Font;
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

    private static final String[] TABLE_KEYS = {
            "midnightthoughts.summary.table.blocks",
            "midnightthoughts.summary.table.distance",
            "midnightthoughts.summary.table.mobs",
            "midnightthoughts.summary.table.deaths",
            "midnightthoughts.summary.table.jumps",
            "midnightthoughts.summary.table.damage"
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

    public static void render(GuiGraphics context, Font font, DailySummaryPacket.PlayerDailySummary player,
                              SummaryLayout layout, float fadeAlpha, float countProgress, List<HoverArea> areas) {
        int x = layout.panelX();
        int y = layout.columnsY();
        int width = layout.panelWidth();
        int height = layout.panelHeight();

        if (ThemeColors.tableStyle()) {
            RenderHelper.blitTexture(context, SummaryConstants.getStatsTableTexture(),
                    x, y, width, height, fadeAlpha);
            renderTable(context, font, counted(values(player), countProgress), layout, fadeAlpha);
            return;
        }

        RenderHelper.blitTexture(context, SummaryConstants.getStatsPanelTexture(),
                x, y, width, height, fadeAlpha);

        areas.add(new HoverArea(x, y, width, height, lines(values(player))));
    }

    private static void renderTable(GuiGraphics context, Font font, int[] values,
                                    SummaryLayout layout, float fadeAlpha) {
        ThemeColors.TextStyle style = ThemeColors.current().table();
        int color = style.argb(fadeAlpha);
        float scale = layout.textScale();
        int left = layout.tableTextLeft();
        int right = layout.tableTextRight();

        for (int row = 0; row < SummaryLayout.TABLE_ROWS; row++) {
            int y = layout.tableRowY(row);
            String value = NumberFormatter.formatLargeNumber(values[row]);
            int valueWidth = RenderUtils.scaledWidth(font, value, scale);
            int labelBudget = right - left - valueWidth - layout.s(SummaryLayout.TABLE_MIN_GAP);
            String label = RenderUtils.truncateWithEllipsis(font,
                    Component.translatable(TABLE_KEYS[row]).getString(),
                    Math.max(1, Math.round(labelBudget / scale)));

            RenderUtils.renderScaledText(context, font, label, left, y, color, scale, style.shadow());
            RenderUtils.renderScaledText(context, font, value, right - valueWidth, y, color, scale, style.shadow());
        }
    }

    static int[] counted(int[] values, float progress) {
        if (progress >= 1.0f) return values;
        float eased = Math.max(0.0f, progress) * (2.0f - Math.max(0.0f, progress));
        int[] shown = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            shown[i] = (int) Math.round(values[i] * (double) eased);
        }
        return shown;
    }

    private static int[] values(DailySummaryPacket.PlayerDailySummary player) {
        return new int[]{
                player.blocksDestroyed(),
                player.distanceWalked() / 100,
                player.mobsKilled(),
                player.deaths(),
                player.jumps(),
                player.damageDealt() / 10
        };
    }

    private static List<String> lines(int[] values) {
        List<String> lines = new ArrayList<>(KEYS.length);
        for (int i = 0; i < KEYS.length; i++) {
            lines.add(Component.translatable(KEYS[i],
                    NumberFormatter.formatLargeNumber(values[i]),
                    Plurals.unit(UNITS[i], values[i])).getString());
        }
        return lines;
    }
}
