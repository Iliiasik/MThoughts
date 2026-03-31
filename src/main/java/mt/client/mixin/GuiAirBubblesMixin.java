package mt.client.mixin;

import mt.client.ui.WellRestedHud;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiAirBubblesMixin {

    @Inject(method = "renderAirLevel", at = @At("HEAD"))
    private void onRenderAirLevelHead(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isPrimaryPosition()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, -(WellRestedHud.BAR_GUI_H + 3), 0);
        }
    }

    @Inject(method = "renderAirLevel", at = @At("TAIL"))
    private void onRenderAirLevelTail(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isPrimaryPosition()) {
            guiGraphics.pose().popPose();
        }
    }
}