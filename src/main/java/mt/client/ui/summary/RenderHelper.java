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
        int color = ARGB.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale((float) width, (float) height);
        context.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0f, 0.0f, 1, 1, 1, 1, color);
        context.pose().popMatrix();
    }

    public static void blitTextureSimple(GuiGraphics context, Identifier texture,
                                         int x, int y, int width, int height,
                                         int texW, int texH, float alpha) {
        int color = ARGB.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale((float) width / texW, (float) height / texH);
        context.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH, color);
        context.pose().popMatrix();
    }
}