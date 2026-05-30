package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UserContentPacket(Map<String, List<String>> content) {
    public static final Identifier ID = new Identifier("midnightthoughts", "user_content");

    public static void encode(UserContentPacket packet, PacketByteBuf buf) {
        buf.writeInt(packet.content().size());
        for (Map.Entry<String, List<String>> entry : packet.content().entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeInt(entry.getValue().size());
            for (String s : entry.getValue()) {
                buf.writeString(s);
            }
        }
    }

    public static UserContentPacket decode(PacketByteBuf buf) {
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
}