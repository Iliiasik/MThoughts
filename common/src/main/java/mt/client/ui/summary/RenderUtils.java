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
