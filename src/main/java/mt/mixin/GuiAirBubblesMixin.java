package mt.mixin;

import mt.client.ui.WellRestedHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeGui.class, remap = false)
public abstract class GuiAirBubblesMixin {

    @Inject(method = "renderAir", at = @At("HEAD"))
    private void onRenderAirHead(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isBarPosition()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, -(WellRestedHud.BAR_GUI_H + 3), 0);
        }
    }

    @Inject(method = "renderAir", at = @At("TAIL"))
    private void onRenderAirTail(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isBarPosition()) {
            guiGraphics.pose().popPose();
        }
    }
}
