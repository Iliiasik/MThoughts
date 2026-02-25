package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public class FrameRenderer {

    public static void render(GuiGraphics context, SummaryDimensions dims, float fadeAlpha) {
        RenderSystem.setShaderTexture(0, SummaryConstants.getFrameTexture());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = context.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        int alpha = (int)(fadeAlpha * 255);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, dims.panelX, dims.panelY + dims.panelHeight, 0).uv(0, 1).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, dims.panelX + dims.panelWidth, dims.panelY + dims.panelHeight, 0).uv(1, 1).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, dims.panelX + dims.panelWidth, dims.panelY, 0).uv(1, 0).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, dims.panelX, dims.panelY, 0).uv(0, 0).color(255, 255, 255, alpha).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
    }

    public static void renderBadge(GuiGraphics context, Font textRenderer, SummaryDimensions dims, float fadeAlpha) {
        Component title = Component.translatable("midnightthoughts.summary.title");

        float badgeScale = dims.panelWidth / 1000.0f;
        int badgeWidth = (int)(240 * badgeScale);
        int badgeHeight = (int)(80 * badgeScale);

        int contentPaddingSides = (int)(SummaryConstants.FRAME_CONTENT_PADDING_SIDES * (dims.panelWidth / 1000.0f));
        int badgeOffsetX = (int)(15 * badgeScale);
        int badgeOffsetY = (int)(25 * badgeScale);
        int badgeX = dims.panelX + contentPaddingSides + badgeOffsetX;
        int badgeY = dims.panelY - badgeHeight / 2 + badgeOffsetY;

        RenderSystem.setShaderTexture(0, SummaryConstants.getBadgeTexture());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = context.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        int alpha = (int)(fadeAlpha * 255);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, badgeX, badgeY + badgeHeight, 0).uv(0, 1).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, badgeX + badgeWidth, badgeY + badgeHeight, 0).uv(1, 1).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, badgeX + badgeWidth, badgeY, 0).uv(1, 0).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, badgeX, badgeY, 0).uv(0, 0).color(255, 255, 255, alpha).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int titleAlpha = (int)(fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | colors.badgeTextColor();

        float textScale = badgeScale * 1.8f;
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
        if (totalPages <= 1) {
            return;
        }

        Component pageInfo = Component.translatable("midnightthoughts.summary.page", currentPage + 1, totalPages);

        float pagesHolderScale = dims.panelWidth / 1000.0f;
        int pagesHolderHeight = (int)(60 * pagesHolderScale);
        int pagesHolderWidth = (int)(pagesHolderHeight * (SummaryConstants.PAGES_HOLDER_TEXTURE_WIDTH / (float)SummaryConstants.PAGES_HOLDER_TEXTURE_HEIGHT));

        float contentPaddingTop = SummaryConstants.FRAME_CONTENT_PADDING_TOP * (dims.panelHeight / 640.0f);
        float contentPaddingBottom = SummaryConstants.FRAME_CONTENT_PADDING_BOTTOM * (dims.panelHeight / 640.0f);

        int listAreaHeight = dims.playersPerPage * dims.playerRowHeight;
        int availableSpace = dims.panelHeight - (int)contentPaddingTop - (int)contentPaddingBottom - listAreaHeight;

        int pagesHolderX = dims.panelX + (dims.panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = dims.panelY + (int)contentPaddingTop + listAreaHeight + (availableSpace - pagesHolderHeight) / 2;

        RenderSystem.setShaderTexture(0, SummaryConstants.getPagesHolderTexture());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = context.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        int alpha = (int)(fadeAlpha * 255);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, pagesHolderX, pagesHolderY + pagesHolderHeight, 0).uv(0, 1).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, pagesHolderX + pagesHolderWidth, pagesHolderY + pagesHolderHeight, 0).uv(1, 1).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, pagesHolderX + pagesHolderWidth, pagesHolderY, 0).uv(1, 0).color(255, 255, 255, alpha).endVertex();
        buffer.vertex(matrix, pagesHolderX, pagesHolderY, 0).uv(0, 0).color(255, 255, 255, alpha).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int pageTextAlpha = (int)(fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | colors.pagesHolderTextColor();

        float textScale = pagesHolderScale * 1.5f;
        int scaledTextWidth = (int)(textRenderer.width(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int)(textRenderer.lineHeight * textScale)) / 2;

        context.pose().pushPose();
        context.pose().translate(pageTextX, pageTextY, 0);
        context.pose().scale(textScale, textScale, 1.0f);
        context.drawString(textRenderer, pageInfo, 0, 0, pageTextColor, false);
        context.pose().popPose();
    }
}

