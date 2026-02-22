package mt.client.ui.summary;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

public class FrameRenderer {

    public static void render(DrawContext context, SummaryDimensions dims, float fadeAlpha) {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        Identifier frameTexture = SummaryConstants.getFrameTexture();
        int color = ((int)(fadeAlpha * 255) << 24) | 0xFFFFFF;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, frameTexture, dims.panelX, dims.panelY,
            0.0f, 0.0f, dims.panelWidth, dims.panelHeight,
            SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
            SummaryConstants.FRAME_TEXTURE_WIDTH, SummaryConstants.FRAME_TEXTURE_HEIGHT,
            color);
    }

    public static void renderBadge(DrawContext context, TextRenderer textRenderer, SummaryDimensions dims, float fadeAlpha) {
        Text title = Text.translatable("midnightthoughts.summary.title");
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        Identifier badgeTexture = SummaryConstants.getBadgeTexture();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        float badgeScale = dims.panelWidth / 1000.0f;
        int badgeWidth = (int)(240 * badgeScale);
        int badgeHeight = (int)(80 * badgeScale);

        int contentPaddingSides = (int)(SummaryConstants.FRAME_CONTENT_PADDING_SIDES * (dims.panelWidth / 1000.0f));
        int badgeOffsetX = (int)(15 * badgeScale);
        int badgeOffsetY = (int)(25 * badgeScale);
        int badgeX = dims.panelX + contentPaddingSides + badgeOffsetX;
        int badgeY = dims.panelY - badgeHeight / 2 + badgeOffsetY;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, badgeTexture, badgeX, badgeY,
            0.0f, 0.0f, badgeWidth, badgeHeight,
            SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
            SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
            0xFFFFFFFF);

        int titleAlpha = (int)(fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | colors.badgeTextColor();

        float textScale = badgeScale * 1.8f;
        int scaledTextWidth = (int)(textRenderer.getWidth(title) * textScale);
        int titleX = badgeX + (badgeWidth - scaledTextWidth) / 2;
        int titleY = badgeY + (badgeHeight - (int)(textRenderer.fontHeight * textScale)) / 2;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(titleX, titleY);
        matrices.scale(textScale, textScale);
        context.drawText(textRenderer, title, 0, 0, titleColor, false);
        matrices.popMatrix();
    }

    public static void renderPagesHolder(DrawContext context, TextRenderer textRenderer, SummaryDimensions dims,
                                         int currentPage, int totalPages, float fadeAlpha) {
        if (totalPages <= 1) {
            return;
        }

        Text pageInfo = Text.translatable("midnightthoughts.summary.page", currentPage + 1, totalPages);
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        Identifier pagesHolderTexture = SummaryConstants.getPagesHolderTexture();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        float pagesHolderScale = dims.panelWidth / 1000.0f;
        int pagesHolderHeight = (int)(60 * pagesHolderScale);
        int pagesHolderWidth = (int)(pagesHolderHeight * (SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT));

        float contentPaddingTop = SummaryConstants.FRAME_CONTENT_PADDING_TOP * (dims.panelHeight / 640.0f);
        float contentPaddingBottom = SummaryConstants.FRAME_CONTENT_PADDING_BOTTOM * (dims.panelHeight / 640.0f);

        int listAreaHeight = dims.playersPerPage * dims.playerRowHeight;
        int availableSpace = dims.panelHeight - (int)contentPaddingTop - (int)contentPaddingBottom - listAreaHeight;

        int pagesHolderX = dims.panelX + (dims.panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = dims.panelY + (int)contentPaddingTop + listAreaHeight + (availableSpace - pagesHolderHeight) / 2;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, pagesHolderTexture, pagesHolderX, pagesHolderY,
            0.0f, 0.0f, pagesHolderWidth, pagesHolderHeight,
            SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
            SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH, SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT,
            0xFFFFFFFF);

        int pageTextAlpha = (int)(fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | colors.pagesHolderTextColor();

        float textScale = pagesHolderScale * 1.5f;
        int scaledTextWidth = (int)(textRenderer.getWidth(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int)(textRenderer.fontHeight * textScale)) / 2;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(pageTextX, pageTextY);
        matrices.scale(textScale, textScale);
        context.drawText(textRenderer, pageInfo, 0, 0, pageTextColor, false);
        matrices.popMatrix();
    }
}
