package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SummaryAcknowledgePacket {
    public static final Identifier ID = new Identifier("midnightthoughts", "summary_acknowledge");

    public static void encode(SummaryAcknowledgePacket packet, PacketByteBuf buf) {
    }

    public static SummaryAcknowledgePacket decode(PacketByteBuf buf) {
        return new SummaryAcknowledgePacket();
    }
}