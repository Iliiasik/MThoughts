package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UserContentPacket(Map<String, List<String>> content) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "user_content");
    public static final CustomPacketPayload.Type<UserContentPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, UserContentPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.content().size());
                for (Map.Entry<String, List<String>> entry : packet.content().entrySet()) {
                    buf.writeUtf(entry.getKey());
                    buf.writeVarInt(entry.getValue().size());
                    for (String s : entry.getValue()) {
                        buf.writeUtf(s);
                    }
                }
            },
            buf -> {
                int mapSize = buf.readVarInt();
                Map<String, List<String>> map = new HashMap<>();
                for (int i = 0; i < mapSize; i++) {
                    String key = buf.readUtf();
                    int listSize = buf.readVarInt();
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