package mt.client.ui.summary;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class RenderUtils {

    public static void renderScaledText(DrawContext context, TextRenderer textRenderer, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        context.drawText(textRenderer, text, 0, 0, color, shadow);
        context.getMatrices().popMatrix();
    }

    public static void renderCenteredScaledText(DrawContext context, TextRenderer textRenderer, Text text,
                                                int areaX, int areaY, int areaWidth, int areaHeight,
                                                int color, float scale, boolean shadow) {
        int scaledTextWidth = (int) (textRenderer.getWidth(text) * scale);
        int x = areaX + (areaWidth - scaledTextWidth) / 2;
        int y = areaY + (areaHeight - (int) (textRenderer.fontHeight * scale)) / 2;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        context.drawText(textRenderer, text, 0, 0, color, shadow);
        context.getMatrices().popMatrix();
    }

    public static String truncateWithEllipsis(TextRenderer font, String text, int maxWidth) {
        String ellipsis = "...";
        int ellipsisW = font.getWidth(ellipsis);
        if (font.getWidth(text) <= maxWidth) return text;
        while (!text.isEmpty() && font.getWidth(text) + ellipsisW > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }
}