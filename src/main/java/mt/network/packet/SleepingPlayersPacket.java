package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SleepingPlayersPacket(int sleepingCount, int totalPlayers) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "sleeping_players");
    public static final CustomPacketPayload.Type<SleepingPlayersPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, SleepingPlayersPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.sleepingCount());
                buf.writeInt(packet.totalPlayers());
            },
            buf -> new SleepingPlayersPacket(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}