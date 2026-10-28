package mt.client.ui.engine;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RenderUtils {

    private static final float GLYPH_HEIGHT = 8.0f;
    private static final float SHADOWED_INK_HEIGHT = 9.0f;

    public static int scaledWidth(Font font, String text, float scale) {
        return Math.round(font.width(text) * scale);
    }

    public static int scaledWidth(Font font, Component text, float scale) {
        return Math.round(font.width(text) * scale);
    }

    public static int centeredX(int boxX, int boxWidth, int contentWidth) {
        return boxX + Math.round((boxWidth - contentWidth) / 2.0f);
    }

    public static int centeredTextY(int boxY, int boxHeight, float scale, boolean shadow) {
        float ink = (shadow ? SHADOWED_INK_HEIGHT : GLYPH_HEIGHT) * scale;
        return boxY + Math.round((boxHeight - ink) / 2.0f);
    }

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
