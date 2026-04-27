package mt.client.manager;

import net.minecraft.client.player.LocalPlayer;

public class SleepStateManager {
    private boolean isSleeping = false;
    private boolean wasSleeping = false;

    public void tick(LocalPlayer player) {
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