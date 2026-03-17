package mt.mixin;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerLevelMixin {
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;wakeSleepingPlayers()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onAfterWakeUpAllPlayers(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!MidnightThoughtsConfig.getInstance().getServer().resetPhantomTimerForNonSleepers) return;
        ServerWorld world = (ServerWorld) (Object) this;
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!player.isSpectator()) {
                player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
            }
        }
    }
}