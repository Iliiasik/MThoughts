package mt.client.ui.summary;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    public static void renderScaledText(DrawContext context, TextRenderer textRenderer, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        context.drawText(textRenderer, text, 0, 0, color, shadow);
        context.getMatrices().popMatrix();
    }
}