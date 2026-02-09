package mt.client.ui.summary;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;

public class BackgroundRenderer {

    public static void render(DrawContext context, int width, int height) {
        float screenAspect = (float)width / (float)height;
        int bgX, bgY, bgWidth, bgHeight;

        if (screenAspect > SummaryConstants.BACKGROUND_ASPECT_RATIO) {
            bgWidth = width;
            bgHeight = (int)(width / SummaryConstants.BACKGROUND_ASPECT_RATIO);
            bgX = 0;
            bgY = (height - bgHeight) / 2;
        } else {
            bgHeight = height;
            bgWidth = (int)(height * SummaryConstants.BACKGROUND_ASPECT_RATIO);
            bgX = (width - bgWidth) / 2;
            bgY = 0;
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, SummaryConstants.BACKGROUND_TEXTURE,
            bgX, bgY,
            0, 0,
            bgWidth, bgHeight,
            bgWidth, bgHeight,
            bgWidth, bgHeight,
            0xFFFFFFFF);
    }
}

