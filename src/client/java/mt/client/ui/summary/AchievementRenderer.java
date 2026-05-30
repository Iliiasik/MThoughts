package mt.client.ui.summary;

import mt.cache.ClientAchievementCache;
import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import mt.server.AchievementDefinition;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

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

    private static void renderBadge(DrawContext context, TextRenderer textRenderer,
                                    int x, int y, int width, int height,
                                    String achievementId, float textScale, float fadeAlpha,
                                    ThemeColors.ThemeColor colors) {
        int texW = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.ACHIEVEMENT_BADGE_TEXTURE_HEIGHT;
        RenderHelper.ScaledBlit sb = RenderHelper.computeScaledBlit(y, width, height, texW, texH);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(x, sb.renderY(), 0);
        context.getMatrices().scale((float) sb.renderW() / texW, (float) sb.renderH() / texH, 1.0f);
        context.drawTexture(SummaryConstants.getAchievementBadgeTexture(), 0, 0, 0.0f, 0.0f, texW, texH, texW, texH);
        context.getMatrices().pop();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        String name = resolveAchievementName(achievementId);
        int padX = Math.max(3, (int) (4 * ((float) sb.renderH() / texH)));
        int maxTextW = (int) ((sb.renderW() - padX * 2) / textScale);
        name = RenderUtils.truncateWithEllipsis(textRenderer, name, maxTextW);

        int alpha = (int) (fadeAlpha * 255);
        int textColor = (alpha << 24) | colors.achievementTextColor();
        int textY = sb.renderY() + (sb.renderH() - (int) (8 * textScale)) / 2;
        RenderUtils.renderScaledText(context, textRenderer, name, x + padX, textY, textColor, textScale, true);
    }

    private static String resolveAchievementName(String achievementId) {
        for (AchievementDefinition def : ClientAchievementCache.get()) {
            if (def.id.equals(achievementId)) {
                return def.name;
            }
        }
        return Text.translatable("midnightthoughts.achievement." + achievementId).getString();
    }

    public static String resolveAchievementTooltip(String achievementId) {
        for (AchievementDefinition def : ClientAchievementCache.get()) {
            if (def.id.equals(achievementId)) {
                return def.tooltip != null ? def.tooltip : def.name;
            }
        }
        return Text.translatable("midnightthoughts.achievement." + achievementId + ".desc").getString();
    }
}