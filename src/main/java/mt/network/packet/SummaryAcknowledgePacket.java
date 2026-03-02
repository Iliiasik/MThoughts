package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SummaryAcknowledgePacket() implements CustomPacketPayload {
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "summary_acknowledge");
    public static final CustomPacketPayload.Type<SummaryAcknowledgePacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, SummaryAcknowledgePacket> CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new SummaryAcknowledgePacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}