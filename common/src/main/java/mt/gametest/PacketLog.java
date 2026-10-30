package mt.gametest;

import mt.platform.MTNetwork;
import mt.network.MTPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PacketLog implements MTNetwork.Bridge {

    private record Sent(UUID player, MTPacket payload) {}

    private static final List<Sent> SENT = new ArrayList<>();

    private PacketLog() {}

    public static void install() {
        MTNetwork.setBridge(new PacketLog());
    }

    @Override
    public void sendToPlayer(ServerPlayer player, MTPacket payload) {
        SENT.add(new Sent(player.getUUID(), payload));
    }

    public static <T extends MTPacket> List<T> sentTo(UUID player, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Sent sent : SENT) {
            if (sent.player().equals(player) && type.isInstance(sent.payload())) {
                result.add(type.cast(sent.payload()));
            }
        }
        return result;
    }

    public static void clear() {
        SENT.clear();
    }
}
