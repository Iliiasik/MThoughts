package mt.mixin;

import mt.client.MTClient;
import mt.client.render.SleepOverlayRenderer;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiOverlayMessageMixin {
    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetOverlayMessage(CallbackInfo ci) {
        SleepOverlayRenderer overlay = MTClient.overlay();
        if (overlay != null && overlay.shouldHideHudMessages()) {
            ci.cancel();
        }
    }
}
