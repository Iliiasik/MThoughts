package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record MoonPhasePacket(int moonPhase) {
    public static final Identifier ID = new Identifier("midnightthoughts", "moon_phase");

    public static void encode(MoonPhasePacket packet, PacketByteBuf buf) {
        buf.writeVarInt(packet.moonPhase);
    }

    public static MoonPhasePacket decode(PacketByteBuf buf) {
        int moonPhase = buf.readVarInt();
        return new MoonPhasePacket(moonPhase);
    }
}