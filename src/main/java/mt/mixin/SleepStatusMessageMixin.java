package mt.mixin;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class SleepStatusMessageMixin {
    @Inject(method = "announceSleepStatus", at = @At("HEAD"), cancellable = true)
    private void midnightthoughts$suppressSleepStatus(CallbackInfo ci) {
        if (MidnightThoughtsConfig.getInstance().getServer().suppressVanillaSleepMessages) {
            ci.cancel();
        }
    }
}
