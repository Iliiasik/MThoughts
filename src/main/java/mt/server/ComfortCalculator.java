package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;

public class ComfortCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final TagKey<Block> LIGHTING_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_lighting"));
    private static final TagKey<Block> CARPET_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_carpet"));
    private static final TagKey<Block> FURNITURE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_furniture"));
    private static final TagKey<Block> DECOR_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_decoration"));
    private static final TagKey<Block> STRUCTURE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_structure"));

    public static int calculateComfortLevel(ServerPlayerEntity player) {
        if (!CONFIG.getComfort().enabled) {
            return 0;
        }

        int scanRadius = CONFIG.getComfort().scanRadius;
        BlockPos bedPos = player.getSleepingPosition().orElse(player.getBlockPos());
        World world = player.getEntityWorld();

        boolean hasLighting = false;
        boolean hasCarpet = false;
        boolean hasFurniture = false;
        boolean hasDecor = false;
        boolean hasStructure = false;

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockPos checkPos = bedPos.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    boolean inLighting = state.isIn(LIGHTING_TAG);
                    boolean inCarpet = state.isIn(CARPET_TAG);
                    boolean inFurniture = state.isIn(FURNITURE_TAG);
                    boolean inDecor = state.isIn(DECOR_TAG);
                    boolean inStructure = state.isIn(STRUCTURE_TAG);

                    if (!hasLighting && inLighting) {
                        hasLighting = true;
                    }
                    if (!hasCarpet && inCarpet) {
                        hasCarpet = true;
                    }
                    if (!hasFurniture && inFurniture) {
                        hasFurniture = true;
                    }
                    if (!hasDecor && inDecor) {
                        hasDecor = true;
                    }
                    if (!hasStructure && inStructure) {
                        hasStructure = true;
                    }

                    if (hasLighting && hasCarpet && hasFurniture && hasDecor && hasStructure) {
                        break;
                    }
                }
            }
        }

        int comfortLevel = 0;
        if (hasLighting) comfortLevel++;
        if (hasCarpet) comfortLevel++;
        if (hasFurniture) comfortLevel++;
        if (hasDecor) comfortLevel++;
        if (hasStructure) comfortLevel++;

        return comfortLevel;
    }
}