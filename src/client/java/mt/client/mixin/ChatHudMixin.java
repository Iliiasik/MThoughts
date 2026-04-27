package mt.client.mixin;

import mt.client.MidnightThoughtsClient;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ChatComponent.DisplayMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @Inject(
            at = @At("HEAD"),
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
            cancellable = true
    )
    private void onRenderChat(GuiGraphicsExtractor graphics, Font font, int ticks, int mouseX, int mouseY, DisplayMode displayMode, boolean changeCursorOnInsertions, CallbackInfo ci) {
        MidnightThoughtsClient instance = MidnightThoughtsClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null) {
            if (instance.getOverlayRenderer().shouldHideChat()) {
                ci.cancel();
            }
        }
    }
}