package mt.client.manager;

import net.minecraft.client.network.ClientPlayerEntity;

public class SleepStateManager {
    private boolean isSleeping = false;
    private boolean wasSleeping = false;

    public void tick(ClientPlayerEntity player) {
        wasSleeping = isSleeping;
        isSleeping = player != null && player.isSleeping();
    }

    public boolean isSleeping() {
        return isSleeping;
    }

    public boolean justStartedSleeping() {
        return isSleeping && !wasSleeping;
    }

    public boolean justStoppedSleeping() {
        return !isSleeping && wasSleeping;
    }
}
