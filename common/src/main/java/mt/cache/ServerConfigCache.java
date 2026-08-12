package mt.cache;

import mt.network.packet.SyncConfigPacket;

public class ServerConfigCache {
    private static SyncConfigPacket cached = null;

    public static void apply(SyncConfigPacket packet) {
        cached = packet;
    }

    public static SyncConfigPacket get() {
        return cached;
    }

    public static void clear() {
        cached = null;
    }
}
