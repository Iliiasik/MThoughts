package mt.client.mixin;

import mt.client.MidnightThoughtsClient;
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
    private void renderWellRestedHud(GuiGraphics guiGraphics, CallbackInfo ci) {
        MidnightThoughtsClient.renderWellRestedHud(guiGraphics);
    }

    @Inject(method = "renderSleepOverlay", at = @At("TAIL"))
    private void renderModSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MidnightThoughtsClient.renderSleepOverlay(guiGraphics);
    }

    @Inject(method = "renderChat", at = @At("HEAD"))
    private void renderSleepingPlayersHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MidnightThoughtsClient.renderSleepingPlayersHud(guiGraphics);
    }
}
