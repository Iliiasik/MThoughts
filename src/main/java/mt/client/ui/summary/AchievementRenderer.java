package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.server.AchievementDefinition;
import mt.server.AchievementLoader;
import mt.server.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AchievementRenderer {

    public static void render(GuiGraphics context, Font textRenderer, List<String> achievements,
                              int x, int y, int width, int height, SummaryDimensions dims,
                              float fadeAlpha, List<AchievementTooltipArea> achievementAreas) {
        int maxAchievements = 3;
        int count = Math.min(achievements.size(), maxAchievements);
        if (count == 0) return;

        BadgeDimensions badgeDims = BadgeDimensions.calculate(dims);
        int badgeH = badgeDims.height();
        int rowSpacing = badgeDims.rowSpacing();
        float textScale = badgeDims.textScale();

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int texW = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_HEIGHT;
        float badgeScale = Math.min((float) badgeH / texH, (float) width / texW);
        int renderW = (int)(texW * badgeScale);

        int totalH = count * badgeH + (count - 1) * rowSpacing;
        int startY = y + Math.max(0, (height - totalH) / 2);

        for (int i = 0; i < count; i++) {
            String achievementId = achievements.get(i);
            int rowY = startY + i * (badgeH + rowSpacing);
            if (rowY + badgeH > y + height) break;

            int renderH = (int)(texH * badgeScale);
            int actualY = rowY + (badgeH - renderH) / 2;
            renderBadge(context, textRenderer, x, rowY, width, badgeH, achievementId, textScale, fadeAlpha, colors);
            achievementAreas.add(new AchievementTooltipArea(x, actualY, renderW, renderH, achievementId));
        }
    }

    private static void renderBadge(GuiGraphics context, Font textRenderer,
                                    int x, int y, int width, int height,
                                    String achievementId, float textScale, float fadeAlpha,
                                    ThemeColors.ThemeColor colors) {
        int texW = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_HEIGHT;
        float scaleH = (float) height / texH;
        float scaleW = (float) width / texW;
        float scale = Math.min(scaleH, scaleW);
        int renderW = (int)(texW * scale);
        int renderH = (int)(texH * scale);
        int renderY = y + (height - renderH) / 2;

        int alpha = (int)(fadeAlpha * 255);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        context.pose().pushPose();
        context.pose().translate(x, renderY, 0);
        context.pose().scale((float) renderW / texW, (float) renderH / texH, 1.0f);
        context.blit(SummaryConstants.getAchievementBadgeTexture(),
                0, 0, 0, 0, texW, texH, texW, texH);
        context.pose().popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        String name = resolveAchievementName(achievementId);
        int padX = Math.max(3, (int)(4 * scale));
        int maxTextW = width - padX * 2;

        String ellipsis = "...";
        int ellipsisW = textRenderer.width(ellipsis);
        if (textRenderer.width(name) > maxTextW) {
            while (!name.isEmpty() && textRenderer.width(name) + ellipsisW > maxTextW) {
                name = name.substring(0, name.length() - 1);
            }
            name = name + ellipsis;
        }

        int textColor = (alpha << 24) | colors.achievementTextColor();
        int textY = renderY + (renderH - (int)(8 * textScale)) / 2;
        RenderUtils.renderScaledText(context, textRenderer, name, x + padX, textY, textColor, textScale, false);
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