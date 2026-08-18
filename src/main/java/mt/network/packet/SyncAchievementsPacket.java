package mt.network.packet;

import mt.server.AchievementDefinition;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record SyncAchievementsPacket(List<AchievementDefinition> achievements) {

    public static void encode(SyncAchievementsPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.achievements.size());
        for (AchievementDefinition def : packet.achievements) {
            buf.writeUtf(def.id);
            buf.writeUtf(def.name);
            buf.writeUtf(def.tooltip != null ? def.tooltip : "");
        }
    }

    public static SyncAchievementsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<AchievementDefinition> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            AchievementDefinition def = new AchievementDefinition();
            def.id = buf.readUtf();
            def.name = buf.readUtf();
            String tooltip = buf.readUtf();
            def.tooltip = tooltip.isEmpty() ? null : tooltip;
            list.add(def);
        }
        return new SyncAchievementsPacket(list);
    }
}