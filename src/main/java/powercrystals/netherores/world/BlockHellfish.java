package powercrystals.netherores.world;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNetherrack;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.entity.EntityHellfish;
import powercrystals.netherores.net.ServerProxy;

public class BlockHellfish extends BlockNetherrack {
   public BlockHellfish() {
      this.setHardness(0.4F);
      this.setStepSound(Block.soundTypePiston);
      this.setBlockName("netherores.hellfish");
      this.setBlockTextureName("netherrack");
   }

   public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
      return var5 == ForgeDirection.UP;
   }

   public boolean canSilkHarvest() {
      return false;
   }

   public Item getItemDropped(int var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.netherrack);
   }

   public int quantityDropped(Random var1) {
      return NetherOresCore.enableHellfish.getBoolean(true) ? 0 : 1;
   }

   public void breakBlock(World var1, int var2, int var3, int var4, Block var5, int var6) {
      spawnHellfish(var1, var2, var3, var4);
      super.breakBlock(var1, var2, var3, var4, var5, var6);
   }

   public boolean isReplaceableOreGen(World var1, int var2, int var3, int var4, Block var5) {
      return this == var5 || Blocks.netherrack.isReplaceableOreGen(var1, var2, var3, var4, var5);
   }

   public static void spawnHellfish(World var0, int var1, int var2, int var3) {
      if (!var0.isRemote && !ServerProxy.isChunkPopulating(var0, var1, var2, var3) && NetherOresCore.enableHellfish.getBoolean(true)) {
         EntityHellfish var4 = new EntityHellfish(var0);
         var4.setLocationAndAngles(var1 + 0.5, var2, var3 + 0.5, 0.0F, 0.0F);
         var0.spawnEntityInWorld(var4);
         var4.spawnExplosionParticle();
      }
   }
}
