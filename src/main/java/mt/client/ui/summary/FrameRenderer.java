package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.server.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FrameRenderer {

    public static void render(GuiGraphics context, SummaryDimensions dims, float fadeAlpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        context.pose().pushPose();
        context.pose().translate(dims.panelX, dims.panelY, 0);
        context.pose().scale(dims.panelWidth / (float)SummaryConstants.FRAME_TEXTURE_WIDTH,
                dims.panelHeight / (float)SummaryConstants.FRAME_TEXTURE_HEIGHT, 1.0f);
        context.blit(SummaryConstants.getFrameTexture(),
                0, 0, 0, 0,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT);
        context.pose().popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void renderBadge(GuiGraphics context, Font textRenderer, SummaryDimensions dims, float fadeAlpha) {
        Component title = Component.translatable("midnightthoughts.summary.title");

        int badgeWidth = dims.s(180);
        int badgeHeight = dims.s(60);

        int contentPaddingSides = dims.s(60);
        int badgeX = dims.panelX + contentPaddingSides + dims.s(11);
        int badgeY = dims.panelY - badgeHeight / 2 + dims.s(19);

        int alpha = (int)(fadeAlpha * 255);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        context.pose().pushPose();
        context.pose().translate(badgeX, badgeY, 0);
        context.pose().scale(badgeWidth / (float)SummaryConstants.BADGE_TEXTURE_WIDTH,
                badgeHeight / (float)SummaryConstants.BADGE_TEXTURE_HEIGHT, 1.0f);
        context.blit(SummaryConstants.getBadgeTexture(),
                0, 0, 0, 0,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT);
        context.pose().popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int titleColor = (alpha << 24) | colors.badgeTextColor();

        float textScale = dims.uiScale * 1.35f;
        int scaledTextWidth = (int)(textRenderer.width(title) * textScale);
        int titleX = badgeX + (badgeWidth - scaledTextWidth) / 2;
        int titleY = badgeY + (badgeHeight - (int)(textRenderer.lineHeight * textScale)) / 2;

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
        int pagesHolderWidth = (int)(pagesHolderHeight * (SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT));

        int contentPaddingBottom = dims.s(37);

        int pagesHolderX = dims.panelX + (dims.panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = dims.panelY + dims.panelHeight - contentPaddingBottom - pagesHolderHeight - dims.s(5);

        int alpha = (int)(fadeAlpha * 255);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        context.pose().pushPose();
        context.pose().translate(pagesHolderX, pagesHolderY, 0);
        context.pose().scale(pagesHolderWidth / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH,
                pagesHolderHeight / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT, 1.0f);
        context.blit(SummaryConstants.getPagesHolderTexture(),
                0, 0, 0, 0,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT);
        context.pose().popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int pageTextColor = (alpha << 24) | colors.pagesHolderTextColor();

        float textScale = dims.uiScale * 1.12f;
        int scaledTextWidth = (int)(textRenderer.width(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int)(textRenderer.lineHeight * textScale)) / 2;

        context.pose().pushPose();
        context.pose().translate(pageTextX, pageTextY, 0);
        context.pose().scale(textScale, textScale, 1.0f);
        context.drawString(textRenderer, pageInfo, 0, 0, pageTextColor, true);
        context.pose().popPose();
    }
}