package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class FrameRenderer {

    public static void render(DrawContext context, SummaryDimensions dims, float fadeAlpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(dims.panelX, dims.panelY, 0);
        context.getMatrices().scale(dims.panelWidth / (float) SummaryConstants.FRAME_TEXTURE_WIDTH,
                dims.panelHeight / (float) SummaryConstants.FRAME_TEXTURE_HEIGHT, 1.0f);
        context.drawTexture(SummaryConstants.getFrameTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
                SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT);
        context.getMatrices().pop();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void renderBadge(DrawContext context, TextRenderer textRenderer, SummaryDimensions dims, float fadeAlpha) {
        Text title = Text.translatable("midnightthoughts.summary.title");

        int badgeWidth = dims.s(180);
        int badgeHeight = dims.s(60);

        int contentPaddingSides = dims.s(60);
        int badgeX = dims.panelX + contentPaddingSides + dims.s(11);
        int badgeY = dims.panelY - badgeHeight / 2 + dims.s(19);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(badgeX, badgeY, 0);
        context.getMatrices().scale(badgeWidth / (float) SummaryConstants.BADGE_TEXTURE_WIDTH,
                badgeHeight / (float) SummaryConstants.BADGE_TEXTURE_HEIGHT, 1.0f);
        context.drawTexture(SummaryConstants.getBadgeTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT);
        context.getMatrices().pop();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int titleAlpha = (int) (fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | colors.badgeTextColor();

        float textScale = dims.uiScale * 1.35f;
        int scaledTextWidth = (int) (textRenderer.getWidth(title) * textScale);
        int titleX = badgeX + (badgeWidth - scaledTextWidth) / 2;
        int titleY = badgeY + (badgeHeight - (int) (textRenderer.fontHeight * textScale)) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(titleX, titleY, 0);
        context.getMatrices().scale(textScale, textScale, 1.0f);
        context.drawText(textRenderer, title, 0, 0, titleColor, false);
        context.getMatrices().pop();
    }

    public static void renderPagesHolder(DrawContext context, TextRenderer textRenderer, SummaryDimensions dims,
                                         int currentPage, int totalPages, float fadeAlpha) {
        if (totalPages <= 1) return;

        Text pageInfo = Text.translatable("midnightthoughts.summary.page", currentPage + 1, totalPages);

        int pagesHolderHeight = dims.s(45);
        int pagesHolderWidth = (int) (pagesHolderHeight * (SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH / (float) SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT));

        int contentPaddingBottom = dims.s(37);

        int pagesHolderX = dims.panelX + (dims.panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = dims.panelY + dims.panelHeight - contentPaddingBottom - pagesHolderHeight - dims.s(5);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(pagesHolderX, pagesHolderY, 0);
        context.getMatrices().scale(pagesHolderWidth / (float) SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH,
                pagesHolderHeight / (float) SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT, 1.0f);
        context.drawTexture(SummaryConstants.getPagesHolderTexture(),
                0, 0, 0.0f, 0.0f,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
                SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT);
        context.getMatrices().pop();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int pageTextAlpha = (int) (fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | colors.pagesHolderTextColor();

        float textScale = dims.uiScale * 1.12f;
        int scaledTextWidth = (int) (textRenderer.getWidth(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int) (textRenderer.fontHeight * textScale)) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(pageTextX, pageTextY, 0);
        context.getMatrices().scale(textScale, textScale, 1.0f);
        context.drawText(textRenderer, pageInfo, 0, 0, pageTextColor, false);
        context.getMatrices().pop();
    }
}