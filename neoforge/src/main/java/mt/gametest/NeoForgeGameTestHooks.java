package mt.gametest;

import mt.api.event.ComfortCalculatedEvent;
import mt.api.event.MvpDeterminedEvent;
import mt.api.event.NightmareEvent;
import mt.api.event.WellRestedAppliedEvent;
import mt.api.event.WellRestedExpiredEvent;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

public final class NeoForgeGameTestHooks {

    private NeoForgeGameTestHooks() {}

    public static void register(IEventBus modEventBus) {
        TestHarness.setLoaderName("NeoForge");
        List<MTGameTests.Entry> entries = MTGameTests.entries();
        modEventBus.addListener(RegisterEvent.class, event -> event.register(Registries.TEST_FUNCTION,
                helper -> entries.forEach(entry -> helper.register(entry.id(), entry.function()))));
        modEventBus.addListener(RegisterGameTestsEvent.class, event -> entries.forEach(entry ->
                event.registerTest(entry.id(), entry.instance(event.registerEnvironment(entry.environmentId())))));
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
