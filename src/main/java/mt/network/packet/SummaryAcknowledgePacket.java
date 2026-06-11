package mt.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record SummaryAcknowledgePacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull SummaryAcknowledgePacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("midnightthoughts", "summary_acknowledge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SummaryAcknowledgePacket> CODEC =
            StreamCodec.unit(new SummaryAcknowledgePacket());

    @Override
    public CustomPacketPayload.@NotNull Type<@NotNull ? extends CustomPacketPayload> type() {
        return TYPE;
    }
}