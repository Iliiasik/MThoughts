package mt.gametest;

import mt.api.event.ComfortCalculatedEvent;
import mt.api.event.MvpDeterminedEvent;
import mt.api.event.NightmareEvent;
import mt.api.event.WellRestedAppliedEvent;
import mt.api.event.WellRestedExpiredEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.function.Consumer;

public final class ForgeGameTestHooks {

    private ForgeGameTestHooks() {}

    public static void register(RegisterGameTestsEvent event) {
        TestHarness.setLoaderName("Forge");
        event.register(MTGameTests.class);
        listen(ComfortCalculatedEvent.class, e -> {
            e.setLevel(e.getLevel() + ApiEventLog.comfortBonus());
            ApiEventLog.record("comfort", e.getPlayer(), e.getLevel(), false, 0);
        });
        listen(NightmareEvent.class,
                e -> ApiEventLog.record("nightmare", e.getPlayer(), e.getComfortLevel(), false, 0));
        listen(WellRestedAppliedEvent.class,
                e -> ApiEventLog.record("applied", e.getPlayer(), e.getLevel(), e.isMvp(), e.getDurationTicks()));
        listen(WellRestedExpiredEvent.class,
                e -> ApiEventLog.record("expired", e.getPlayer(), 0, false, 0));
        listen(MvpDeterminedEvent.class,
                e -> ApiEventLog.record("mvp", e.getMvp(), 0, true, 0));
    }

    private static <T extends Event> void listen(Class<T> type, Consumer<T> listener) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, type, listener);
    }
}
