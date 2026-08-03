package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public class ComfortCalculatedKubeEvent extends EventJS {
    private final mt.api.event.ComfortCalculatedEvent event;

    public ComfortCalculatedKubeEvent(mt.api.event.ComfortCalculatedEvent event) {
        this.event = event;
    }

    public ServerPlayer getPlayer() { return event.getPlayer(); }
    public int getLevel() { return event.getLevel(); }
    public void setLevel(int level) { event.setLevel(level); }
}