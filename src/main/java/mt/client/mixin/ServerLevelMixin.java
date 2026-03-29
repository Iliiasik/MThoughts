package mt.client.mixin;

import mt.server.config.MidnightThoughtsConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;wakeUpAllPlayers()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onAfterWakeUpAllPlayers(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!MidnightThoughtsConfig.getInstance().getServer().resetPhantomTimerForNonSleepers) return;
        ServerLevel level = (ServerLevel) (Object) this;
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator()) {
                player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            }
        }
    }
}