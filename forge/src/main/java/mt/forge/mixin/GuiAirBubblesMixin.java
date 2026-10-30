package mt.forge.mixin;

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
    private void midnightthoughts$shiftAirBubbles(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, -WellRestedHud.airBubbleShift(), 0);
    }

    @Inject(method = "renderAir", at = @At("TAIL"))
    private void midnightthoughts$restoreAirBubbles(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        guiGraphics.pose().popPose();
    }
}
