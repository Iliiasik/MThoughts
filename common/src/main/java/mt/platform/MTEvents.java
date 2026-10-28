package mt.platform;

import net.minecraft.server.level.ServerPlayer;

public final class MTEvents {

    public interface Bridge {
        int comfortCalculated(ServerPlayer player, int level);

        void nightmare(ServerPlayer player, int comfortLevel);

        void wellRestedApplied(ServerPlayer player, int level, boolean mvp, int durationTicks);

        void wellRestedExpired(ServerPlayer player);

        void mvpDetermined(ServerPlayer mvp);
    }

    private static final Bridge NOOP = new Bridge() {
        @Override
        public int comfortCalculated(ServerPlayer player, int level) {
            return level;
        }

        @Override
        public void nightmare(ServerPlayer player, int comfortLevel) {}

        @Override
        public void wellRestedApplied(ServerPlayer player, int level, boolean mvp, int durationTicks) {}

        @Override
        public void wellRestedExpired(ServerPlayer player) {}

        @Override
        public void mvpDetermined(ServerPlayer mvp) {}
    };

    private static Bridge bridge = NOOP;

    private MTEvents() {}

    public static void setBridge(Bridge value) {
        bridge = value != null ? value : NOOP;
    }

    public static int comfortCalculated(ServerPlayer player, int level) {
        return bridge.comfortCalculated(player, level);
    }

    public static void nightmare(ServerPlayer player, int comfortLevel) {
        bridge.nightmare(player, comfortLevel);
    }

    public static void wellRestedApplied(ServerPlayer player, int level, boolean mvp, int durationTicks) {
        bridge.wellRestedApplied(player, level, mvp, durationTicks);
    }

    public static void wellRestedExpired(ServerPlayer player) {
        bridge.wellRestedExpired(player);
    }

    public static void mvpDetermined(ServerPlayer mvp) {
        bridge.mvpDetermined(mvp);
    }
}
