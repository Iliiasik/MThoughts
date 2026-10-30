package mt.client.ui.summary;

import mt.client.ui.engine.RenderHelper;
import mt.client.ui.engine.RenderUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class FrameRenderer {

    private FrameRenderer() {}

    public static void render(GuiGraphics context, SummaryLayout layout, boolean mvp, float fadeAlpha) {
        RenderHelper.blitTexture(context, SummaryConstants.getFrameTexture(),
                layout.frameX, layout.frameY, layout.frameWidth(), layout.frameHeight(), fadeAlpha);

        RenderHelper.blitTexture(context, SummaryConstants.getInfoTexture(mvp),
                layout.infoX(), layout.infoY(),
                layout.s(SummaryConstants.INFO_TEXTURE_WIDTH),
                layout.s(SummaryConstants.INFO_TEXTURE_HEIGHT), fadeAlpha);
    }

    public static void renderBadge(GuiGraphics context, Font font, SummaryLayout layout,
                                   Component headline, float fadeAlpha) {
        float textScale = layout.textScale();
        int inset = layout.s(SummaryConstants.BADGE_BORDER + SummaryConstants.BADGE_INSET);
        int maxText = layout.contentWidth() - inset * 2;

        String text = RenderUtils.truncateWithEllipsis(font, headline.getString(),
                Math.max(1, Math.round(maxText / textScale)));
        int textWidth = RenderUtils.scaledWidth(font, text, textScale);

        int height = layout.s(SummaryConstants.BADGE_TEXTURE_HEIGHT);
        int width = Math.min(layout.contentWidth(), textWidth + inset * 2);
        int x = RenderUtils.centeredX(layout.contentX(), layout.contentWidth(), width);
        int y = layout.contentY();

        RenderHelper.blitNineSlice(context, SummaryConstants.getBadgeTexture(), x, y, width, height,
                SummaryConstants.BADGE_TEXTURE_WIDTH, SummaryConstants.BADGE_TEXTURE_HEIGHT,
                SummaryConstants.BADGE_BORDER, layout.scale, fadeAlpha);

        ThemeColors.TextStyle style = ThemeColors.current().badge();
        RenderUtils.renderScaledText(context, font, text,
                RenderUtils.centeredX(x, width, textWidth),
                RenderUtils.centeredTextY(y, height, textScale, style.shadow()),
                style.argb(fadeAlpha), textScale, style.shadow());
    }
}
