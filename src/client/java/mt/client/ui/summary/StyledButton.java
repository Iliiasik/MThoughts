package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class StyledButton extends AbstractButton {
    private static final int TEX_W = 200;
    private static final int TEX_H = 32;

    public interface PressAction {
        void onPress(StyledButton button);
    }

    private final PressAction onPress;

    public StyledButton(int x, int y, int width, int height, Component message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public void onPress(@NotNull InputWithModifiers input) {
        if (this.active && this.visible && this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        boolean hovered = isHovered();

        Identifier texture = Identifier.fromNamespaceAndPath(
                MidnightThoughtsClient.MOD_ID,
                "textures/gui/" + theme + (hovered ? "/button_hover.png" : "/button.png")
        );

        float scale = Math.min((float) getWidth() / TEX_W, (float) getHeight() / TEX_H);
        int renderW = (int) (TEX_W * scale);
        int renderH = (int) (TEX_H * scale);
        int renderX = getX() + (getWidth() - renderW) / 2;
        int renderY = getY() + (getHeight() - renderH) / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(renderX, renderY);
        graphics.pose().scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                0, 0, 0.0f, 0.0f,
                TEX_W, TEX_H,
                TEX_W, TEX_H, TEX_W, TEX_H, 0xFFFFFFFF);
        graphics.pose().popMatrix();

        Font font = Minecraft.getInstance().font;
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);
        int textColor = hovered ? (0xFF000000 | colors.buttonTextHoverColor()) : (0xFF000000 | colors.buttonTextColor());

        float textScale = Math.min(scale * 1.5f, (float) getHeight() / font.lineHeight * 0.6f);
        int scaledTextW = (int) (font.width(getMessage()) * textScale);
        int textX = getX() + (getWidth() - scaledTextW) / 2;
        int textY = getY() + (getHeight() - (int) (font.lineHeight * textScale)) / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(textX, textY);
        graphics.pose().scale(textScale, textScale);
        graphics.text(font, getMessage(), 0, 0, textColor, false);
        graphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }
}