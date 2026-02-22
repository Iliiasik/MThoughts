package mt.client.ui.summary;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class StyledButton extends ClickableWidget {

    public interface PressAction {
        void onPress(StyledButton button);
    }

    private final PressAction onPress;

    public StyledButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        if (this.active && this.visible && this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        boolean hovered = this.isHovered();

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
        int textX = getX() + (getWidth() - textRenderer.getWidth(getMessage())) / 2;
        int textY = getY() + (getHeight() - 8) / 2;
        context.drawText(textRenderer, getMessage(), textX, textY, textColor, true);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}

