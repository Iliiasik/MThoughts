package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
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

    public ThemeSwitchButton(int x, int y, int size) {
        super(x, y, size, size, Component.empty());
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        float alpha = isHovered ? 1.0f : 0.7f;

        RenderSystem.setShaderTexture(0, GEAR_ICON);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = context.pose().last().pose();

        int alphaInt = (int)(alpha * 255);
        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.width;
        int y2 = y1 + this.height;

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(matrix, x1, y2, 0).setUv(0, 1).setColor(255, 255, 255, alphaInt);
        buffer.addVertex(matrix, x2, y2, 0).setUv(1, 1).setColor(255, 255, 255, alphaInt);
        buffer.addVertex(matrix, x2, y1, 0).setUv(1, 0).setColor(255, 255, 255, alphaInt);
        buffer.addVertex(matrix, x1, y1, 0).setUv(0, 0).setColor(255, 255, 255, alphaInt);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ClientConfig.getInstance().cycleTheme();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
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