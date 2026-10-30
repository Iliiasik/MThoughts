package mt.network.packet;

import mt.MTConstants;
import mt.network.MTPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UserContentPacket(Map<String, List<String>> content) implements MTPacket {
    public static final ResourceLocation ID = MTConstants.id("user_content");

    public static UserContentPacket read(FriendlyByteBuf buf) {
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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(content().size());
        for (Map.Entry<String, List<String>> entry : content().entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue().size());
            for (String s : entry.getValue()) {
                buf.writeUtf(s);
            }
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}