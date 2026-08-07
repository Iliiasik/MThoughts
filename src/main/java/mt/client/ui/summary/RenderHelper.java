package mt.client.ui.summary;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class RenderHelper {
    public record ScaledBlit(int renderW, int renderH, int renderY) {}

    public static ScaledBlit computeScaledBlit(int y, int width, int height, int texW, int texH) {
        float scale = Math.min((float) height / texH, (float) width / texW);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int renderY = y + (height - renderH) / 2;
        return new ScaledBlit(renderW, renderH, renderY);
    }

    public static void blitTexture(GuiGraphics context, Identifier texture,
                                   int x, int y, int width, int height, float alpha) {
        if (width <= 0 || height <= 0) return;

        int color = ARGB.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f);
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f,
                width, height, width, height, color);
    }
}