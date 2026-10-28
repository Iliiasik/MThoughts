package mt.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class MTPlatform {

    public interface Bridge {
        boolean isModLoaded(String modId);

        CompoundTag persistentData(Player player);
    }

    private static final Bridge NOOP = new Bridge() {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public CompoundTag persistentData(Player player) {
            return new CompoundTag();
        }
    };

    private static Bridge bridge = NOOP;

    private MTPlatform() {}

    public static void setBridge(Bridge value) {
        bridge = value != null ? value : NOOP;
    }

    public static boolean isModLoaded(String modId) {
        return bridge.isModLoaded(modId);
    }

    public static CompoundTag persistentData(Player player) {
        return bridge.persistentData(player);
    }
}
