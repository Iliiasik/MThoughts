package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StyledButton extends ButtonWidget {

    private static final int TEX_W = 100;
    private static final int TEX_H = 32;

    public StyledButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        boolean hovered = isMouseOver(mouseX, mouseY);

        Identifier texture = Identifier.of(
                MidnightThoughtsClient.MOD_ID,
                "textures/gui/" + theme + (hovered ? "/button_hover.png" : "/button.png")
        );

        float scale = Math.min((float) getWidth() / TEX_W, (float) getHeight() / TEX_H);
        int renderW = (int) (TEX_W * scale);
        int renderH = (int) (TEX_H * scale);
        int renderX = getX() + (getWidth() - renderW) / 2;
        int renderY = getY() + (getHeight() - renderH) / 2;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(renderX, renderY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTexture(texture, 0, 0, 0.0f, 0.0f, TEX_W, TEX_H, TEX_W, TEX_H);
        context.getMatrices().pop();
        RenderSystem.disableBlend();

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);
        int textColor = hovered ? (0xFF000000 | colors.buttonTextHoverColor()) : (0xFF000000 | colors.buttonTextColor());

        float textScale = Math.min(scale * 1.5f, (float) getHeight() / textRenderer.fontHeight * 0.6f);
        int scaledTextW = (int) (textRenderer.getWidth(getMessage()) * textScale);
        int textX = getX() + (getWidth() - scaledTextW) / 2;
        int textY = getY() + (getHeight() - (int) (textRenderer.fontHeight * textScale)) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(textX, textY, 0);
        context.getMatrices().scale(textScale, textScale, 1.0f);
        context.drawText(textRenderer, getMessage(), 0, 0, textColor, true);
        context.getMatrices().pop();
    }
}