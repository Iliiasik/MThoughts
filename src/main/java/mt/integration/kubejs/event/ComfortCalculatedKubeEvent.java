package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public class ComfortCalculatedKubeEvent extends EventJS {
    private final ServerPlayer player;
    private int level;

    public ComfortCalculatedKubeEvent(ServerPlayer player, int level) {
        this.player = player;
        this.level = level;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
