package mt.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ComfortCalculatedCallback {

    Event<ComfortCalculatedCallback> EVENT = EventFactory.createArrayBacked(
            ComfortCalculatedCallback.class,
            listeners -> (player, level) -> {
                int result = level;
                for (ComfortCalculatedCallback listener : listeners) {
                    result = listener.onComfortCalculated(player, result);
                }
                return result;
            });

    int onComfortCalculated(ServerPlayer player, int level);
}
