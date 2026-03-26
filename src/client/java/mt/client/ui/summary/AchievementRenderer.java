package mt.client.ui.summary;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class AchievementRenderer {

    public static void render(DrawContext context, TextRenderer textRenderer, List<String> achievements,
                              int x, int y, int width, int height, SummaryDimensions dims,
                              float fadeAlpha, List<AchievementTooltipArea> achievementAreas) {
        int maxAchievements = 3;
        int count = Math.min(achievements.size(), maxAchievements);
        if (count == 0) return;

        BadgeDimensions badgeDims = BadgeDimensions.calculate(dims);
        int badgeH = badgeDims.height();
        int rowSpacing = badgeDims.rowSpacing();
        float textScale = badgeDims.textScale();

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int texW = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_HEIGHT;
        float badgeScale = Math.min((float) badgeH / texH, (float) width / texW);
        int renderW = (int) (texW * badgeScale);

        int totalH = count * badgeH + (count - 1) * rowSpacing;
        int startY = y + Math.max(0, (height - totalH) / 2);

        for (int i = 0; i < count; i++) {
            String achievementId = achievements.get(i);
            int rowY = startY + i * (badgeH + rowSpacing);
            if (rowY + badgeH > y + height) break;

            int renderH = (int) (texH * badgeScale);
            int actualY = rowY + (badgeH - renderH) / 2;
            renderBadge(context, textRenderer, x, rowY, width, badgeH, achievementId, textScale, fadeAlpha, colors);
            achievementAreas.add(new AchievementTooltipArea(x, actualY, renderW, renderH, achievementId));
        }
    }

    private static void renderBadge(DrawContext context, TextRenderer textRenderer,
                                    int x, int y, int width, int height,
                                    String achievementId, float textScale, float fadeAlpha,
                                    ThemeColors.ThemeColor colors) {
        int texW = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_HEIGHT;
        float scaleH = (float) height / texH;
        float scaleW = (float) width / texW;
        float scale = Math.min(scaleH, scaleW);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int renderY = y + (height - renderH) / 2;

        Identifier badgeTex = SummaryConstants.getAchievementBadgeTexture();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(x, renderY, 0);
        context.getMatrices().scale((float) renderW / texW, (float) renderH / texH, 1.0f);
        context.drawTexture(badgeTex, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH);
        context.getMatrices().pop();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        int padX = Math.max(3, (int) (4 * scale));
        String text = Text.translatable("midnightthoughts.achievement." + achievementId).getString();
        int alpha = (int) (fadeAlpha * 255);
        int textColor = (alpha << 24) | colors.achievementTextColor();
        int textY = renderY + (renderH - (int) (8 * textScale)) / 2;
        RenderUtils.renderScaledText(context, textRenderer, text, x + padX, textY, textColor, textScale, false);
    }
}