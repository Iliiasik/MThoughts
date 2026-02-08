package mt.client.ui.summary;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public class AchievementRenderer {

    public static void render(DrawContext context, TextRenderer textRenderer, int x, int y1, int y2,
                             List<String> achievements, BadgeDimensions dims, int maxWidth,
                             SummaryDimensions screenDims, float fadeAlpha, List<AchievementTooltipArea> achievementAreas) {

        int padding = (int)(3 * screenDims.uiScale);
        int spacing = (int)(3 * screenDims.uiScale);
        int maxAchievements = screenDims.isCompactMode ? 2 : 4;

        int currentX = x;
        int currentY = y1;
        int count = 0;

        for (String achievementId : achievements) {
            if (count >= maxAchievements) break;

            String achievementText = Text.translatable("midnightthoughts.achievement." + achievementId).getString();
            int textWidth = (int)(textRenderer.getWidth(achievementText) * dims.textScale());
            int badgeWidth = textWidth + padding * 2;

            if (shouldMoveToNextRow(currentX, badgeWidth, x, maxWidth, currentY, y1)) {
                currentX = x;
                currentY = y2;
            }

            if (shouldStopRendering(currentX, badgeWidth, x, maxWidth, currentY, y2)) {
                break;
            }

            renderBadge(context, textRenderer, currentX, currentY, badgeWidth, dims.height(), padding,
                       achievementText, dims.textScale(), fadeAlpha);
            achievementAreas.add(new AchievementTooltipArea(currentX, currentY, badgeWidth, dims.height(), achievementId));

            currentX += badgeWidth + spacing;
            count++;
        }
    }

    private static boolean shouldMoveToNextRow(int currentX, int badgeWidth, int x, int maxWidth, int currentY, int y1) {
        return currentX + badgeWidth > x + maxWidth && currentY == y1;
    }

    private static boolean shouldStopRendering(int currentX, int badgeWidth, int x, int maxWidth, int currentY, int y2) {
        return currentX + badgeWidth > x + maxWidth && currentY == y2;
    }

    private static void renderBadge(DrawContext context, TextRenderer textRenderer, int x, int y, int width,
                                    int height, int padding, String text, float textScale, float fadeAlpha) {
        int alpha = (int)(fadeAlpha * 200);
        int bgColor = (alpha << 24) | 0x8a6a2a;
        context.fill(x, y, x + width, y + height, bgColor);

        int textColor = ((int)(fadeAlpha * 255) << 24) | 0xffd700;
        int textY = y + (height - (int)(8 * textScale)) / 2;

        RenderUtils.renderScaledText(context, textRenderer, text, x + padding, textY, textColor, textScale, false);
    }
}

