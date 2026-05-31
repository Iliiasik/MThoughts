package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record SleepingPlayersPacket(int sleepingCount, int totalPlayers) implements CustomPacketPayload {
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "sleeping_players");
    public static final CustomPacketPayload.Type<@org.jetbrains.annotations.NotNull SleepingPlayersPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, SleepingPlayersPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.sleepingCount());
                buf.writeInt(packet.totalPlayers());
            },
            buf -> new SleepingPlayersPacket(buf.readInt(), buf.readInt())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}