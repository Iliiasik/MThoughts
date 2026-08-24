package mt.mixin;

import mt.common.PersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private void midnightthoughts$save(ValueOutput valueOutput, CallbackInfo ci) {
        if (!midnightthoughts$persistentData.isEmpty()) {
            valueOutput.store(MIDNIGHTTHOUGHTS_KEY, CompoundTag.CODEC, midnightthoughts$persistentData.copy());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void midnightthoughts$load(ValueInput valueInput, CallbackInfo ci) {
        midnightthoughts$persistentData = valueInput.read(MIDNIGHTTHOUGHTS_KEY, CompoundTag.CODEC)
                .map(CompoundTag::copy)
                .orElseGet(CompoundTag::new);
    }
}
