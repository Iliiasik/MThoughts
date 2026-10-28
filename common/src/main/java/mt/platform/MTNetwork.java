package mt.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class MTNetwork {

    public interface Bridge {
        void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

        void sendToAll(MinecraftServer server, CustomPacketPayload payload);
    }

    private static final Bridge NOOP = new Bridge() {
        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {}

        @Override
        public void sendToAll(MinecraftServer server, CustomPacketPayload payload) {}
    };

    private static Bridge bridge = NOOP;

    private MTNetwork() {}

    public static void setBridge(Bridge value) {
        bridge = value != null ? value : NOOP;
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        bridge.sendToPlayer(player, payload);
    }

    public static void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
        bridge.sendToAll(server, payload);
    }
}
