package mt.mixin;

import mt.client.ui.WellRestedHud;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiAirBubblesMixin {

    @Inject(method = "renderAirBubbles", at = @At("HEAD"))
    private void onRenderAirBubblesHead(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight, int right, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isPrimaryPosition()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(0, -(WellRestedHud.BAR_GUI_H + 3));
        }
    }

    @Inject(method = "renderAirBubbles", at = @At("TAIL"))
    private void onRenderAirBubblesTail(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight, int right, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isPrimaryPosition()) {
            guiGraphics.pose().popMatrix();
        }
    }
}