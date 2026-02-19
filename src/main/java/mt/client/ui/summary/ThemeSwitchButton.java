package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ThemeSwitchButton extends AbstractWidget {
    private static final ResourceLocation GEAR_ICON = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/gear.png");
    private boolean isHovered = false;

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Component.empty());
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                        mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        float alpha = isHovered ? 1.0f : 0.7f;

        RenderSystem.setShaderTexture(0, GEAR_ICON);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = context.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        int alphaInt = (int)(alpha * 255);
        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.width;
        int y2 = y1 + this.height;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, x1, y2, 0).uv(0, 1).color(255, 255, 255, alphaInt).endVertex();
        buffer.vertex(matrix, x2, y2, 0).uv(1, 1).color(255, 255, 255, alphaInt).endVertex();
        buffer.vertex(matrix, x2, y1, 0).uv(1, 0).color(255, 255, 255, alphaInt).endVertex();
        buffer.vertex(matrix, x1, y1, 0).uv(0, 0).color(255, 255, 255, alphaInt).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ClientConfig.getInstance().cycleTheme();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        String currentTheme = ClientConfig.getInstance().getEffectiveTheme();
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getX() && mouseY >= this.getY() &&
               mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }

    public Component getTooltipText() {
        String currentTheme = ClientConfig.getInstance().getEffectiveTheme();
        return Component.translatable("midnightthoughts.theme.current",
            Component.translatable("midnightthoughts.theme." + currentTheme));
    }
}
