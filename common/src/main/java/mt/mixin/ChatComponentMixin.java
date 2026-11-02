package mt.mixin;

import mt.client.MTClient;
import mt.client.render.SleepOverlayRenderer;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderChat(CallbackInfo ci) {
        SleepOverlayRenderer overlay = MTClient.overlay();
        if (overlay != null && overlay.shouldHideChat()) {
            ci.cancel();
        }
    }
}