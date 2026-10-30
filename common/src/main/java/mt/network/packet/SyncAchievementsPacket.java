package mt.network.packet;

import mt.MTConstants;
import mt.network.MTPacket;
import mt.server.AchievementDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncAchievementsPacket(List<AchievementDefinition> achievements) implements MTPacket {
    public static final ResourceLocation ID = MTConstants.id("sync_achievements");

    public static SyncAchievementsPacket read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(achievements().size());
        for (AchievementDefinition def : achievements()) {
            buf.writeUtf(def.id);
            buf.writeUtf(def.name);
            buf.writeUtf(def.tooltip != null ? def.tooltip : "");
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}