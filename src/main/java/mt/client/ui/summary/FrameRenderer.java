package mt.client.ui.summary;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FrameRenderer {

    public static void render(GuiGraphics context, SummaryDimensions dims, float fadeAlpha) {
        RenderHelper.blitTexture(context, SummaryConstants.getFrameTexture(),
                dims.panelX, dims.panelY, dims.panelWidth, dims.panelHeight, fadeAlpha);
    }

    public static void renderBadge(GuiGraphics context, Font textRenderer, SummaryDimensions dims, float fadeAlpha) {
        Component title = Component.translatable("midnightthoughts.summary.title");

        int badgeWidth = dims.s(180);
        int badgeHeight = dims.s(60);

        int contentPaddingSides = dims.s(60);
        int badgeX = dims.panelX + contentPaddingSides + dims.s(11);
        int badgeY = dims.panelY - badgeHeight / 2 + dims.s(19);

        RenderHelper.blitTexture(context, SummaryConstants.getBadgeTexture(),
                badgeX, badgeY, badgeWidth, badgeHeight, fadeAlpha);

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int titleAlpha = (int) (fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | colors.badgeTextColor();

        float textScale = dims.uiScale * 1.35f;
        int scaledTextWidth = (int) (textRenderer.width(title) * textScale);
        int titleX = badgeX + (badgeWidth - scaledTextWidth) / 2;
        int titleY = badgeY + (badgeHeight - (int) (textRenderer.lineHeight * textScale)) / 2;

        context.pose().pushPose();
        context.pose().translate(titleX, titleY, 0);
        context.pose().scale(textScale, textScale, 1.0f);
        context.drawString(textRenderer, title, 0, 0, titleColor, false);
        context.pose().popPose();
    }

    public static void renderPagesHolder(GuiGraphics context, Font textRenderer, SummaryDimensions dims,
                                         int currentPage, int totalPages, float fadeAlpha) {
        if (totalPages <= 1) return;

        Component pageInfo = Component.translatable("midnightthoughts.summary.page", currentPage + 1, totalPages);

        int pagesHolderHeight = dims.s(45);
        int pagesHolderWidth = (int) (pagesHolderHeight * (SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH / (float) SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT));

        int contentPaddingBottom = dims.s(37);

        int pagesHolderX = dims.panelX + (dims.panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = dims.panelY + dims.panelHeight - contentPaddingBottom - pagesHolderHeight - dims.s(5);

        RenderHelper.blitTexture(context, SummaryConstants.getPagesHolderTexture(),
                pagesHolderX, pagesHolderY, pagesHolderWidth, pagesHolderHeight, fadeAlpha);

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int pageTextAlpha = (int) (fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | colors.pagesHolderTextColor();

        float textScale = dims.uiScale * 1.12f;
        int scaledTextWidth = (int) (textRenderer.width(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int) (textRenderer.lineHeight * textScale)) / 2;

        context.pose().pushPose();
        context.pose().translate(pageTextX, pageTextY, 0);
        context.pose().scale(textScale, textScale, 1.0f);
        context.drawString(textRenderer, pageInfo, 0, 0, pageTextColor, true);
        context.pose().popPose();
    }
}