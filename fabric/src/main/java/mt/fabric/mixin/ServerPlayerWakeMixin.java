package mt.fabric.mixin;

import mt.server.MTServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerWakeMixin {

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void midnightthoughts$onWake(boolean wakeImmediately, boolean updateLevel, CallbackInfo ci) {
        if (wakeImmediately || !updateLevel) return;
        MTServer.onWokeUp((ServerPlayer) (Object) this);
    }
}
