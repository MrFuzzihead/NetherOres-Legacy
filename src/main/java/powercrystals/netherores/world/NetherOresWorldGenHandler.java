package powercrystals.netherores.world;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import cofh.api.world.IFeatureGenerator;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.ores.Ores;

public class NetherOresWorldGenHandler implements IFeatureGenerator {

    private final String name = "NetherOres:WorldGen";

    public String getFeatureName() {
        return "NetherOres:WorldGen";
    }

    public boolean generateFeature(Random rand, int chunkX, int chunkZ, World world, boolean isNewChunk) {
        int dim = world.provider.dimensionId;
        boolean isNether = dim == -1;
        boolean isEnd = dim == 1 && NetherOresCore.enableEndOreGen.getBoolean(false);
        if (!isNether && !isEnd && !NetherOresCore.worldGenAllDimensions.getBoolean(false)) {
            return false;
        }
        if (isEnd) {
            generateEnd(world, rand, chunkX * 16, chunkZ * 16, isNewChunk);
        } else {
            generateNether(world, rand, chunkX * 16, chunkZ * 16, isNewChunk);
        }
        return true;
    }

    private void generateNether(World world, Random rand, int startX, int startZ, boolean isNewChunk) {
        if (NetherOresCore.enableWorldGen.getBoolean(true)) {
            for (Ores ore : Ores.values()) {
                if ((ore.getForced() || (ore.isRegisteredSmelting() || ore.isRegisteredMacerator()
                    || NetherOresCore.forceOreSpawn.getBoolean(false)) && !ore.getDisabled())
                    && (isNewChunk || ore.getRetroGen())) {
                    int groups = ore.getGroupsPerChunk();
                    while (groups-- > 0) {
                        int x = startX + rand.nextInt(16);
                        int y = ore.getMinY() + rand.nextInt(ore.getMaxY() - ore.getMinY());
                        int z = startZ + rand.nextInt(16);
                        new WorldGenNetherOres(
                            NetherOresCore.getOreBlock(ore.getBlockIndex()),
                            ore.getMetadata(),
                            ore.getBlocksPerGroup()).generate(world, rand, x, y, z);
                    }
                }
            }
        }

        if (NetherOresCore.enableHellfish.getBoolean(true)
            && (isNewChunk || !isNewChunk && NetherOresCore.hellFishRetrogen.getBoolean())) {
            int hellfishPerGroup = NetherOresCore.hellFishPerGroup.getInt();
            int hellfishMinY = NetherOresCore.hellFishMinY.getInt();
            int hellfishMaxY = NetherOresCore.hellFishMaxY.getInt();
            int hellfishPerChunk = NetherOresCore.hellFishPerChunk.getInt();

            while (hellfishPerChunk-- > 0) {
                int x = startX + rand.nextInt(16);
                int y = hellfishMinY + rand.nextInt(hellfishMaxY - hellfishMinY);
                int z = startZ + rand.nextInt(16);
                new WorldGenNetherOres(NetherOresCore.blockHellfish, 0, hellfishPerGroup)
                    .generate(world, rand, x, y, z);
            }
        }
    }

    // Mirrors generateNether but embeds the ores in end stone instead of netherrack.
    private void generateEnd(World world, Random rand, int startX, int startZ, boolean isNewChunk) {
        if (NetherOresCore.enableWorldGen.getBoolean(true)) {
            for (Ores ore : Ores.values()) {
                if ((ore.getForced() || (ore.isRegisteredSmelting() || ore.isRegisteredMacerator()
                    || NetherOresCore.forceOreSpawn.getBoolean(false)) && !ore.getDisabled())
                    && (isNewChunk || ore.getRetroGen())) {
                    int groups = ore.getGroupsPerChunk();
                    while (groups-- > 0) {
                        int x = startX + rand.nextInt(16);
                        // The End's main island is low (y < 64); keep ores well inside end stone.
                        int y = 2 + rand.nextInt(52);
                        int z = startZ + rand.nextInt(16);
                        new WorldGenNetherOres(
                            NetherOresCore.getOreBlock(ore.getBlockIndex()),
                            ore.getMetadata(),
                            ore.getBlocksPerGroup(),
                            Blocks.end_stone).generate(world, rand, x, y, z);
                    }
                }
            }
        }
    }
}
