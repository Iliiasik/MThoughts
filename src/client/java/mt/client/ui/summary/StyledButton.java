package mt.client.ui.summary;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class StyledButton extends ButtonWidget {

    public StyledButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        boolean hovered = isMouseOver(mouseX, mouseY);

        int bgColor = hovered ? 0xDD6a5a8a : 0xCC4a3a6a;
        int borderColor = hovered ? 0xFFaa8acc : 0xFF8a6a9a;

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
        context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderColor);
        context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
        context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderColor);
        context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

        int textColor = hovered ? 0xFFeeddff : 0xFFddccee;
        int textX = getX() + (getWidth() - textRenderer.getWidth(getMessage())) / 2;
        int textY = getY() + (getHeight() - 8) / 2;
        context.drawText(textRenderer, getMessage(), textX, textY, textColor, true);
    }
}

