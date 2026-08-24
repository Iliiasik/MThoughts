package mt.client.ui.summary;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RenderUtils {

    public static void renderScaledText(GuiGraphics context, Font textRenderer, String text,
                                        int x, int y, int color, float scale, boolean shadow) {
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.drawString(textRenderer, text, 0, 0, color, shadow);
        context.pose().popMatrix();
    }

    public static void renderCenteredScaledText(GuiGraphics context, Font textRenderer, Component text,
                                                int areaX, int areaY, int areaWidth, int areaHeight,
                                                int color, float scale, boolean shadow) {
        int scaledTextWidth = (int) (textRenderer.width(text) * scale);
        int x = areaX + (areaWidth - scaledTextWidth) / 2;
        int y = areaY + (areaHeight - (int) (textRenderer.lineHeight * scale)) / 2;
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.drawString(textRenderer, text, 0, 0, color, shadow);
        context.pose().popMatrix();
    }

    public static String truncateWithEllipsis(Font font, String text, int maxWidth) {
        if (text == null || text.isEmpty()) return text;
        if (font.width(text) <= maxWidth) return text;

        String ellipsis = "...";
        int budget = maxWidth - font.width(ellipsis);
        if (budget <= 0) return ellipsis;

        int lo = 0;
        int hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (font.width(text.substring(0, mid)) <= budget) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return text.substring(0, lo) + ellipsis;
    }
}