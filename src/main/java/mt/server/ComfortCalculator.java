package mt.server;

import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ComfortCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final TagKey<Block> LIGHTING_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("midnightthoughts", "comfort_lighting"));
    private static final TagKey<Block> CARPET_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("midnightthoughts", "comfort_carpet"));
    private static final TagKey<Block> FURNITURE_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("midnightthoughts", "comfort_furniture"));
    private static final TagKey<Block> DECOR_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("midnightthoughts", "comfort_decoration"));
    private static final TagKey<Block> STRUCTURE_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("midnightthoughts", "comfort_structure"));

    public static int calculateComfortLevel(ServerPlayer player) {
        if (!CONFIG.getComfort().enabled) {
            return 0;
        }

        int scanRadius = CONFIG.getComfort().scanRadius;
        BlockPos bedPos = player.getSleepingPos().orElse(player.blockPosition());
        Level world = player.level();

        boolean hasLighting = false;
        boolean hasCarpet = false;
        boolean hasFurniture = false;
        boolean hasDecor = false;
        boolean hasStructure = false;

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockPos checkPos = bedPos.offset(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    boolean inLighting = state.is(LIGHTING_TAG);
                    boolean inCarpet = state.is(CARPET_TAG);
                    boolean inFurniture = state.is(FURNITURE_TAG);
                    boolean inDecor = state.is(DECOR_TAG);
                    boolean inStructure = state.is(STRUCTURE_TAG);

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