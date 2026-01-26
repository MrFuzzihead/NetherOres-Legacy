package powercrystals.netherores.world;

import cofh.api.world.IFeatureGenerator;
import java.util.Random;
import net.minecraft.world.World;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.ores.Ores;

public class NetherOresWorldGenHandler implements IFeatureGenerator {
   private final String name = "NetherOres:WorldGen";

   public String getFeatureName() {
      return "NetherOres:WorldGen";
   }

   public boolean generateFeature(Random var1, int var2, int var3, World var4, boolean var5) {
      if (var4.provider.dimensionId != -1 && !NetherOresCore.worldGenAllDimensions.getBoolean(false)) {
         return false;
      } else {
         this.generateNether(var4, var1, var2 * 16, var3 * 16, var5);
         return true;
      }
   }

   private void generateNether(World var1, Random var2, int var3, int var4, boolean var5) {
      if (NetherOresCore.enableWorldGen.getBoolean(true)) {
         for (Ores var9 : Ores.values()) {
            if ((
                  var9.getForced()
                     || (var9.isRegisteredSmelting() || var9.isRegisteredMacerator() || NetherOresCore.forceOreSpawn.getBoolean(false)) && !var9.getDisabled()
               )
               && (var5 || var9.getRetroGen())) {
               int var10 = var9.getGroupsPerChunk();

               while (var10-- > 0) {
                  int var11 = var3 + var2.nextInt(16);
                  int var12 = var9.getMinY() + var2.nextInt(var9.getMaxY() - var9.getMinY());
                  int var13 = var4 + var2.nextInt(16);
                  new WorldGenNetherOres(NetherOresCore.getOreBlock(var9.getBlockIndex()), var9.getMetadata(), var9.getBlocksPerGroup())
                     .generate(var1, var2, var11, var12, var13);
               }
            }
         }
      }

      if (NetherOresCore.enableHellfish.getBoolean(true) && (var5 || !var5 && NetherOresCore.hellFishRetrogen.getBoolean())) {
         int var14 = NetherOresCore.hellFishPerGroup.getInt();
         int var15 = NetherOresCore.hellFishMinY.getInt();
         int var16 = NetherOresCore.hellFishMaxY.getInt();
         int var17 = NetherOresCore.hellFishPerChunk.getInt();

         while (var17-- > 0) {
            int var18 = var3 + var2.nextInt(16);
            int var19 = var15 + var2.nextInt(var16 - var15);
            int var20 = var4 + var2.nextInt(16);
            new WorldGenNetherOres(NetherOresCore.blockHellfish, 0, var14).generate(var1, var2, var18, var19, var20);
         }
      }
   }
}
