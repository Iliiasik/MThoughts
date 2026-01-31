package mt.client.mixin;

import mt.client.MidnightThoughtsClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Inject(
            method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        MidnightThoughtsClient instance = MidnightThoughtsClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null) {
            if (instance.getOverlayRenderer().shouldHideCrosshair()) {
                ci.cancel();
            }
        }
    }
}
