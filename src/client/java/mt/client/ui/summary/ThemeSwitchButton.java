package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gl.RenderPipelines;

public class ThemeSwitchButton extends ClickableWidget {
    private static final Identifier GEAR_ICON = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/gear.png");

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Text.empty());
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        ClientConfig.getInstance().cycleTheme();
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                        mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        float alpha = isHovered ? 1.0f : 0.7f;
        int color = (int)(alpha * 255) << 24 | 0xFFFFFF;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, GEAR_ICON, getX(), getY(), 0.0f, 0.0f, this.width, this.height, 23, 23, 23, 23, color);
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

    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
