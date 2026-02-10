package mt.client.ui.summary;

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
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Font textRenderer = Minecraft.getInstance().font;

        boolean hovered = isMouseOver(mouseX, mouseY);

        int bgColor = hovered ? 0xDD6a5a8a : 0xCC4a3a6a;
        int borderColor = hovered ? 0xFFaa8acc : 0xFF8a6a9a;

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
        context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderColor);
        context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
        context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderColor);
        context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

        int textColor = hovered ? 0xFFeeddff : 0xFFddccee;
        int textX = getX() + (getWidth() - textRenderer.width(getMessage())) / 2;
        int textY = getY() + (getHeight() - 8) / 2;
        textRenderer.drawInBatch(getMessage().getString(), textX, textY, textColor, true, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }
}

