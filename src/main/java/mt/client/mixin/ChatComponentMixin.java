package mt.client.mixin;

import mt.client.MidnightThoughtsClient;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderChat(CallbackInfo ci) {
        MidnightThoughtsClient instance = MidnightThoughtsClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null) {
            if (instance.getOverlayRenderer().shouldHideChat()) {
                ci.cancel();
            }
        }
    }
}