package mt.mixin;

import mt.client.MTClient;
import mt.client.render.SleepOverlayRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiCrosshairMixin {
    @Inject(
            method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        SleepOverlayRenderer overlay = MTClient.overlay();
        if (overlay != null && overlay.shouldHideCrosshair()) {
            ci.cancel();
        }
    }
}