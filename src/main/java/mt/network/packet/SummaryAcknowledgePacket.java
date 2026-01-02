package mt.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SummaryAcknowledgePacket() implements CustomPayload {
    public static final CustomPayload.Id<SummaryAcknowledgePacket> ID =
        new CustomPayload.Id<>(Identifier.of("midnightthoughts", "summary_acknowledge"));

    public static final PacketCodec<RegistryByteBuf, SummaryAcknowledgePacket> CODEC =
        PacketCodec.of(
            (value, buf) -> {},
            buf -> new SummaryAcknowledgePacket()
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

