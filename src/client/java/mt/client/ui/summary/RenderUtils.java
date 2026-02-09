package mt.client.ui.summary;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

public class RenderUtils {

    public static void renderScaledText(DrawContext context, TextRenderer textRenderer, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        if (scale < 1.0f) {
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            context.drawText(textRenderer, text, 0, 0, color, shadow);
            matrices.popMatrix();
        } else {
            context.drawText(textRenderer, text, x, y, color, shadow);
        }
    }
}

