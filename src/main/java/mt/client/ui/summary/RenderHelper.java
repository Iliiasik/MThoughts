package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderHelper {

    public record ScaledBlit(int renderW, int renderH, int renderY) {}

    public static ScaledBlit computeScaledBlit(int y, int width, int height, int texW, int texH) {
        float scale = Math.min((float) height / texH, (float) width / texW);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int renderY = y + (height - renderH) / 2;
        return new ScaledBlit(renderW, renderH, renderY);
    }

    public static void blitTexture(GuiGraphics context, ResourceLocation texture,
                                   int x, int y, int width, int height, float alpha) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = context.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        int a = (int) (alpha * 255);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, x,         y + height, 0).uv(0, 1).color(255, 255, 255, a).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).uv(1, 1).color(255, 255, 255, a).endVertex();
        buffer.vertex(matrix, x + width, y,          0).uv(1, 0).color(255, 255, 255, a).endVertex();
        buffer.vertex(matrix, x,         y,          0).uv(0, 0).color(255, 255, 255, a).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
    }

    public static void blitTextureSimple(GuiGraphics context, ResourceLocation texture,
                                         int x, int y, int width, int height,
                                         int texW, int texH, float alpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        context.blit(texture, x, y, width, height, 0, 0, texW, texH, texW, texH);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}