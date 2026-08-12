package mt.mixin;

import mt.client.ui.WellRestedHud;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiAirBubblesMixin {

    @Inject(method = "extractAirBubbles", at = @At("HEAD"))
    private void onExtractAirBubblesHead(GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir, int xRight, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isBarPosition()) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, -(WellRestedHud.BAR_GUI_H + 3));
        }
    }

    @Inject(method = "extractAirBubbles", at = @At("TAIL"))
    private void onExtractAirBubblesTail(GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir, int xRight, CallbackInfo ci) {
        if (WellRestedHud.isActive() && WellRestedHud.isBarPosition()) {
            graphics.pose().popMatrix();
        }
    }
}
