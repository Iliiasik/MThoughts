package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ComfortCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final TagKey<Block> LIGHTING_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_lighting"));
    private static final TagKey<Block> CARPET_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_carpet"));
    private static final TagKey<Block> FURNITURE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_furniture"));
    private static final TagKey<Block> DECOR_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_decoration"));
    private static final TagKey<Block> STRUCTURE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_structure"));
    private static final TagKey<Block> NEGATIVE_MACABRE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_macabre"));
    private static final TagKey<Block> NEGATIVE_HOSTILE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_hostile"));
    private static final TagKey<Block> NEGATIVE_DARK_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_dark"));

    @SuppressWarnings("unchecked")
    private static final TagKey<Block>[] ALL_TAGS = (TagKey<Block>[]) new TagKey[]{
            LIGHTING_TAG, CARPET_TAG, FURNITURE_TAG, DECOR_TAG, STRUCTURE_TAG,
            NEGATIVE_MACABRE_TAG, NEGATIVE_HOSTILE_TAG, NEGATIVE_DARK_TAG
    };

    public static int calculateComfortLevel(ServerPlayer player) {
        if (!CONFIG.getComfort().enabled) {
            return 0;
        }

        int scanRadius = CONFIG.getComfort().scanRadius;
        BlockPos bedPos = player.getSleepingPos().orElse(player.blockPosition());
        Level world = player.level();

        boolean[] found = new boolean[ALL_TAGS.length];
        outer:
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockState state = world.getBlockState(bedPos.offset(x, y, z));
                    boolean allFound = true;
                    for (int i = 0; i < ALL_TAGS.length; i++) {
                        if (!found[i] && state.is(ALL_TAGS[i])) found[i] = true;
                        if (!found[i]) allFound = false;
                    }
                    if (allFound) break outer;
                }
            }
        }

        int comfortLevel = 0;
        if (found[0]) comfortLevel++;
        if (found[1]) comfortLevel++;
        if (found[2]) comfortLevel++;
        if (found[3]) comfortLevel++;
        if (found[4]) comfortLevel++;
        if (found[5]) comfortLevel--;
        if (found[6]) comfortLevel--;
        if (found[7]) comfortLevel--;

        return comfortLevel;
    }

    public static boolean isNightmareMode(ServerPlayer player) {
        return calculateComfortLevel(player) < 0;
    }

    public static boolean isSleepBlocked(ServerPlayer player) {
        return calculateComfortLevel(player) <= -2;
    }
}