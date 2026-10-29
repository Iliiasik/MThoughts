package mt.fabric.mixin;

import mt.client.MTClient;
import net.minecraft.client.DeltaTracker;
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

    @Inject(method = "renderSleepOverlay", at = @At("TAIL"))
    private void midnightthoughts$renderSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MTClient.renderSleepOverlay(guiGraphics);
    }

    @Inject(method = "renderChat", at = @At("HEAD"))
    private void midnightthoughts$renderSleepingPlayersHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MTClient.renderSleepingPlayersHud(guiGraphics);
    }
}
