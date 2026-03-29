package mt.client.ui.summary;

import mt.server.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class FrameRenderer {

    public static void render(GuiGraphics context, SummaryDimensions dims, float fadeAlpha) {
        int color = ARGB.colorFromFloat(fadeAlpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(dims.panelX, dims.panelY);
        context.pose().scale(dims.panelWidth / (float)SummaryConstants.FRAME_TEXTURE_WIDTH,
                dims.panelHeight / (float)SummaryConstants.FRAME_TEXTURE_HEIGHT);
        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getFrameTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
                color);
        context.pose().popMatrix();
    }

    public static void renderBadge(GuiGraphics context, Font textRenderer, SummaryDimensions dims, float fadeAlpha) {
        Component title = Component.translatable("midnightthoughts.summary.title");

        int badgeWidth = dims.s(180);
        int badgeHeight = dims.s(60);

        int contentPaddingSides = dims.s(60);
        int badgeX = dims.panelX + contentPaddingSides + dims.s(11);
        int badgeY = dims.panelY - badgeHeight / 2 + dims.s(19);

        int color = ARGB.colorFromFloat(fadeAlpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(badgeX, badgeY);
        context.pose().scale(badgeWidth / (float)SummaryConstants.BADGE_TEXTURE_WIDTH,
                badgeHeight / (float)SummaryConstants.BADGE_TEXTURE_HEIGHT);
        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getBadgeTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                color);
        context.pose().popMatrix();

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int titleAlpha = (int)(fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | colors.badgeTextColor();

        float textScale = dims.uiScale * 1.35f;
        int scaledTextWidth = (int)(textRenderer.width(title) * textScale);
        int titleX = badgeX + (badgeWidth - scaledTextWidth) / 2;
        int titleY = badgeY + (badgeHeight - (int)(textRenderer.lineHeight * textScale)) / 2;

        context.pose().pushMatrix();
        context.pose().translate(titleX, titleY);
        context.pose().scale(textScale, textScale);
        context.drawString(textRenderer, title, 0, 0, titleColor, false);
        context.pose().popMatrix();
    }

    public static void renderPagesHolder(GuiGraphics context, Font textRenderer, SummaryDimensions dims,
                                         int currentPage, int totalPages, float fadeAlpha) {
        if (totalPages <= 1) return;

        Component pageInfo = Component.translatable("midnightthoughts.summary.page", currentPage + 1, totalPages);

        int pagesHolderHeight = dims.s(45);
        int pagesHolderWidth = (int)(pagesHolderHeight * (SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT));

        int contentPaddingBottom = dims.s(37);

        int pagesHolderX = dims.panelX + (dims.panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = dims.panelY + dims.panelHeight - contentPaddingBottom - pagesHolderHeight - dims.s(5);

        int color = ARGB.colorFromFloat(fadeAlpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(pagesHolderX, pagesHolderY);
        context.pose().scale(pagesHolderWidth / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH,
                pagesHolderHeight / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT);
        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getPagesHolderTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
                color);
        context.pose().popMatrix();

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int pageTextAlpha = (int)(fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | colors.pagesHolderTextColor();

        float textScale = dims.uiScale * 1.12f;
        int scaledTextWidth = (int)(textRenderer.width(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int)(textRenderer.lineHeight * textScale)) / 2;

        context.pose().pushMatrix();
        context.pose().translate(pageTextX, pageTextY);
        context.pose().scale(textScale, textScale);
        context.drawString(textRenderer, pageInfo, 0, 0, pageTextColor, false);
        context.pose().popMatrix();
    }
}