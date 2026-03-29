package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.server.config.MidnightThoughtsConfig;
import mt.client.util.NumberFormatter;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StatBadgeRenderer {

    private record StatEntry(ResourceLocation icon, String labelKey, int value) {}

    public static void render(GuiGraphics context, Font textRenderer, DailySummaryPacket.PlayerDailySummary player,
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
        int iconSize = Math.max(6, Math.min(badgeH - 2, (int)(badgeH * 0.7f)));
        float textScale = badgeDims.textScale();

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
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

    private static void renderBadge(GuiGraphics context, Font textRenderer,
                                    int x, int y, int width, int height,
                                    ResourceLocation icon, String labelKey, int value,
                                    int iconSize, float textScale, float fadeAlpha,
                                    ThemeColors.ThemeColor colors) {
        int texW = SummaryConstants.STAT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.STAT_BADGE_TEXTURE_HEIGHT;
        float scaleH = (float) height / texH;
        float scaleW = (float) width / texW;
        float scale = Math.min(scaleH, scaleW);
        int renderW = (int)(texW * scale);
        int renderH = (int)(texH * scale);
        int renderY = y + (height - renderH) / 2;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        context.pose().pushPose();
        context.pose().translate(x, renderY, 0);
        context.pose().scale((float) renderW / texW, (float) renderH / texH, 1.0f);
        context.blit(SummaryConstants.getStatBadgeTexture(), 0, 0, 0, 0, texW, texH, texW, texH);
        context.pose().popPose();

        int padX = Math.max(3, (int)(4 * scale));
        int iconY = y + (height - iconSize) / 2;
        context.blit(icon, x + padX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int textY = y + (height - (int)(8 * textScale)) / 2;
        int alpha = (int)(fadeAlpha * 255);
        int textColor = (alpha << 24) | colors.statTextColor();

        String label = Component.translatable(labelKey).getString();
        RenderUtils.renderScaledText(context, textRenderer, label, x + padX + iconSize + padX, textY, textColor, textScale, false);

        String valueStr = NumberFormatter.formatLargeNumber(value);
        int valueW = (int)(textRenderer.width(valueStr) * textScale);
        int valueX = x + renderW - padX - valueW;
        RenderUtils.renderScaledText(context, textRenderer, valueStr, valueX, textY, textColor, textScale, false);
    }
}