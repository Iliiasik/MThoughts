package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ThemeSwitchButton extends ButtonWidget {
    private static final Identifier GEAR_ICON = new Identifier(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/gear.png");

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Text.empty(), button -> {
            if (!MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) {
                ClientConfig.getInstance().cycleTheme();
            }
        }, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) return;

        boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        float alpha = isHovered ? 1.0f : 0.7f;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        context.drawTexture(GEAR_ICON, getX(), getY(), this.width, this.height,
                0.0f, 0.0f, this.width, this.height, this.width, this.height);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) return false;
        return mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }

    public Text getTooltipText() {
        String currentTheme = ClientConfig.getInstance().getEffectiveTheme();
        return Text.translatable("midnightthoughts.theme.current",
                Text.translatable("midnightthoughts.theme." + currentTheme));
    }
}