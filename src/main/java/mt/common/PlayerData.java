package mt.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class PlayerData {

    private PlayerData() {}

    public static CompoundTag of(Player player) {
        return ((PersistentDataHolder) player).midnightthoughts$getPersistentData();
    }
}
