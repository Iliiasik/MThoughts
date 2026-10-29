package mt.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class MTClientNetwork {

    public interface Bridge {
        void sendToServer(CustomPacketPayload payload);
    }

    private static Bridge bridge = payload -> {};

    private MTClientNetwork() {}

    public static void setBridge(Bridge value) {
        bridge = value;
    }

    public static void sendToServer(CustomPacketPayload payload) {
        bridge.sendToServer(payload);
    }
}
