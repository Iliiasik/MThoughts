package mt.client.ui.engine;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        slice(context, texture, x, y, cap, cap, 0, 0, border, border, texW, texH);
        slice(context, texture, rightX, y, cap, cap, rightU, 0, border, border, texW, texH);
        slice(context, texture, x, bottomY, cap, cap, 0, bottomV, border, border, texW, texH);
        slice(context, texture, rightX, bottomY, cap, cap, rightU, bottomV, border, border, texW, texH);

        if (innerW > 0 && srcInnerW > 0) {
            slice(context, texture, x + cap, y, innerW, cap, border, 0, srcInnerW, border, texW, texH);
            slice(context, texture, x + cap, bottomY, innerW, cap, border, bottomV, srcInnerW, border, texW, texH);
        }

        if (innerH > 0 && srcInnerH > 0) {
            slice(context, texture, x, y + cap, cap, innerH, 0, border, border, srcInnerH, texW, texH);
            slice(context, texture, rightX, y + cap, cap, innerH, rightU, border, border, srcInnerH, texW, texH);
        }

        if (innerW > 0 && innerH > 0 && srcInnerW > 0 && srcInnerH > 0) {
            slice(context, texture, x + cap, y + cap, innerW, innerH,
                    border, border, srcInnerW, srcInnerH, texW, texH);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static void slice(GuiGraphics context, ResourceLocation texture,
                              int x, int y, int width, int height,
                              int u, int v, int uWidth, int vHeight, int texW, int texH) {
        context.blit(texture, x, y, width, height, (float) u, (float) v, uWidth, vHeight, texW, texH);
    }
}
