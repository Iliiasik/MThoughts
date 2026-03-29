package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mt.server.config.MidnightThoughtsConfig;
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

        int badgeWidth = dims.s(180);
        int badgeHeight = dims.s(60);

        int contentPaddingSides = dims.s(60);
        int badgeX = dims.panelX + contentPaddingSides + dims.s(11);
        int badgeY = dims.panelY - badgeHeight / 2 + dims.s(19);

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

        float textScale = dims.uiScale * 1.12f;
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