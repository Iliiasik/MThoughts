package mt.client.ui.summary;

import mt.client.ui.engine.RenderHelper;
import mt.client.ui.engine.RenderUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public final class TooltipRenderer {

    private static final int LINE_STEP = 10;
    private static final int INK_HEIGHT = 9;
    private static final int INNER_PAD = 2;
    private static final int CURSOR_OFFSET = 4;

    private TooltipRenderer() {}

    public static void render(GuiGraphics context, Font font, int mouseX, int mouseY,
                              List<HoverArea> areas, SummaryLayout layout,
                              int screenWidth, int screenHeight, float fadeAlpha) {
        for (HoverArea area : areas) {
            if (area.contains(mouseX, mouseY)) {
                draw(context, font, mouseX, mouseY, area.lines(),
                        layout, screenWidth, screenHeight, fadeAlpha);
                return;
            }
        }
    }

    private static void draw(GuiGraphics context, Font font, int mouseX, int mouseY, List<String> lines,
                             SummaryLayout layout, int screenWidth, int screenHeight, float fadeAlpha) {
        if (lines.isEmpty()) return;

        int pixelScale = layout.scale;
        int inset = (SummaryConstants.TOOLTIP_BORDER + INNER_PAD) * pixelScale;
        int step = LINE_STEP * pixelScale;

        int maxText = Math.max(1, Math.round((screenWidth - 16 * pixelScale - inset * 2) / (float) pixelScale));
        List<String> fitted = new ArrayList<>(lines.size());
        int textWidth = 0;
        for (String line : lines) {
            String clipped = RenderUtils.truncateWithEllipsis(font, line, maxText);
            fitted.add(clipped);
            textWidth = Math.max(textWidth, RenderUtils.scaledWidth(font, clipped, pixelScale));
        }

        int width = textWidth + inset * 2;
        int height = (fitted.size() - 1) * step + INK_HEIGHT * pixelScale + inset * 2;

        int offset = CURSOR_OFFSET * pixelScale;
        int x = mouseX + offset;
        int y = mouseY - height - 2 * pixelScale;
        if (x + width > screenWidth) x = mouseX - width - offset;
        if (x < 0) x = 0;
        if (y < 0) y = mouseY + 8 * pixelScale;
        if (y + height > screenHeight) y = Math.max(0, screenHeight - height);

        context.pose().pushPose();
        context.pose().translate(0, 0, 400);

        RenderHelper.blitNineSlice(context, SummaryConstants.getTooltipTexture(), x, y, width, height,
                SummaryConstants.TOOLTIP_TEXTURE_WIDTH, SummaryConstants.TOOLTIP_TEXTURE_HEIGHT,
                SummaryConstants.TOOLTIP_BORDER, pixelScale, fadeAlpha);

        ThemeColors.TextStyle style = ThemeColors.current().tooltip();
        int color = style.argb(fadeAlpha);
        for (int i = 0; i < fitted.size(); i++) {
            RenderUtils.renderScaledText(context, font, fitted.get(i),
                    x + inset, y + inset + i * step, color, pixelScale, style.shadow());
        }

        context.pose().popPose();
    }
}
