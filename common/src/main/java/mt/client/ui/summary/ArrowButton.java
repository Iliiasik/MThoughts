package mt.client.ui.summary;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ArrowButton extends Button {

    private static final float HOVER_TINT = 1.25f;
    private static final float IDLE_TINT = 0.85f;

    private final boolean next;

    public ArrowButton(int x, int y, int size, boolean next, OnPress onPress) {
        super(x, y, size, size, Component.translatable(
                next ? "midnightthoughts.summary.next" : "midnightthoughts.summary.previous"),
                onPress, DEFAULT_NARRATION);
        this.next = next;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        float tint = isMouseOver(mouseX, mouseY) ? HOVER_TINT : IDLE_TINT;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(tint, tint, tint, this.alpha);
        context.blit(SummaryConstants.getButtonTexture(next), getX(), getY(), getWidth(), getHeight(),
                0.0f, 0.0f,
                SummaryConstants.BUTTON_TEXTURE_SIZE, SummaryConstants.BUTTON_TEXTURE_SIZE,
                SummaryConstants.BUTTON_TEXTURE_SIZE, SummaryConstants.BUTTON_TEXTURE_SIZE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}
