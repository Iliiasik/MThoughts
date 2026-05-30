package mt.client.ui.summary;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import mt.client.util.NumberFormatter;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StatBadgeRenderer {

    private record StatEntry(Identifier icon, String labelKey, int value) {}

    public static void render(DrawContext context, TextRenderer textRenderer, DailySummaryPacket.PlayerDailySummary player,
                              int x, int y, int width, int height, SummaryDimensions dims,
                              float fadeAlpha, long animationStartTime) {
        float animProgress = AnimationHelper.getProgress(animationStartTime);

        StatEntry[] stats = {
                new StatEntry(SummaryConstants.ICON_BLOCKS, "midnightthoughts.summary.blocks",
                        NumberFormatter.safeAnimatedValue(player.blocksDestroyed(), animProgress)),
                new StatEntry(SummaryConstants.ICON_DISTANCE, "midnightthoughts.summary.distance",
                        NumberFormatter.safeAnimatedValue(NumberFormatter.safeDivide(player.distanceWalked(), 100), animProgress)),
                new StatEntry(SummaryConstants.ICON_SWORD, "midnightthoughts.summary.mobs",
                        NumberFormatter.safeAnimatedValue(player.mobsKilled(), animProgress)),
                new StatEntry(SummaryConstants.ICON_DEATH, "midnightthoughts.summary.deaths",
                        NumberFormatter.safeAnimatedValue(player.deaths(), animProgress)),
                new StatEntry(SummaryConstants.ICON_JUMP, "midnightthoughts.summary.jumps",
                        NumberFormatter.safeAnimatedValue(player.jumps(), animProgress)),
                new StatEntry(SummaryConstants.ICON_AXE, "midnightthoughts.summary.damage",
                        NumberFormatter.safeAnimatedValue(NumberFormatter.safeDivide(player.damageDealt(), 10), animProgress)),
        };

        BadgeDimensions badgeDims = BadgeDimensions.calculate(dims);
        int badgeH = badgeDims.height();
        int rowSpacing = badgeDims.rowSpacing();
        int iconSize = Math.max(6, Math.min(badgeH - 2, (int) (badgeH * 0.7f)));
        float textScale = badgeDims.textScale();

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int colGap = Math.max(3, dims.s(5));
        int colW = (width - colGap) / 2;
        int col2X = x + colW + colGap;

        int totalH = 3 * badgeH + 2 * rowSpacing;
        int startY = y + Math.max(0, (height - totalH) / 2);

        for (int i = 0; i < 3; i++) {
            int rowY = startY + i * (badgeH + rowSpacing);
            if (rowY + badgeH > y + height) break;
            renderBadge(context, textRenderer, x, rowY, colW, badgeH,
                    stats[i].icon(), stats[i].labelKey(), stats[i].value(), iconSize, textScale, fadeAlpha, colors);
        }
        for (int i = 0; i < 3; i++) {
            int rowY = startY + i * (badgeH + rowSpacing);
            if (rowY + badgeH > y + height) break;
            renderBadge(context, textRenderer, col2X, rowY, colW, badgeH,
                    stats[i + 3].icon(), stats[i + 3].labelKey(), stats[i + 3].value(), iconSize, textScale, fadeAlpha, colors);
        }
    }

    private static void renderBadge(DrawContext context, TextRenderer textRenderer,
                                    int x, int y, int width, int height,
                                    Identifier icon, String labelKey, int value,
                                    int iconSize, float textScale, float fadeAlpha, ThemeColors.ThemeColor colors) {
        int texW = SummaryConstants.STAT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.STAT_BADGE_TEXTURE_HEIGHT;
        RenderHelper.ScaledBlit sb = RenderHelper.computeScaledBlit(y, width, height, texW, texH);

        RenderHelper.blitTexture(context, SummaryConstants.getStatBadgeTexture(),
                x, sb.renderY(), sb.renderW(), sb.renderH(), fadeAlpha);

        int padX = Math.max(3, (int) (4 * ((float) sb.renderH() / texH)));
        int iconY = y + (height - iconSize) / 2;

        RenderHelper.blitTextureSimple(context, icon, x + padX, iconY, iconSize, iconSize, iconSize, iconSize, fadeAlpha);

        int textY = y + (height - (int) (8 * textScale)) / 2;
        int alpha = (int) (fadeAlpha * 255);
        int textColor = (alpha << 24) | colors.statTextColor();

        String label = Text.translatable(labelKey).getString();
        RenderUtils.renderScaledText(context, textRenderer, label, x + padX + iconSize + padX, textY, textColor, textScale, true);

        String valueStr = NumberFormatter.formatLargeNumber(value);
        int valueW = (int) (textRenderer.getWidth(valueStr) * textScale);
        int valueX = x + sb.renderW() - padX - valueW;
        RenderUtils.renderScaledText(context, textRenderer, valueStr, valueX, textY, textColor, textScale, true);
    }
}