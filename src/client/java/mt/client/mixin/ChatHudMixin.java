package mt.client.mixin;

import mt.client.MidnightThoughtsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderChat(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        MidnightThoughtsClient instance = MidnightThoughtsClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null) {
            if (instance.getOverlayRenderer().shouldHideChat()) {
                ci.cancel();
            }
        }
    }
}