package mt.client.ui.summary;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class RenderHelper {

    public record ScaledBlit(int renderW, int renderH, int renderY) {}

    public static ScaledBlit computeScaledBlit(int y, int width, int height, int texW, int texH) {
        float scale = Math.min((float) height / texH, (float) width / texW);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int renderY = y + (height - renderH) / 2;
        return new ScaledBlit(renderW, renderH, renderY);
    }

    public static void blitTexture(DrawContext context, Identifier texture,
                                   int x, int y, int width, int height, float alpha) {
        int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(1.0f, 1.0f);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0f, 0.0f, width, height, width, height, width, height, color);
        context.getMatrices().popMatrix();
    }

    public static void blitTextureSimple(DrawContext context, Identifier texture,
                                         int x, int y, int width, int height,
                                         int texW, int texH, float alpha) {
        int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f, width, height, texW, texH, texW, texH, color);
    }
}