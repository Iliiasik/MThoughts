package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;

public record MoonPhasePacket(int moonPhase) {

    public static void encode(MoonPhasePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.moonPhase);
    }

    public static MoonPhasePacket decode(FriendlyByteBuf buf) {
        return new MoonPhasePacket(buf.readInt());
    }
}