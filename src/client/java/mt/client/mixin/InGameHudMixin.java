package mt.client.mixin;

import mt.client.MidnightThoughtsClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
            ),
            method = "extractRenderState",
            cancellable = true
    )
    private void onBeforeExtractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MidnightThoughtsClient instance = MidnightThoughtsClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null) {
            if (instance.getOverlayRenderer().shouldHideCrosshair()) {
                ci.cancel();
            }
        }
    }
}