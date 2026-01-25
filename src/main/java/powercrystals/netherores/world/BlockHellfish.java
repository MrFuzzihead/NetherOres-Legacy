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
      this.func_149711_c(0.4F);
      this.func_149672_a(Block.field_149780_i);
      this.func_149663_c("netherores.hellfish");
      this.func_149658_d("netherrack");
   }

   public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
      return var5 == ForgeDirection.UP;
   }

   public boolean func_149700_E() {
      return false;
   }

   public Item func_149650_a(int var1, Random var2, int var3) {
      return Item.func_150898_a(Blocks.field_150424_aL);
   }

   public int func_149745_a(Random var1) {
      return NetherOresCore.enableHellfish.getBoolean(true) ? 0 : 1;
   }

   public void func_149749_a(World var1, int var2, int var3, int var4, Block var5, int var6) {
      spawnHellfish(var1, var2, var3, var4);
      super.func_149749_a(var1, var2, var3, var4, var5, var6);
   }

   public boolean isReplaceableOreGen(World var1, int var2, int var3, int var4, Block var5) {
      return this == var5 || Blocks.field_150424_aL.isReplaceableOreGen(var1, var2, var3, var4, var5);
   }

   public static void spawnHellfish(World var0, int var1, int var2, int var3) {
      if (!var0.field_72995_K && !ServerProxy.isChunkPopulating(var0, var1, var2, var3) && NetherOresCore.enableHellfish.getBoolean(true)) {
         EntityHellfish var4 = new EntityHellfish(var0);
         var4.func_70012_b(var1 + 0.5, var2, var3 + 0.5, 0.0F, 0.0F);
         var0.func_72838_d(var4);
         var4.func_70656_aK();
      }
   }
}
