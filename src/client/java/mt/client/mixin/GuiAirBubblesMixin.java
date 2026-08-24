package mt.client.mixin;

import mt.client.ui.WellRestedHud;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public abstract class GuiAirBubblesMixin {

    @ModifyArg(
            method = "renderPlayerHealth",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V"
            ),
            index = 3
    )
    private int shiftAirBubbles(int top) {
        if (WellRestedHud.isActive() && WellRestedHud.isBarPosition()) {
            return top - (WellRestedHud.BAR_GUI_H + 3);
        }
        return top;
    }
}
