package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import mt.server.config.MidnightThoughtsConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ThemeSwitchButton extends AbstractWidget {
    private static final ResourceLocation GEAR_ICON = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/gear.png");

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Component.empty());
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) return;

        boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        float alpha = isHovered ? 1.0f : 0.7f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        context.blit(GEAR_ICON, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onClick(double mouseX, double mouseY) {
        ClientConfig.getInstance().cycleTheme();
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
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