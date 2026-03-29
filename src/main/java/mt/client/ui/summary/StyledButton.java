package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.server.config.MidnightThoughtsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StyledButton extends Button {

    private static final int TEX_W = 200;
    private static final int TEX_H = 32;

    public StyledButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        boolean hovered = isMouseOver(mouseX, mouseY);

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                MidnightThoughtsClient.MOD_ID,
                "textures/gui/" + theme + (hovered ? "/button_hover.png" : "/button.png")
        );

        float scale = Math.min((float) getWidth() / TEX_W, (float) getHeight() / TEX_H);
        int renderW = (int)(TEX_W * scale);
        int renderH = (int)(TEX_H * scale);
        int renderX = getX() + (getWidth() - renderW) / 2;
        int renderY = getY() + (getHeight() - renderH) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        context.blit(texture, renderX, renderY, renderW, renderH, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);
        RenderSystem.disableBlend();

        Font font = Minecraft.getInstance().font;
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);
        int textColor = hovered ? (0xFF000000 | colors.buttonTextHoverColor()) : (0xFF000000 | colors.buttonTextColor());

        float textScale = Math.min(scale * 1.5f, (float) getHeight() / font.lineHeight * 0.6f);
        int scaledTextW = (int)(font.width(getMessage()) * textScale);
        int textX = getX() + (getWidth() - scaledTextW) / 2;
        int textY = getY() + (getHeight() - (int)(font.lineHeight * textScale)) / 2;

        context.pose().pushPose();
        context.pose().translate(textX, textY, 0);
        context.pose().scale(textScale, textScale, 1.0f);
        context.drawString(font, getMessage(), 0, 0, textColor, true);
        context.pose().popPose();
    }
}