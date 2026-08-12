package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public class NightmareKubeEvent implements KubeEvent {
    private final ServerPlayer player;
    private final int comfortLevel;

    public NightmareKubeEvent(ServerPlayer player, int comfortLevel) {
        this.player = player;
        this.comfortLevel = comfortLevel;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getComfortLevel() { return comfortLevel; }
}
