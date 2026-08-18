package mt.mixin;

import mt.common.PersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerPersistentDataMixin implements PersistentDataHolder {

    @Unique
    private static final String MIDNIGHTTHOUGHTS_KEY = "midnightthoughts";

    @Unique
    private CompoundTag midnightthoughts$persistentData = new CompoundTag();

    @Override
    public CompoundTag midnightthoughts$getPersistentData() {
        return midnightthoughts$persistentData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void midnightthoughts$save(CompoundTag tag, CallbackInfo ci) {
        if (!midnightthoughts$persistentData.isEmpty()) {
            tag.put(MIDNIGHTTHOUGHTS_KEY, midnightthoughts$persistentData.copy());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void midnightthoughts$load(CompoundTag tag, CallbackInfo ci) {
        midnightthoughts$persistentData = tag.contains(MIDNIGHTTHOUGHTS_KEY)
                ? tag.getCompound(MIDNIGHTTHOUGHTS_KEY).copy()
                : new CompoundTag();
    }
}
