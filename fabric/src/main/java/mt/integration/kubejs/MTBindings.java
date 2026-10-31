package mt.integration.kubejs;

import mt.server.ComfortCalculator;
import mt.server.WellRestedEffect;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public class MTBindings {
    private MTBindings() {}

    public static void grantWellRested(ServerPlayer player, int level) {
        WellRestedEffect.applyToPlayer(player, level);
    }

    public static void grantMvp(ServerPlayer player) {
        WellRestedEffect.applyMvpToPlayer(player);
    }

    public static void clearWellRested(ServerPlayer player) {
        WellRestedEffect.clear(player);
    }

    public static boolean hasWellRested(ServerPlayer player) {
        return WellRestedEffect.hasEffect(player);
    }

    public static int getLevel(ServerPlayer player) {
        return WellRestedEffect.getLevel(player);
    }

    public static int getComfort(ServerPlayer player) {
        return ComfortCalculator.calculateComfortLevel(player);
    }
}
