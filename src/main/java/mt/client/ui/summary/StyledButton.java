package mt.client.ui.summary;

import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class StyledButton extends Button {

    public StyledButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Font textRenderer = Minecraft.getInstance().font;

        boolean hovered = isMouseOver(mouseX, mouseY);

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int baseColor = colors.buttonColor();
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        int bgColor = hovered
                ? (0xDD << 24) | (Math.min(255, r + 32) << 16) | (Math.min(255, g + 32) << 8) | Math.min(255, b + 32)
                : (0xCC << 24) | baseColor;

        int borderColor = hovered
                ? (0xFF << 24) | (Math.min(255, r + 64) << 16) | (Math.min(255, g + 64) << 8) | Math.min(255, b + 64)
                : (0xFF << 24) | (Math.min(255, r + 32) << 16) | (Math.min(255, g + 32) << 8) | Math.min(255, b + 32);

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
        context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderColor);
        context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
        context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderColor);
        context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

        int textColor = hovered ? (0xFF << 24) | colors.buttonTextHoverColor() : (0xFF << 24) | colors.buttonTextColor();
        int textX = getX() + (getWidth() - textRenderer.width(getMessage())) / 2;
        int textY = getY() + (getHeight() - 8) / 2;
        context.drawString(textRenderer, getMessage(), textX, textY, textColor, true);
    }
}

