package mt.client.ui.summary;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class RenderHelper {
    public record ScaledBlit(int renderW, int renderH, int renderY) {}

    public static ScaledBlit computeScaledBlit(int y, int width, int height, int texW, int texH) {
        float scale = Math.min((float) height / texH, (float) width / texW);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int renderY = y + (height - renderH) / 2;
        return new ScaledBlit(renderW, renderH, renderY);
    }

    public static void blitTexture(GuiGraphicsExtractor graphics, Identifier texture,
                                   int x, int y, int width, int height, float alpha) {
        int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(1.0f, 1.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0f, 0.0f, width, height, width, height, width, height, color);
        graphics.pose().popMatrix();
    }
}