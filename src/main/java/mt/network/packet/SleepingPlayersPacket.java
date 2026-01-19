package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SleepingPlayersPacket {
    public static final Identifier ID = new Identifier("midnightthoughts", "sleeping_players");

    private final int sleepingCount;
    private final int totalPlayers;

    public SleepingPlayersPacket(int sleepingCount, int totalPlayers) {
        this.sleepingCount = sleepingCount;
        this.totalPlayers = totalPlayers;
    }

    public int sleepingCount() {
        return sleepingCount;
    }

    public int totalPlayers() {
        return totalPlayers;
    }

    public static void encode(SleepingPlayersPacket packet, PacketByteBuf buf) {
        buf.writeVarInt(packet.sleepingCount);
        buf.writeVarInt(packet.totalPlayers);
    }

    public static SleepingPlayersPacket decode(PacketByteBuf buf) {
        int sleepingCount = buf.readVarInt();
        int totalPlayers = buf.readVarInt();
        return new SleepingPlayersPacket(sleepingCount, totalPlayers);
    }
}