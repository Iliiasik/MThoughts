package mt.client.ui.summary;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class RenderUtils {

    public static void renderScaledText(GuiGraphics context, Font textRenderer, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.drawString(textRenderer, text, 0, 0, color, shadow);
        context.pose().popMatrix();
    }
}
