package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ThemeSwitchButton extends ButtonWidget {
    private static final Identifier GEAR_ICON = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/gear.png");

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Text.empty(), button -> ClientConfig.getInstance().cycleTheme(), DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                        mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        float alpha = isHovered ? 1.0f : 0.7f;

        RenderSystem.setShaderTexture(0, GEAR_ICON);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();

        context.drawTexture(GEAR_ICON,
            getX(), getY(), this.width, this.height,
            0.0f, 0.0f, 23, 23, 23, 23);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getX() && mouseY >= this.getY() &&
               mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            String currentTheme = ClientConfig.getInstance().getEffectiveTheme();
            Text themeDisplayName = Text.translatable("midnightthoughts.theme." + currentTheme);
            Text tooltip = Text.translatable("midnightthoughts.theme.current", themeDisplayName);
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, mouseX, mouseY);
        }
    }
}
