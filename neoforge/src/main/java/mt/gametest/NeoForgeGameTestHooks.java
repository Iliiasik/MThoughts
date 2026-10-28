package mt.gametest;

import mt.api.event.ComfortCalculatedEvent;
import mt.api.event.MvpDeterminedEvent;
import mt.api.event.NightmareEvent;
import mt.api.event.WellRestedAppliedEvent;
import mt.api.event.WellRestedExpiredEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class NeoForgeGameTestHooks {

    private NeoForgeGameTestHooks() {}

    public static void register(RegisterGameTestsEvent event) {
        TestHarness.setLoaderName("NeoForge");
        event.register(MTGameTests.class);
        NeoForge.EVENT_BUS.addListener(ComfortCalculatedEvent.class, e -> {
            e.setLevel(e.getLevel() + ApiEventLog.comfortBonus());
            ApiEventLog.record("comfort", e.getPlayer(), e.getLevel(), false, 0);
        });
        NeoForge.EVENT_BUS.addListener(NightmareEvent.class,
                e -> ApiEventLog.record("nightmare", e.getPlayer(), e.getComfortLevel(), false, 0));
        NeoForge.EVENT_BUS.addListener(WellRestedAppliedEvent.class,
                e -> ApiEventLog.record("applied", e.getPlayer(), e.getLevel(), e.isMvp(), e.getDurationTicks()));
        NeoForge.EVENT_BUS.addListener(WellRestedExpiredEvent.class,
                e -> ApiEventLog.record("expired", e.getPlayer(), 0, false, 0));
        NeoForge.EVENT_BUS.addListener(MvpDeterminedEvent.class,
                e -> ApiEventLog.record("mvp", e.getMvp(), 0, true, 0));
    }
}
