package mt.client.ui.summary;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class RenderUtils {

    public static void renderScaledText(GuiGraphicsExtractor graphics, Font font, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, 0, 0, color, shadow);
        graphics.pose().popMatrix();
    }

    public static void renderCenteredScaledText(GuiGraphicsExtractor graphics, Font font, Component text,
                                                int areaX, int areaY, int areaWidth, int areaHeight,
                                                int color, float scale, boolean shadow) {
        int scaledTextWidth = (int) (font.width(text) * scale);
        int x = areaX + (areaWidth - scaledTextWidth) / 2;
        int y = areaY + (areaHeight - (int) (font.lineHeight * scale)) / 2;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(font, text.getString(), 0, 0, color, shadow);
        graphics.pose().popMatrix();
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