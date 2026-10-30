package mt.fabric.mixin;

import mt.client.MTClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiRenderMixin {

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"))
    private void midnightthoughts$renderWellRestedHud(GuiGraphics guiGraphics, CallbackInfo ci) {
        MTClient.renderWellRestedHud(guiGraphics);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;)V"
            )
    )
    private void midnightthoughts$renderSleepOverlay(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MTClient.renderSleepOverlay(guiGraphics);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;III)V"
            )
    )
    private void midnightthoughts$renderSleepingPlayersHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MTClient.renderSleepingPlayersHud(guiGraphics);
    }
}
