package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;

public class ThemeSwitchButton extends AbstractWidget {
    private static final Identifier GEAR_ICON = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/summary/gear.png");

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Component.empty());
    }

    @Override
    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) return;

        boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        float alpha = isHovered ? 1.0f : 0.7f;
        int color = (int) (alpha * 255) << 24 | 0xFFFFFF;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GEAR_ICON,
                this.getX(), this.getY(), 0.0f, 0.0f,
                this.width, this.height, this.width, this.height, this.width, this.height, color);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean consumed) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) {
            return false;
        }
        if (!consumed && isMouseOver(event.x(), event.y())) {
            ClientConfig.getInstance().cycleTheme();
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) return false;
        return mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }

    public Component getTooltipText() {
        String currentTheme = ClientConfig.getInstance().getEffectiveTheme();
        return Component.translatable("midnightthoughts.theme.current",
                Component.translatable("midnightthoughts.theme." + currentTheme));
    }
}