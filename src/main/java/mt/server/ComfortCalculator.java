package mt.server;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class ComfortCalculator {
    private static final int SCAN_RADIUS = 5;

    private static final Set<Block> LIGHTING_BLOCKS = new HashSet<>();
    private static final Set<Block> CARPET_BLOCKS = new HashSet<>();
    private static final Set<Block> FURNITURE_BLOCKS = new HashSet<>();
    private static final Set<Block> DECOR_BLOCKS = new HashSet<>();
    private static final Set<Block> STRUCTURE_BLOCKS = new HashSet<>();

    static {
        LIGHTING_BLOCKS.add(Blocks.TORCH);
        LIGHTING_BLOCKS.add(Blocks.WALL_TORCH);
        LIGHTING_BLOCKS.add(Blocks.SOUL_TORCH);
        LIGHTING_BLOCKS.add(Blocks.SOUL_WALL_TORCH);
        LIGHTING_BLOCKS.add(Blocks.LANTERN);
        LIGHTING_BLOCKS.add(Blocks.SOUL_LANTERN);
        LIGHTING_BLOCKS.add(Blocks.GLOWSTONE);
        LIGHTING_BLOCKS.add(Blocks.SEA_LANTERN);
        LIGHTING_BLOCKS.add(Blocks.REDSTONE_LAMP);
        LIGHTING_BLOCKS.add(Blocks.SHROOMLIGHT);
        LIGHTING_BLOCKS.add(Blocks.END_ROD);
        LIGHTING_BLOCKS.add(Blocks.CANDLE);
        LIGHTING_BLOCKS.add(Blocks.WHITE_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.ORANGE_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.MAGENTA_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.LIGHT_BLUE_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.YELLOW_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.LIME_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.PINK_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.GRAY_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.LIGHT_GRAY_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.CYAN_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.PURPLE_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.BLUE_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.BROWN_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.GREEN_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.RED_CANDLE);
        LIGHTING_BLOCKS.add(Blocks.BLACK_CANDLE);

        CARPET_BLOCKS.add(Blocks.WHITE_CARPET);
        CARPET_BLOCKS.add(Blocks.ORANGE_CARPET);
        CARPET_BLOCKS.add(Blocks.MAGENTA_CARPET);
        CARPET_BLOCKS.add(Blocks.LIGHT_BLUE_CARPET);
        CARPET_BLOCKS.add(Blocks.YELLOW_CARPET);
        CARPET_BLOCKS.add(Blocks.LIME_CARPET);
        CARPET_BLOCKS.add(Blocks.PINK_CARPET);
        CARPET_BLOCKS.add(Blocks.GRAY_CARPET);
        CARPET_BLOCKS.add(Blocks.LIGHT_GRAY_CARPET);
        CARPET_BLOCKS.add(Blocks.CYAN_CARPET);
        CARPET_BLOCKS.add(Blocks.PURPLE_CARPET);
        CARPET_BLOCKS.add(Blocks.BLUE_CARPET);
        CARPET_BLOCKS.add(Blocks.BROWN_CARPET);
        CARPET_BLOCKS.add(Blocks.GREEN_CARPET);
        CARPET_BLOCKS.add(Blocks.RED_CARPET);
        CARPET_BLOCKS.add(Blocks.BLACK_CARPET);
        CARPET_BLOCKS.add(Blocks.MOSS_CARPET);

        FURNITURE_BLOCKS.add(Blocks.BOOKSHELF);
        FURNITURE_BLOCKS.add(Blocks.CHEST);
        FURNITURE_BLOCKS.add(Blocks.TRAPPED_CHEST);
        FURNITURE_BLOCKS.add(Blocks.CRAFTING_TABLE);
        FURNITURE_BLOCKS.add(Blocks.FURNACE);
        FURNITURE_BLOCKS.add(Blocks.BLAST_FURNACE);
        FURNITURE_BLOCKS.add(Blocks.SMOKER);
        FURNITURE_BLOCKS.add(Blocks.ENCHANTING_TABLE);
        FURNITURE_BLOCKS.add(Blocks.ANVIL);
        FURNITURE_BLOCKS.add(Blocks.CHIPPED_ANVIL);
        FURNITURE_BLOCKS.add(Blocks.DAMAGED_ANVIL);
        FURNITURE_BLOCKS.add(Blocks.LECTERN);
        FURNITURE_BLOCKS.add(Blocks.BARREL);
        FURNITURE_BLOCKS.add(Blocks.CARTOGRAPHY_TABLE);
        FURNITURE_BLOCKS.add(Blocks.FLETCHING_TABLE);
        FURNITURE_BLOCKS.add(Blocks.GRINDSTONE);
        FURNITURE_BLOCKS.add(Blocks.LOOM);
        FURNITURE_BLOCKS.add(Blocks.SMITHING_TABLE);
        FURNITURE_BLOCKS.add(Blocks.STONECUTTER);
        FURNITURE_BLOCKS.add(Blocks.BREWING_STAND);
        FURNITURE_BLOCKS.add(Blocks.CAULDRON);
        FURNITURE_BLOCKS.add(Blocks.WATER_CAULDRON);
        FURNITURE_BLOCKS.add(Blocks.LAVA_CAULDRON);
        FURNITURE_BLOCKS.add(Blocks.POWDER_SNOW_CAULDRON);
        FURNITURE_BLOCKS.add(Blocks.JUKEBOX);
        FURNITURE_BLOCKS.add(Blocks.BELL);
        FURNITURE_BLOCKS.add(Blocks.DECORATED_POT);

        DECOR_BLOCKS.add(Blocks.FLOWER_POT);
        DECOR_BLOCKS.add(Blocks.POTTED_OAK_SAPLING);
        DECOR_BLOCKS.add(Blocks.POTTED_SPRUCE_SAPLING);
        DECOR_BLOCKS.add(Blocks.POTTED_BIRCH_SAPLING);
        DECOR_BLOCKS.add(Blocks.POTTED_JUNGLE_SAPLING);
        DECOR_BLOCKS.add(Blocks.POTTED_ACACIA_SAPLING);
        DECOR_BLOCKS.add(Blocks.POTTED_CHERRY_SAPLING);
        DECOR_BLOCKS.add(Blocks.POTTED_DARK_OAK_SAPLING);
        DECOR_BLOCKS.add(Blocks.POTTED_MANGROVE_PROPAGULE);
        DECOR_BLOCKS.add(Blocks.POTTED_FERN);
        DECOR_BLOCKS.add(Blocks.POTTED_DANDELION);
        DECOR_BLOCKS.add(Blocks.POTTED_POPPY);
        DECOR_BLOCKS.add(Blocks.POTTED_BLUE_ORCHID);
        DECOR_BLOCKS.add(Blocks.POTTED_ALLIUM);
        DECOR_BLOCKS.add(Blocks.POTTED_AZURE_BLUET);
        DECOR_BLOCKS.add(Blocks.POTTED_RED_TULIP);
        DECOR_BLOCKS.add(Blocks.POTTED_ORANGE_TULIP);
        DECOR_BLOCKS.add(Blocks.POTTED_WHITE_TULIP);
        DECOR_BLOCKS.add(Blocks.POTTED_PINK_TULIP);
        DECOR_BLOCKS.add(Blocks.POTTED_OXEYE_DAISY);
        DECOR_BLOCKS.add(Blocks.POTTED_CORNFLOWER);
        DECOR_BLOCKS.add(Blocks.POTTED_LILY_OF_THE_VALLEY);
        DECOR_BLOCKS.add(Blocks.POTTED_WITHER_ROSE);
        DECOR_BLOCKS.add(Blocks.POTTED_RED_MUSHROOM);
        DECOR_BLOCKS.add(Blocks.POTTED_BROWN_MUSHROOM);
        DECOR_BLOCKS.add(Blocks.POTTED_DEAD_BUSH);
        DECOR_BLOCKS.add(Blocks.POTTED_CACTUS);
        DECOR_BLOCKS.add(Blocks.POTTED_BAMBOO);
        DECOR_BLOCKS.add(Blocks.POTTED_AZALEA_BUSH);
        DECOR_BLOCKS.add(Blocks.POTTED_FLOWERING_AZALEA_BUSH);
        DECOR_BLOCKS.add(Blocks.POTTED_CRIMSON_FUNGUS);
        DECOR_BLOCKS.add(Blocks.POTTED_WARPED_FUNGUS);
        DECOR_BLOCKS.add(Blocks.POTTED_CRIMSON_ROOTS);
        DECOR_BLOCKS.add(Blocks.POTTED_WARPED_ROOTS);
        DECOR_BLOCKS.add(Blocks.POTTED_TORCHFLOWER);

        STRUCTURE_BLOCKS.add(Blocks.GLASS);
        STRUCTURE_BLOCKS.add(Blocks.GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.WHITE_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.ORANGE_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.MAGENTA_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.LIGHT_BLUE_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.YELLOW_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.LIME_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.PINK_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.GRAY_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.LIGHT_GRAY_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.CYAN_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.PURPLE_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.BLUE_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.BROWN_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.GREEN_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.RED_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.BLACK_STAINED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.WHITE_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.ORANGE_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.MAGENTA_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.YELLOW_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.LIME_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.PINK_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.GRAY_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.CYAN_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.PURPLE_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.BLUE_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.BROWN_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.GREEN_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.RED_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.BLACK_STAINED_GLASS_PANE);
        STRUCTURE_BLOCKS.add(Blocks.TINTED_GLASS);
        STRUCTURE_BLOCKS.add(Blocks.OAK_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.SPRUCE_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.BIRCH_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.JUNGLE_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.ACACIA_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.DARK_OAK_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.MANGROVE_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.CHERRY_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.BAMBOO_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.CRIMSON_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.WARPED_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.IRON_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.EXPOSED_COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.WEATHERED_COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.OXIDIZED_COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_EXPOSED_COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_WEATHERED_COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_OXIDIZED_COPPER_DOOR);
        STRUCTURE_BLOCKS.add(Blocks.OAK_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.SPRUCE_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.BIRCH_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.JUNGLE_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.ACACIA_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.DARK_OAK_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.MANGROVE_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.CHERRY_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.BAMBOO_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.CRIMSON_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.WARPED_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.IRON_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.COPPER_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.EXPOSED_COPPER_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.WEATHERED_COPPER_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.OXIDIZED_COPPER_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_COPPER_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
        STRUCTURE_BLOCKS.add(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
    }

    public static int calculateComfortLevel(ServerPlayerEntity player) {
        BlockPos bedPos = player.getSleepingPosition().orElse(player.getBlockPos());
        World world = player.getWorld();

        boolean hasLighting = false;
        boolean hasCarpet = false;
        boolean hasFurniture = false;
        boolean hasDecor = false;
        boolean hasStructure = false;

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    BlockPos checkPos = bedPos.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);
                    Block block = state.getBlock();

                    if (!hasLighting && LIGHTING_BLOCKS.contains(block)) {
                        hasLighting = true;
                    }
                    if (!hasCarpet && CARPET_BLOCKS.contains(block)) {
                        hasCarpet = true;
                    }
                    if (!hasFurniture && FURNITURE_BLOCKS.contains(block)) {
                        hasFurniture = true;
                    }
                    if (!hasDecor && DECOR_BLOCKS.contains(block)) {
                        hasDecor = true;
                    }
                    if (!hasStructure && STRUCTURE_BLOCKS.contains(block)) {
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