package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.server.level.ServerPlayer;

public class NightmareKubeEvent extends EventJS {
    private final ServerPlayer player;
    private final int comfortLevel;

    public NightmareKubeEvent(ServerPlayer player, int comfortLevel) {
        this.player = player;
        this.comfortLevel = comfortLevel;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getComfortLevel() { return comfortLevel; }
}