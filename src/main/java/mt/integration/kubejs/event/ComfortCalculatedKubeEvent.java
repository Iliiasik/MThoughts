package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerPlayer;

public class ComfortCalculatedKubeEvent implements KubeEvent {
    private final mt.api.event.ComfortCalculatedEvent event;

    public ComfortCalculatedKubeEvent(mt.api.event.ComfortCalculatedEvent event) {
        this.event = event;
    }

    public ServerPlayer getPlayer() { return event.getPlayer(); }
    public int getLevel() { return event.getLevel(); }
    public void setLevel(int level) { event.setLevel(level); }
}
