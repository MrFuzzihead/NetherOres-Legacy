package powercrystals.netherores.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import powercrystals.netherores.NetherOresCore;

public class NOCreativeTab extends CreativeTabs {
   public static final NOCreativeTab tab = new NOCreativeTab("Nether Ores");

   public NOCreativeTab(String var1) {
      super(var1);
   }

   public String func_78024_c() {
      return this.func_78013_b();
   }

   @SideOnly(Side.CLIENT)
   public Item func_78016_d() {
      return Item.func_150898_a(NetherOresCore.blockNetherOres[0]);
   }

   @SideOnly(Side.CLIENT)
   public int func_151243_f() {
      return 1;
   }
}
