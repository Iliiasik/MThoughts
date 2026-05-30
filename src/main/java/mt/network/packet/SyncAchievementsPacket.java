package mt.network.packet;

import mt.server.AchievementDefinition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record SyncAchievementsPacket(List<AchievementDefinition> achievements) {
    public static final Identifier ID = new Identifier("midnightthoughts", "sync_achievements");

    public static void encode(SyncAchievementsPacket packet, PacketByteBuf buf) {
        buf.writeInt(packet.achievements().size());
        for (AchievementDefinition def : packet.achievements()) {
            buf.writeString(def.id);
            buf.writeString(def.name);
            buf.writeString(def.tooltip != null ? def.tooltip : "");
        }
    }

    public static SyncAchievementsPacket decode(PacketByteBuf buf) {
        int size = buf.readInt();
        List<AchievementDefinition> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            AchievementDefinition def = new AchievementDefinition();
            def.id = buf.readString();
            def.name = buf.readString();
            String tooltip = buf.readString();
            def.tooltip = tooltip.isEmpty() ? null : tooltip;
            list.add(def);
        }
        return new SyncAchievementsPacket(list);
    }
}