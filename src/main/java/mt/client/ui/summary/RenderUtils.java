package mt.client.ui.summary;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class RenderUtils {

    public static void renderScaledText(GuiGraphics context, Font textRenderer, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        context.pose().pushPose();
        context.pose().translate(x, y, 0);
        context.pose().scale(scale, scale, 1.0f);
        context.drawString(textRenderer, text, 0, 0, color, shadow);
        context.pose().popPose();
    }

    public static String truncateWithEllipsis(Font font, String text, int maxWidth) {
        String ellipsis = "...";
        int ellipsisW = font.width(ellipsis);
        if (font.width(text) <= maxWidth) return text;
        while (!text.isEmpty() && font.width(text) + ellipsisW > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }
}