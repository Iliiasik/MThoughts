package mt.fabric.mixin;

import mt.client.ui.WellRestedHud;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Gui.class)
public abstract class GuiAirBubblesMixin {

    @ModifyArg(
            method = "renderPlayerHealth",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;getMaxAirSupply()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
            ),
            index = 2
    )
    private int shiftAirBubbles(int y) {
        return y - WellRestedHud.airBubbleShift();
    }
}
