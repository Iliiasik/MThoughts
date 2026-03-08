package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class StyledButton extends Button {

    private static final int TEX_W = 200;
    private static final int TEX_H = 32;

    public StyledButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        boolean hovered = isHovered();

        Identifier texture = Identifier.fromNamespaceAndPath(
                MidnightThoughtsClient.MOD_ID,
                "textures/gui/" + theme + (hovered ? "/button_hover.png" : "/button.png")
        );

        float scale = Math.min((float) getWidth() / TEX_W, (float) getHeight() / TEX_H);
        int renderW = (int)(TEX_W * scale);
        int renderH = (int)(TEX_H * scale);
        int renderX = getX() + (getWidth() - renderW) / 2;
        int renderY = getY() + (getHeight() - renderH) / 2;

        context.pose().pushMatrix();
        context.pose().translate(renderX, renderY);
        context.pose().scale(scale, scale);
        context.blit(RenderPipelines.GUI_TEXTURED, texture,
                0, 0, 0.0f, 0.0f,
                TEX_W, TEX_H,
                TEX_W, TEX_H, 0xFFFFFFFF);
        context.pose().popMatrix();

        Font font = Minecraft.getInstance().font;
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);
        int textColor = hovered ? (0xFF000000 | colors.buttonTextHoverColor()) : (0xFF000000 | colors.buttonTextColor());
        int textX = getX() + (getWidth() - font.width(getMessage())) / 2;
        int textY = getY() + (getHeight() - font.lineHeight) / 2;
        context.drawString(font, getMessage(), textX, textY, textColor, true);
    }
}