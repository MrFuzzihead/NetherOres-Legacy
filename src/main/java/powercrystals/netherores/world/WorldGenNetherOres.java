package powercrystals.netherores.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenNetherOres extends WorldGenerator {

    private Block _minableBlock;
    private int _minableBlockMeta;
    private int _numberOfBlocks;

    public WorldGenNetherOres(Block var1, int var2, int var3) {
        this._minableBlock = var1;
        this._minableBlockMeta = var2;
        this._numberOfBlocks = var3;
    }

    public boolean generate(World var1, Random var2, int var3, int var4, int var5) {
        int var6 = this._numberOfBlocks;
        float var7 = var2.nextFloat() * (float) Math.PI;
        double var8 = var4 + var2.nextInt(3) - 2;
        double var10 = var4 + var2.nextInt(3) - 2;
        if (var6 == 1 && var8 > var10) {
            var6++;
        }

        if (var6 == 2 && var7 > (float) (Math.PI / 2)) {
            var6++;
        }

        double var12 = var3 + 8 + MathHelper.sin(var7) * var6 / 8.0F;
        double var14 = var3 + 8 - MathHelper.sin(var7) * var6 / 8.0F;
        double var16 = var5 + 8 + MathHelper.cos(var7) * var6 / 8.0F;
        double var18 = var5 + 8 - MathHelper.cos(var7) * var6 / 8.0F;

        for (int var20 = 0; var20 <= var6; var20++) {
            double var21 = var12 + (var14 - var12) * var20 / var6;
            double var23 = var8 + (var10 - var8) * var20 / var6;
            double var25 = var16 + (var18 - var16) * var20 / var6;
            double var27 = var2.nextDouble() * var6 / 16.0;
            double var29 = (MathHelper.sin(var20 * 3.141593F / var6) + 1.0F) * var27 + 1.0;
            double var31 = (MathHelper.sin(var20 * 3.141593F / var6) + 1.0F) * var27 + 1.0;
            int var33 = MathHelper.floor_double(var21 - var29 / 2.0);
            int var34 = MathHelper.floor_double(var23 - var31 / 2.0);
            int var35 = MathHelper.floor_double(var25 - var29 / 2.0);
            int var36 = MathHelper.floor_double(var21 + var29 / 2.0);
            int var37 = MathHelper.floor_double(var23 + var31 / 2.0);
            int var38 = MathHelper.floor_double(var25 + var29 / 2.0);

            for (int var39 = var33; var39 <= var36; var39++) {
                double var40 = (var39 + 0.5 - var21) / (var29 / 2.0);
                if (!(var40 * var40 >= 1.0)) {
                    for (int var42 = var34; var42 <= var37; var42++) {
                        double var43 = (var42 + 0.5 - var23) / (var31 / 2.0);
                        if (!(var40 * var40 + var43 * var43 >= 1.0)) {
                            for (int var45 = var35; var45 <= var38; var45++) {
                                double var46 = (var45 + 0.5 - var25) / (var29 / 2.0);
                                Block var48 = var1.getBlock(var39, var42, var45);
                                if (var48 != null && var40 * var40 + var43 * var43 + var46 * var46 < 1.0
                                    && var48.isReplaceableOreGen(var1, var39, var42, var45, Blocks.netherrack)) {
                                    var1.setBlock(var39, var42, var45, this._minableBlock, this._minableBlockMeta, 2);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
