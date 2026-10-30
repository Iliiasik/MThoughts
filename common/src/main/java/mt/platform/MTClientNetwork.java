package mt.platform;

import mt.network.MTPacket;

public final class MTClientNetwork {

    public interface Bridge {
        void sendToServer(MTPacket packet);
    }

    private static Bridge bridge = packet -> {};

    private MTClientNetwork() {}

    public static void setBridge(Bridge value) {
        bridge = value;
    }

    public static void sendToServer(MTPacket packet) {
        bridge.sendToServer(packet);
    }
}
