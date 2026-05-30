package mt.client.ui.summary;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class FrameRenderer {

    public static void render(DrawContext context, SummaryDimensions dims, float fadeAlpha) {
        RenderHelper.blitTexture(context, SummaryConstants.getFrameTexture(),
                dims.panelX, dims.panelY, dims.panelWidth, dims.panelHeight, fadeAlpha);
    }

    public static void renderBadge(DrawContext context, TextRenderer textRenderer, SummaryDimensions dims, float fadeAlpha) {
        Text title = Text.translatable("midnightthoughts.summary.title");

        int badgeWidth = dims.s(180);
        int badgeHeight = dims.s(60);

        int contentPaddingSides = dims.s(60);
        int badgeX = dims.panelX + contentPaddingSides + dims.s(11);
        int badgeY = dims.panelY - badgeHeight / 2 + dims.s(19);

        RenderHelper.blitTexture(context, SummaryConstants.getBadgeTexture(),
                badgeX, badgeY, badgeWidth, badgeHeight, fadeAlpha);

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int titleAlpha = (int) (fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | colors.badgeTextColor();

        float textScale = dims.uiScale * 1.35f;
        RenderUtils.renderCenteredScaledText(context, textRenderer, title,
                badgeX, badgeY, badgeWidth, badgeHeight, titleColor, textScale, false);
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

        RenderHelper.blitTexture(context, SummaryConstants.getPagesHolderTexture(),
                pagesHolderX, pagesHolderY, pagesHolderWidth, pagesHolderHeight, fadeAlpha);

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int pageTextAlpha = (int) (fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | colors.pagesHolderTextColor();

        float textScale = dims.uiScale * 1.12f;
        RenderUtils.renderCenteredScaledText(context, textRenderer, pageInfo,
                pagesHolderX, pagesHolderY, pagesHolderWidth, pagesHolderHeight, pageTextColor, textScale, true);
    }
}