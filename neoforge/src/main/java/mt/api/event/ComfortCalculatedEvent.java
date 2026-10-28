package mt.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

@SuppressWarnings("unused")
public class ComfortCalculatedEvent extends Event {
    private final ServerPlayer player;
    private int level;

    public ComfortCalculatedEvent(ServerPlayer player, int level) {
        this.player = player;
        this.level = level;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
