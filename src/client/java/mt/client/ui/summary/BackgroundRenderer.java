package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;

public class BackgroundRenderer {

    public static void render(DrawContext context, int width, int height) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SummaryConstants.BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

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

        context.drawTexture(SummaryConstants.BACKGROUND_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);
    }
}

