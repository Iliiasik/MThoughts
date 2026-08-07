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
