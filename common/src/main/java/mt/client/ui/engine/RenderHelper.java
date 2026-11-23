package mt.client.ui.engine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderHelper {

    public static void blitTexture(GuiGraphics context, ResourceLocation texture,
                                   int x, int y, int width, int height, float alpha) {
        if (width <= 0 || height <= 0) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        context.blit(texture, x, y, width, height, 0.0f, 0.0f, width, height, width, height);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void fillQuad(GuiGraphics context, int x1, int y1, int x2, int y2,
                                int leftTop, int leftBottom, int rightBottom, int rightTop) {
        Matrix4f pose = context.pose().last().pose();
        VertexConsumer buffer = context.bufferSource().getBuffer(RenderType.gui());
        buffer.vertex(pose, x1, y1, 0).color(leftTop).endVertex();
        buffer.vertex(pose, x1, y2, 0).color(leftBottom).endVertex();
        buffer.vertex(pose, x2, y2, 0).color(rightBottom).endVertex();
        buffer.vertex(pose, x2, y1, 0).color(rightTop).endVertex();
    }

    public static void blitTextureLeft(GuiGraphics context, ResourceLocation texture,
                                       int x, int y, int width, int height, float visibleWidth, float alpha) {
        if (width <= 0 || height <= 0 || visibleWidth <= 0.0f) return;

        float visible = Math.min(visibleWidth, width);
        int whole = (int) visible;
        float partial = visible - whole;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (whole > 0) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            context.blit(texture, x, y, whole, height, 0.0f, 0.0f, whole, height, width, height);
        }
        if (partial > 0.0f && whole < width) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha * partial);
            context.blit(texture, x + whole, y, 1, height, whole, 0.0f, 1, height, width, height);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void blitNineSlice(GuiGraphics context, ResourceLocation texture,
                                     int x, int y, int width, int height,
                                     int texW, int texH, int border, int pixelScale, float alpha) {
        if (width <= 0 || height <= 0) return;

        int cap = Math.min(border * Math.max(1, pixelScale), Math.min(width, height) / 2);
        if (cap <= 0 || border <= 0) {
            blitTexture(context, texture, x, y, width, height, alpha);
            return;
        }

        int innerW = width - cap * 2;
        int innerH = height - cap * 2;
        int srcInnerW = texW - border * 2;
        int srcInnerH = texH - border * 2;

        int rightX = x + width - cap;
        int bottomY = y + height - cap;
        int rightU = texW - border;
        int bottomV = texH - border;

        Matrix4f pose = context.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        slice(buffer, pose, x, y, cap, cap, 0, 0, border, border, texW, texH);
        slice(buffer, pose, rightX, y, cap, cap, rightU, 0, border, border, texW, texH);
        slice(buffer, pose, x, bottomY, cap, cap, 0, bottomV, border, border, texW, texH);
        slice(buffer, pose, rightX, bottomY, cap, cap, rightU, bottomV, border, border, texW, texH);

        if (innerW > 0 && srcInnerW > 0) {
            slice(buffer, pose, x + cap, y, innerW, cap, border, 0, srcInnerW, border, texW, texH);
            slice(buffer, pose, x + cap, bottomY, innerW, cap, border, bottomV, srcInnerW, border, texW, texH);
        }

        if (innerH > 0 && srcInnerH > 0) {
            slice(buffer, pose, x, y + cap, cap, innerH, 0, border, border, srcInnerH, texW, texH);
            slice(buffer, pose, rightX, y + cap, cap, innerH, rightU, border, border, srcInnerH, texW, texH);
        }

        if (innerW > 0 && innerH > 0 && srcInnerW > 0 && srcInnerH > 0) {
            slice(buffer, pose, x + cap, y + cap, innerW, innerH,
                    border, border, srcInnerW, srcInnerH, texW, texH);
        }

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static void slice(BufferBuilder buffer, Matrix4f pose,
                              int x, int y, int width, int height,
                              int u, int v, int uWidth, int vHeight, int texW, int texH) {
        float u0 = u / (float) texW;
        float u1 = (u + uWidth) / (float) texW;
        float v0 = v / (float) texH;
        float v1 = (v + vHeight) / (float) texH;
        buffer.vertex(pose, x, y, 0).uv(u0, v0).endVertex();
        buffer.vertex(pose, x, y + height, 0).uv(u0, v1).endVertex();
        buffer.vertex(pose, x + width, y + height, 0).uv(u1, v1).endVertex();
        buffer.vertex(pose, x + width, y, 0).uv(u1, v0).endVertex();
    }
}
