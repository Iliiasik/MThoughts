package mt.client.ui.summary;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import mt.server.AchievementLoader;
import mt.server.AchievementDefinition;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AchievementRenderer {

    public static void render(GuiGraphicsExtractor graphics, Font font, List<String> achievements,
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

            renderBadge(graphics, font, x, rowY, width, badgeH, achievementId, textScale, fadeAlpha, colors);
            achievementAreas.add(new AchievementTooltipArea(x, actualY, renderW, renderH, achievementId));
        }
    }

    private static void renderBadge(GuiGraphicsExtractor graphics, Font font,
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

        int color = (int) (fadeAlpha * 255) << 24 | 0xFFFFFF;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, renderY);
        graphics.pose().scale((float) renderW / texW, (float) renderH / texH);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getAchievementBadgeTexture(),
                0, 0, 0.0f, 0.0f, texW, texH, texW, texH, texW, texH, color);
        graphics.pose().popMatrix();

        String name = resolveAchievementName(achievementId);
        int padX = Math.max(3, (int) (4 * scale));

        int maxTextW = (int) ((renderW - padX * 2) / textScale);

        String ellipsis = "...";
        int ellipsisW = font.width(ellipsis);
        if (font.width(name) > maxTextW) {
            while (!name.isEmpty() && font.width(name) + ellipsisW > maxTextW) {
                name = name.substring(0, name.length() - 1);
            }
            name = name + ellipsis;
        }

        int alpha = (int) (fadeAlpha * 255);
        int textColor = (alpha << 24) | colors.achievementTextColor();
        int textY = renderY + (renderH - (int) (8 * textScale)) / 2;
        RenderUtils.renderScaledText(graphics, font, name, x + padX, textY, textColor, textScale, true);
    }

    private static String resolveAchievementName(String achievementId) {
        List<AchievementDefinition> customAchievements = AchievementLoader.load();
        for (AchievementDefinition def : customAchievements) {
            if (def.id.equals(achievementId)) {
                return def.name;
            }
        }
        return Component.translatable("midnightthoughts.achievement." + achievementId).getString();
    }

    public static String resolveAchievementTooltip(String achievementId) {
        List<AchievementDefinition> customAchievements = AchievementLoader.load();
        for (AchievementDefinition def : customAchievements) {
            if (def.id.equals(achievementId)) {
                return def.tooltip != null ? def.tooltip : def.name;
            }
        }
        return Component.translatable("midnightthoughts.achievement." + achievementId + ".desc").getString();
    }
}