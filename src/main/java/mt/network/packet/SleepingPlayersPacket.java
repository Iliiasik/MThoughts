package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record SleepingPlayersPacket(int sleepingCount, int totalPlayers) {
    public static final Identifier ID = new Identifier("midnightthoughts", "sleeping_players");

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