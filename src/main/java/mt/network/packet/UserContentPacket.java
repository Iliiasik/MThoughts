package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UserContentPacket(Map<String, List<String>> content) implements CustomPayload {

    public static final Identifier ID_LOC = Identifier.of("midnightthoughts", "user_content");
    public static final Id<@NotNull UserContentPacket> ID = new Id<>(ID_LOC);

    public static final PacketCodec<PacketByteBuf, UserContentPacket> CODEC = PacketCodec.of(
            (packet, buf) -> {
                buf.writeInt(packet.content().size());
                for (Map.Entry<String, List<String>> entry : packet.content().entrySet()) {
                    buf.writeString(entry.getKey());
                    buf.writeInt(entry.getValue().size());
                    for (String s : entry.getValue()) {
                        buf.writeString(s);
                    }
                }
            },
            buf -> {
                int mapSize = buf.readInt();
                Map<String, List<String>> map = new HashMap<>();
                for (int i = 0; i < mapSize; i++) {
                    String key = buf.readString();
                    int listSize = buf.readInt();
                    List<String> list = new ArrayList<>();
                    for (int j = 0; j < listSize; j++) {
                        list.add(buf.readString());
                    }
                    map.put(key, list);
                }
                return new UserContentPacket(map);
            }
    );

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }
}