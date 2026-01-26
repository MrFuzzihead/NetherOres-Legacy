package powercrystals.netherores.ores;

import java.util.List;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockNetherOre extends ItemBlock {
   protected BlockNetherOres _block;

   public ItemBlockNetherOre(Block var1) {
      super(var1);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this._block = (BlockNetherOres)var1;
   }

   public int getMetadata(int var1) {
      return var1;
   }

   public String getUnlocalizedName(ItemStack var1) {
      int var2 = this._block.getBlockIndex();
      Ores[] var3 = Ores.values();
      int var4 = Math.min(var2 * 16 + var1.getItemDamage(), var3.length - 1);
      return "tile.netherores.ore." + var3[var4].name().toLowerCase(Locale.US);
   }

   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      int var4 = this._block.getBlockIndex();
      Ores[] var5 = Ores.values();
      int var6 = 0;

      for (int var7 = Math.min(var4 * 16 + 15, var5.length - 1) % 16; var6 <= var7; var6++) {
         var3.add(new ItemStack(this._block, 1, var6));
      }
   }
}
