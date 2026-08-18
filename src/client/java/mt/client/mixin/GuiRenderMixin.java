package mt.client.mixin;

import mt.client.MidnightThoughtsClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiRenderMixin {

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V"
            )
    )
    private void renderWellRestedHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MidnightThoughtsClient.renderWellRestedHud(guiGraphics);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;)V"
            )
    )
    private void renderSleepOverlay(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MidnightThoughtsClient.renderSleepOverlay(guiGraphics);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;III)V"
            )
    )
    private void renderSleepingPlayersHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MidnightThoughtsClient.renderSleepingPlayersHud(guiGraphics);
    }
}
