package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ThemeSwitchButton extends ClickableWidget {
    private static final Identifier GEAR_ICON = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/gear.png");

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Text.empty());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) return;

        boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        float alpha = isHovered ? 1.0f : 0.7f;
        int color = (int) (alpha * 255) << 24 | 0xFFFFFF;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GEAR_ICON,
                this.getX(), this.getY(), 0.0f, 0.0f,
                this.width, this.height, this.width, this.height, this.width, this.height, color);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (MidnightThoughtsConfig.getInstance().isHideThemeSwitchButton()) {
            return false;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        ClientConfig.getInstance().cycleTheme();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
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