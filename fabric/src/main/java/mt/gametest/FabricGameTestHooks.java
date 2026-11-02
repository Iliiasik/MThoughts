package mt.gametest;

import mt.api.event.ComfortCalculatedCallback;
import mt.api.event.MvpDeterminedCallback;
import mt.api.event.NightmareCallback;
import mt.api.event.WellRestedAppliedCallback;
import mt.api.event.WellRestedExpiredCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;

import java.util.List;
import java.util.Optional;

public final class FabricGameTestHooks {

    private FabricGameTestHooks() {}

    public static void register() {
        TestHarness.setLoaderName("Fabric");
        List<MTGameTests.Entry> entries = MTGameTests.entries();
        entries.forEach(entry -> Registry.register(BuiltInRegistries.TEST_FUNCTION, entry.id(), entry.function()));
        DynamicRegistrySetupCallback.EVENT.register(view -> {
            Optional<Registry<TestEnvironmentDefinition<?>>> environments = view.getOptional(Registries.TEST_ENVIRONMENT);
            Optional<Registry<GameTestInstance>> tests = view.getOptional(Registries.TEST_INSTANCE);
            if (environments.isEmpty() || tests.isEmpty()) return;
            for (MTGameTests.Entry entry : entries) {
                Holder<TestEnvironmentDefinition<?>> environment = Registry.registerForHolder(environments.get(),
                        entry.environmentId(), new TestEnvironmentDefinition.AllOf(List.of()));
                Registry.register(tests.get(), entry.id(), entry.instance(environment));
            }
        });
        ComfortCalculatedCallback.EVENT.register((player, level) -> {
            int result = level + ApiEventLog.comfortBonus();
            ApiEventLog.record("comfort", player, result, false, 0);
            return result;
        });
        NightmareCallback.EVENT.register((player, comfortLevel) ->
                ApiEventLog.record("nightmare", player, comfortLevel, false, 0));
        WellRestedAppliedCallback.EVENT.register((player, level, mvp, durationTicks) ->
                ApiEventLog.record("applied", player, level, mvp, durationTicks));
        WellRestedExpiredCallback.EVENT.register(player ->
                ApiEventLog.record("expired", player, 0, false, 0));
        MvpDeterminedCallback.EVENT.register(mvp ->
                ApiEventLog.record("mvp", mvp, 0, true, 0));
    }
}
