package mt.client.ui.engine;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class RenderHelper {

    public static void blitTexture(GuiGraphicsExtractor context, Identifier texture,
                                   int x, int y, int width, int height, float alpha) {
        if (width <= 0 || height <= 0) return;
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f, width, height, width, height, white(alpha));
    }

    public static int white(float alpha) {
        return (RenderUtils.alphaByte(alpha) << 24) | 0xFFFFFF;
    }

    public static void blitNineSlice(GuiGraphicsExtractor context, Identifier texture,
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

        int color = white(alpha);

        slice(context, texture, x, y, cap, cap, 0, 0, border, border, texW, texH, color);
        slice(context, texture, rightX, y, cap, cap, rightU, 0, border, border, texW, texH, color);
        slice(context, texture, x, bottomY, cap, cap, 0, bottomV, border, border, texW, texH, color);
        slice(context, texture, rightX, bottomY, cap, cap, rightU, bottomV, border, border, texW, texH, color);

        if (innerW > 0 && srcInnerW > 0) {
            slice(context, texture, x + cap, y, innerW, cap, border, 0, srcInnerW, border, texW, texH, color);
            slice(context, texture, x + cap, bottomY, innerW, cap, border, bottomV, srcInnerW, border, texW, texH, color);
        }

        if (innerH > 0 && srcInnerH > 0) {
            slice(context, texture, x, y + cap, cap, innerH, 0, border, border, srcInnerH, texW, texH, color);
            slice(context, texture, rightX, y + cap, cap, innerH, rightU, border, border, srcInnerH, texW, texH, color);
        }

        if (innerW > 0 && innerH > 0 && srcInnerW > 0 && srcInnerH > 0) {
            slice(context, texture, x + cap, y + cap, innerW, innerH,
                    border, border, srcInnerW, srcInnerH, texW, texH, color);
        }

    }

    private static void slice(GuiGraphicsExtractor context, Identifier texture,
                              int x, int y, int width, int height,
                              int u, int v, int uWidth, int vHeight, int texW, int texH, int color) {
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, uWidth, vHeight, texW, texH, color);
    }
}
