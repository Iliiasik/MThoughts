package mt.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

@SuppressWarnings("unused")
public class NightmareEvent extends Event {
    private final ServerPlayer player;
    private final int comfortLevel;

    public NightmareEvent(ServerPlayer player, int comfortLevel) {
        this.player = player;
        this.comfortLevel = comfortLevel;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getComfortLevel() { return comfortLevel; }
}
