package mt.mixin;

import mt.client.MTClient;
import mt.client.render.SleepOverlayRenderer;
import mt.client.ui.SleepingPlayersHud;
import mt.client.ui.WellRestedHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiHudMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onExtractRenderStatePre(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        SleepOverlayRenderer overlay = MTClient.overlay();
        if (overlay == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        overlay.renderOverlayOnly(graphics,
                mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderStatePost(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        SleepOverlayRenderer overlay = MTClient.overlay();
        if (overlay == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        overlay.renderContentOnly(graphics, w, h);
        SleepingPlayersHud.render(graphics, w, h);
        WellRestedHud.render(graphics, w, h);
    }
}
