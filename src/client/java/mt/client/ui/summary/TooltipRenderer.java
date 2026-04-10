package mt.client.ui.summary;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class TooltipRenderer {

    public static void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY,
                              List<AchievementTooltipArea> achievementAreas, int screenWidth, int screenHeight, float fadeAlpha) {
        for (AchievementTooltipArea area : achievementAreas) {
            if (area.contains(mouseX, mouseY)) {
                renderTooltip(context, textRenderer, mouseX, mouseY, area.achievementId(), screenWidth, screenHeight, fadeAlpha);
                break;
            }
        }
    }

    private static void renderTooltip(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY,
                                      String achievementId, int screenWidth, int screenHeight, float fadeAlpha) {
        String tooltipText = AchievementRenderer.resolveAchievementTooltip(achievementId);

        int tooltipPadding = 4;
        int tooltipWidth = textRenderer.getWidth(tooltipText) + tooltipPadding * 2;
        int tooltipHeight = 12;

        int tooltipX = mouseX + 8;
        int tooltipY = mouseY - tooltipHeight - 4;

        if (tooltipX + tooltipWidth > screenWidth) tooltipX = mouseX - tooltipWidth - 8;
        if (tooltipY < 0) tooltipY = mouseY + 16;

        int bgAlpha = (int) (fadeAlpha * 240);
        int bgColor = (bgAlpha << 24) | 0x1a1a2e;
        int borderColor = (bgAlpha << 24) | 0x8a6a2a;

        context.fill(tooltipX - 1, tooltipY - 1, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight + 1, borderColor);
        context.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, bgColor);

        int textColor = ((int) (fadeAlpha * 255) << 24) | 0xffd700;
        context.drawText(textRenderer, tooltipText, tooltipX + tooltipPadding, tooltipY + 2, textColor, false);
    }
}