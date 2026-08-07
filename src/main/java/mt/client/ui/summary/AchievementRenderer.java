package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.cache.ClientAchievementCache;
import mt.client.config.ClientConfig;
import mt.server.AchievementDefinition;
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

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int texW = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_HEIGHT;

        int totalH = count * badgeH + (count - 1) * rowSpacing;
        int startY = y + Math.max(0, (height - totalH) / 2);

        for (int i = 0; i < count; i++) {
            String achievementId = achievements.get(i);
            int rowY = startY + i * (badgeH + rowSpacing);
            if (rowY + badgeH > y + height) break;

            RenderHelper.ScaledBlit sb = RenderHelper.computeScaledBlit(rowY, width, badgeH, texW, texH);
            renderBadge(context, textRenderer, x, rowY, width, badgeH, achievementId, textScale, fadeAlpha, colors);
            achievementAreas.add(new AchievementTooltipArea(x, sb.renderY(), sb.renderW(), sb.renderH(), achievementId));
        }
    }

    private static void renderBadge(GuiGraphics context, Font textRenderer,
                                    int x, int y, int width, int height,
                                    String achievementId, float textScale, float fadeAlpha,
                                    ThemeColors.ThemeColor colors) {
        int texW = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_HEIGHT;
        RenderHelper.ScaledBlit sb = RenderHelper.computeScaledBlit(y, width, height, texW, texH);

        int alpha = (int) (fadeAlpha * 255);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        context.pose().pushPose();
        context.pose().translate(x, sb.renderY(), 0);
        context.pose().scale((float) sb.renderW() / texW, (float) sb.renderH() / texH, 1.0f);
        context.blit(SummaryConstants.getAchievementBadgeTexture(), 0, 0, 0, 0, texW, texH, texW, texH);
        context.pose().popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        String name = resolveAchievementName(achievementId);
        int padX = Math.max(3, (int) (4 * ((float) sb.renderH() / texH)));
        int maxTextW = (int) ((sb.renderW() - padX * 2) / textScale);
        name = RenderUtils.truncateWithEllipsis(textRenderer, name, maxTextW);

        int textColor = (alpha << 24) | colors.achievementTextColor();
        int textY = sb.renderY() + (sb.renderH() - (int) (8 * textScale)) / 2;
        int textW = (int) (textRenderer.width(name) * textScale);
        int textX = x + Math.max(padX, (sb.renderW() - textW) / 2);
        RenderUtils.renderScaledText(context, textRenderer, name, textX, textY, textColor, textScale, true);
    }

    private static String resolveAchievementName(String achievementId) {
        for (AchievementDefinition def : ClientAchievementCache.get()) {
            if (def.id.equals(achievementId)) {
                return def.name;
            }
        }
        return Component.translatable("midnightthoughts.achievement." + achievementId).getString();
    }

    public static String resolveAchievementTooltip(String achievementId) {
        for (AchievementDefinition def : ClientAchievementCache.get()) {
            if (def.id.equals(achievementId)) {
                return def.tooltip != null ? def.tooltip : def.name;
            }
        }
        return Component.translatable("midnightthoughts.achievement." + achievementId + ".desc").getString();
    }
}
