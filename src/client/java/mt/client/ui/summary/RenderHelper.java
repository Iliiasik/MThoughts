package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
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
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale((float) width / width, (float) height / height, 1.0f);
        context.drawTexture(texture, 0, 0, 0.0f, 0.0f, width, height, width, height);
        context.getMatrices().pop();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void blitTextureSimple(DrawContext context, Identifier texture,
                                         int x, int y, int width, int height,
                                         int texW, int texH, float alpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        context.drawTexture(texture, x, y, 0.0f, 0.0f, width, height, texW, texH);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}