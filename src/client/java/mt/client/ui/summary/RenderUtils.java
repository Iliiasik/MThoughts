package mt.client.ui.summary;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    public static void renderScaledText(DrawContext context, TextRenderer textRenderer, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        if (scale < 1.0f) {
            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.drawText(textRenderer, text, 0, 0, color, shadow);
            context.getMatrices().pop();
        } else {
            context.drawText(textRenderer, text, x, y, color, shadow);
        }
    }
}

