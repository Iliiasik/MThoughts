package mt.client.ui.summary;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
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
    protected void extractContents(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float tint = isMouseOver(mouseX, mouseY) ? HOVER_TINT : IDLE_TINT;
        int color = ARGB.colorFromFloat(this.alpha, Math.min(tint, 1.0f), Math.min(tint, 1.0f), Math.min(tint, 1.0f));
        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getButtonTexture(next), getX(), getY(), 0.0f, 0.0f,
                getWidth(), getHeight(),
                SummaryConstants.BUTTON_TEXTURE_SIZE, SummaryConstants.BUTTON_TEXTURE_SIZE,
                SummaryConstants.BUTTON_TEXTURE_SIZE, SummaryConstants.BUTTON_TEXTURE_SIZE, color);
    }
}
