package mt.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class MTClientNetwork {

    public interface Bridge {
        void sendToServer(CustomPacketPayload payload);
    }

    private static final Bridge NOOP = _ -> {};

    private static Bridge bridge = NOOP;

    private MTClientNetwork() {}

    public static void setBridge(Bridge value) {
        bridge = value != null ? value : NOOP;
    }

    public static void sendToServer(CustomPacketPayload payload) {
        bridge.sendToServer(payload);
    }
}
