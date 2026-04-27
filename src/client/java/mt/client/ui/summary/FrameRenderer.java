package mt.client.ui.summary;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class FrameRenderer {

    private static int colorFromFloat(float alpha) {
        return ((int)(alpha * 255) << 24) | ((int)((float) 1.0 * 255) << 16) | ((int)((float) 1.0 * 255) << 8) | (int)((float) 1.0 * 255);
    }

    public static void render(GuiGraphicsExtractor graphics, SummaryDimensions dims, float fadeAlpha) {
        int color = colorFromFloat(fadeAlpha);
        graphics.pose().pushMatrix();
        graphics.pose().translate(dims.panelX, dims.panelY);
        graphics.pose().scale(dims.panelWidth / (float) SummaryConstants.FRAME_TEXTURE_WIDTH,
                dims.panelHeight / (float) SummaryConstants.FRAME_TEXTURE_HEIGHT);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getFrameTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
                color);
        graphics.pose().popMatrix();
    }

    public static void renderBadge(GuiGraphicsExtractor graphics, Font font, SummaryDimensions dims, float fadeAlpha) {
        Component title = Component.translatable("midnightthoughts.summary.title");

        int badgeWidth = dims.s(180);
        int badgeHeight = dims.s(60);

        int contentPaddingSides = dims.s(60);
        int badgeX = dims.panelX + contentPaddingSides + dims.s(11);
        int badgeY = dims.panelY - badgeHeight / 2 + dims.s(19);

        int color = colorFromFloat(fadeAlpha);
        graphics.pose().pushMatrix();
        graphics.pose().translate(badgeX, badgeY);
        graphics.pose().scale(badgeWidth / (float) SummaryConstants.BADGE_TEXTURE_WIDTH,
                badgeHeight / (float) SummaryConstants.BADGE_TEXTURE_HEIGHT);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getBadgeTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                color);
        graphics.pose().popMatrix();

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int titleAlpha = (int) (fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | colors.badgeTextColor();

        float textScale = dims.uiScale * 1.35f;
        int scaledTextWidth = (int) (font.width(title) * textScale);
        int titleX = badgeX + (badgeWidth - scaledTextWidth) / 2;
        int titleY = badgeY + (badgeHeight - (int) (font.lineHeight * textScale)) / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(titleX, titleY);
        graphics.pose().scale(textScale, textScale);
        graphics.text(font, title, 0, 0, titleColor, false);
        graphics.pose().popMatrix();
    }

    public static void renderPagesHolder(GuiGraphicsExtractor graphics, Font font, SummaryDimensions dims,
                                         int currentPage, int totalPages, float fadeAlpha) {
        if (totalPages <= 1) return;

        Component pageInfo = Component.translatable("midnightthoughts.summary.page", currentPage + 1, totalPages);

        int pagesHolderHeight = dims.s(45);
        int pagesHolderWidth = (int) (pagesHolderHeight * (SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH / (float) SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT));

        int contentPaddingBottom = dims.s(37);

        int pagesHolderX = dims.panelX + (dims.panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = dims.panelY + dims.panelHeight - contentPaddingBottom - pagesHolderHeight - dims.s(5);

        int color = colorFromFloat(fadeAlpha);
        graphics.pose().pushMatrix();
        graphics.pose().translate(pagesHolderX, pagesHolderY);
        graphics.pose().scale(pagesHolderWidth / (float) SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH,
                pagesHolderHeight / (float) SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getPagesHolderTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
                color);
        graphics.pose().popMatrix();

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int pageTextAlpha = (int) (fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | colors.pagesHolderTextColor();

        float textScale = dims.uiScale * 1.12f;
        int scaledTextWidth = (int) (font.width(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int) (font.lineHeight * textScale)) / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(pageTextX, pageTextY);
        graphics.pose().scale(textScale, textScale);
        graphics.text(font, pageInfo, 0, 0, pageTextColor, true);
        graphics.pose().popMatrix();
    }
}