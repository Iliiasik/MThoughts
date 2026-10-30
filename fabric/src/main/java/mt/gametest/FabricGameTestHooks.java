package mt.gametest;

import mt.api.event.ComfortCalculatedCallback;
import mt.api.event.MvpDeterminedCallback;
import mt.api.event.NightmareCallback;
import mt.api.event.WellRestedAppliedCallback;
import mt.api.event.WellRestedExpiredCallback;
import net.minecraft.gametest.framework.GameTestRegistry;

public final class FabricGameTestHooks {

    private FabricGameTestHooks() {}

    public static void register() {
        TestHarness.setLoaderName("Fabric");
        GameTestRegistry.register(MTGameTests.class);
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
