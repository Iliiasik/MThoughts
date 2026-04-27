package mt.client.ui.summary;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class RenderUtils {

    public static void renderScaledText(GuiGraphicsExtractor graphics, Font font, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, 0, 0, color, shadow);
        graphics.pose().popMatrix();
    }
}