package mt.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UserContentPacket(Map<String, List<String>> content) implements CustomPacketPayload {

    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "user_content");
    public static final Type<@NotNull UserContentPacket> TYPE = new Type<>(ID_LOC);

    public static final StreamCodec<RegistryFriendlyByteBuf, UserContentPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.content().size());
                for (Map.Entry<String, List<String>> entry : packet.content().entrySet()) {
                    buf.writeUtf(entry.getKey());
                    buf.writeInt(entry.getValue().size());
                    for (String s : entry.getValue()) {
                        buf.writeUtf(s);
                    }
                }
            },
            buf -> {
                int mapSize = buf.readInt();
                Map<String, List<String>> map = new HashMap<>();
                for (int i = 0; i < mapSize; i++) {
                    String key = buf.readUtf();
                    int listSize = buf.readInt();
                    List<String> list = new ArrayList<>();
                    for (int j = 0; j < listSize; j++) {
                        list.add(buf.readUtf());
                    }
                    map.put(key, list);
                }
                return new UserContentPacket(map);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}