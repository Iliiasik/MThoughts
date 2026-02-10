package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class BackgroundRenderer {

    public static void render(GuiGraphics context, int width, int height) {
        RenderSystem.setShaderTexture(0, SummaryConstants.BACKGROUND_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float screenAspect = (float)width / (float)height;
        int bgX, bgY, bgWidth, bgHeight;

        if (screenAspect > SummaryConstants.BACKGROUND_ASPECT_RATIO) {
            bgWidth = width;
            bgHeight = (int)(width / SummaryConstants.BACKGROUND_ASPECT_RATIO);
            bgX = 0;
            bgY = (height - bgHeight) / 2;
        } else {
            bgHeight = height;
            bgWidth = (int)(height * SummaryConstants.BACKGROUND_ASPECT_RATIO);
            bgX = (width - bgWidth) / 2;
            bgY = 0;
        }

        Matrix4f matrix = context.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, bgX, bgY + bgHeight, 0).uv(0, 1).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, bgX + bgWidth, bgY + bgHeight, 0).uv(1, 1).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, bgX + bgWidth, bgY, 0).uv(1, 0).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, bgX, bgY, 0).uv(0, 0).color(255, 255, 255, 255).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
    }
}

