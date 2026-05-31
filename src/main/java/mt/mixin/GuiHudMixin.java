package mt.mixin;

import mt.client.MidnightThoughtsClient;
import mt.client.ui.SleepingPlayersHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderPre(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
        if (inst == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        inst.getOverlayRenderer().renderOverlayOnly(guiGraphics, w, h);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderPost(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
        if (inst == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        inst.getOverlayRenderer().renderContentOnly(guiGraphics, w, h);
        SleepingPlayersHud.render(guiGraphics, w, h);
    }
}