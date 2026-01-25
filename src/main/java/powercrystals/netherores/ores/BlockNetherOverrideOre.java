package powercrystals.netherores.ores;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.world.BlockHellfish;

public class BlockNetherOverrideOre extends Block implements INetherOre {
   protected Block _override;
   private ThreadLocal<Boolean> calling = new ThreadLocal<>();
   private ThreadLocal<Boolean> explode = new ThreadLocal<>();
   private ThreadLocal<Boolean> willAnger = new ThreadLocal<>();

   public BlockNetherOverrideOre(Block var1) {
      super(var1.func_149688_o());
      this._override = var1;
      this.func_149672_a(var1.field_149762_H);
      ObfuscationReflectionHelper.setPrivateValue(ItemBlock.class, (ItemBlock)Item.func_150898_a(this._override), this, new String[]{"field_150939_a"});
   }

   public boolean func_149667_c(Block var1) {
      return var1 == this | var1 == this._override || this._override.func_149667_c(var1);
   }

   @Override
   public boolean equals(Object var1) {
      return var1 == this | var1 == this._override;
   }

   @Override
   public int hashCode() {
      return this._override.hashCode();
   }

   public CreativeTabs func_149708_J() {
      return this._override.func_149708_J();
   }

   public boolean canHarvestBlock(EntityPlayer var1, int var2) {
      return this._override.canHarvestBlock(var1, var2);
   }

   public Item func_149650_a(int var1, Random var2, int var3) {
      return this._override.func_149650_a(var1, var2, var3);
   }

   public int func_149745_a(Random var1) {
      return this._override.func_149745_a(var1);
   }

   public int func_149679_a(int var1, Random var2) {
      return this._override.func_149679_a(var1, var2);
   }

   public void func_149690_a(World var1, int var2, int var3, int var4, int var5, float var6, int var7) {
      this._override.func_149690_a(var1, var2, var3, var4, var5, var6, var7);
   }

   public int getExpDrop(IBlockAccess var1, int var2, int var3) {
      return this._override.getExpDrop(var1, var2, var3);
   }

   public void func_149657_c(World var1, int var2, int var3, int var4, int var5) {
      this._override.func_149657_c(var1, var2, var3, var4, var5);
   }

   public int func_149692_a(int var1) {
      return this._override.func_149692_a(var1);
   }

   public ArrayList<ItemStack> getDrops(World var1, int var2, int var3, int var4, int var5, int var6) {
      return this._override.getDrops(var1, var2, var3, var4, var5, var6);
   }

   public String func_149739_a() {
      return this._override.func_149739_a();
   }

   public String func_149732_F() {
      return this._override.func_149732_F();
   }

   public boolean func_149652_G() {
      return this._override.func_149652_G();
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149735_b(int var1, int var2) {
      return this._override.func_149735_b(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149673_e(IBlockAccess var1, int var2, int var3, int var4, int var5) {
      return this._override.func_149673_e(var1, var2, var3, var4, var5);
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int var1, int var2) {
      return this._override.func_149691_a(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister var1) {
      this._override.func_149651_a(var1);
   }

   @SideOnly(Side.CLIENT)
   public String func_149702_O() {
      return this._override.func_149702_O();
   }

   public boolean isBurning(IBlockAccess var1, int var2, int var3, int var4) {
      return this._override.isBurning(var1, var2, var3, var4);
   }

   public int getFlammability(IBlockAccess var1, int var2, int var3, int var4, ForgeDirection var5) {
      return this._override.getFlammability(var1, var2, var3, var4, var5);
   }

   public boolean isFlammable(IBlockAccess var1, int var2, int var3, int var4, ForgeDirection var5) {
      return this._override.isFlammable(var1, var2, var3, var4, var5);
   }

   public int getFireSpreadSpeed(IBlockAccess var1, int var2, int var3, int var4, ForgeDirection var5) {
      return this._override.getFireSpreadSpeed(var1, var2, var3, var4, var5);
   }

   public boolean canSilkHarvest(World var1, EntityPlayer var2, int var3, int var4, int var5, int var6) {
      return this._override.canSilkHarvest(var1, var2, var3, var4, var5, var6);
   }

   public float getExplosionResistance(Entity var1, World var2, int var3, int var4, int var5, double var6, double var8, double var10) {
      return this._override.getExplosionResistance(var1, var2, var3, var4, var5, var6, var8, var10);
   }

   public void func_149699_a(World var1, int var2, int var3, int var4, EntityPlayer var5) {
      this._override.func_149699_a(var1, var2, var3, var4, var5);
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World var1, int var2, int var3, int var4, Random var5) {
      this._override.func_149734_b(var1, var2, var3, var4, var5);
   }

   public void func_149664_b(World var1, int var2, int var3, int var4, int var5) {
      this._override.func_149664_b(var1, var2, var3, var4, var5);
   }

   public float func_149712_f(World var1, int var2, int var3, int var4) {
      return this._override.func_149712_f(var1, var2, var3, var4);
   }

   public float func_149638_a(Entity var1) {
      return this._override.func_149638_a(var1);
   }

   public int func_149738_a(World var1) {
      return this._override.func_149738_a(var1);
   }

   public boolean func_149653_t() {
      return this._override.func_149653_t();
   }

   public void func_149640_a(World var1, int var2, int var3, int var4, Entity var5, Vec3 var6) {
      this._override.func_149640_a(var1, var2, var3, var4, var5, var6);
   }

   @SideOnly(Side.CLIENT)
   public int func_149677_c(IBlockAccess var1, int var2, int var3, int var4) {
      return this._override.func_149677_c(var1, var2, var3, var4);
   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return this._override.func_149701_w();
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB func_149633_g(World var1, int var2, int var3, int var4) {
      return this._override.func_149633_g(var1, var2, var3, var4);
   }

   public boolean func_149703_v() {
      return this._override.func_149703_v();
   }

   public boolean func_149678_a(int var1, boolean var2) {
      return this._override.func_149678_a(var1, var2);
   }

   public boolean func_149742_c(World var1, int var2, int var3, int var4) {
      return this._override.func_149742_c(var1, var2, var3, var4);
   }

   public void func_149726_b(World var1, int var2, int var3, int var4) {
      this.func_149695_a(var1, var2, var3, var4, Blocks.field_150350_a);
      this._override.func_149726_b(var1, var2, var3, var4);
   }

   public void func_149724_b(World var1, int var2, int var3, int var4, Entity var5) {
      this._override.func_149724_b(var1, var2, var3, var4, var5);
   }

   public void func_149670_a(World var1, int var2, int var3, int var4, Entity var5) {
      this._override.func_149670_a(var1, var2, var3, var4, var5);
   }

   public void func_149674_a(World var1, int var2, int var3, int var4, Random var5) {
      this._override.func_149674_a(var1, var2, var3, var4, var5);
   }

   public boolean func_149727_a(World var1, int var2, int var3, int var4, EntityPlayer var5, int var6, float var7, float var8, float var9) {
      return this._override.func_149727_a(var1, var2, var3, var4, var5, 0, 0.0F, 0.0F, 0.0F);
   }

   public void func_149723_a(World var1, int var2, int var3, int var4, Explosion var5) {
      this._override.func_149723_a(var1, var2, var3, var4, var5);
   }

   public MapColor func_149728_f(int var1) {
      return this._override.func_149728_f(var1);
   }

   public int func_149709_b(IBlockAccess var1, int var2, int var3, int var4, int var5) {
      return this._override.func_149709_b(var1, var2, var3, var4, var5);
   }

   public boolean func_149744_f() {
      return this._override.func_149744_f();
   }

   public float func_149737_a(EntityPlayer var1, World var2, int var3, int var4, int var5) {
      return this._override.func_149737_a(var1, var2, var3, var4, var5);
   }

   @SideOnly(Side.CLIENT)
   public int func_149635_D() {
      return this._override.func_149635_D();
   }

   @SideOnly(Side.CLIENT)
   public int func_149741_i(int var1) {
      return this._override.func_149741_i(var1);
   }

   @SideOnly(Side.CLIENT)
   public int func_149720_d(IBlockAccess var1, int var2, int var3, int var4) {
      return this._override.func_149720_d(var1, var2, var3, var4);
   }

   public void func_149636_a(World var1, EntityPlayer var2, int var3, int var4, int var5, int var6) {
      if (this.calling.get() != Boolean.TRUE) {
         this.calling.set(Boolean.TRUE);
         this._override.func_149636_a(var1, var2, var3, var4, var5, var6);
         this.calling.set(null);
      }
   }

   public int getLightValue(IBlockAccess var1, int var2, int var3, int var4) {
      if (this.calling.get() == Boolean.TRUE) {
         return this._override.func_149750_m();
      } else {
         this.calling.set(Boolean.TRUE);
         int var5 = this._override.getLightValue(var1, var2, var3, var4);
         this.calling.set(null);
         return var5;
      }
   }

   public boolean removedByPlayer(World var1, EntityPlayer var2, int var3, int var4, int var5, boolean var6) {
      boolean var7 = var2 == null || !EnchantmentHelper.func_77502_d(var2);
      this.explode.set(var7);
      this.willAnger.set(true);
      boolean var8 = this._override.removedByPlayer(var1, var2, var3, var4, var5, var6);
      if (var7 || NetherOresCore.silkyStopsPigmen.getBoolean(true)) {
         BlockNetherOres.angerPigmen(var2, var1, var3, var4, var5);
      }

      this.willAnger.set(false);
      this.explode.set(true);
      if (NetherOresCore.enableFortuneExplosions.getBoolean(true)) {
         int var9 = EnchantmentHelper.func_77517_e(var2);
         var9 = var9 > 0 ? var1.field_73012_v.nextInt(var9) : 0;

         while (var9-- > 0) {
            BlockNetherOres.checkExplosionChances(this, var1, var3, var4, var5);
         }
      }

      return var8;
   }

   public void func_149749_a(World var1, int var2, int var3, int var4, Block var5, int var6) {
      if (this.explode.get() != Boolean.FALSE) {
         BlockNetherOres.checkExplosionChances(this, var1, var2, var3, var4);
      }

      if (this.willAnger.get() != Boolean.TRUE) {
         BlockNetherOres.angerPigmen(var1, var2, var3, var4);
      }

      if (NetherOresCore.hellFishFromOre.getBoolean(false) && var1.field_73012_v.nextInt(10000) < NetherOresCore.hellFishFromOreChance.getInt()) {
         BlockHellfish.spawnHellfish(var1, var2, var3, var4);
      }

      this._override.func_149749_a(var1, var2, var3, var4, var5, var6);
   }

   public void onBlockExploded(World var1, int var2, int var3, int var4, Explosion var5) {
      this.explode.set(false);
      this.willAnger.set(NetherOresCore.enableMobsAngerPigmen.getBoolean(true) || var5 == null || !(var5.func_94613_c() instanceof EntityLiving));
      this._override.onBlockExploded(var1, var2, var3, var4, var5);
      this.willAnger.set(true);
      this.explode.set(true);
      if (NetherOresCore.enableExplosionChainReactions.getBoolean(true)) {
         BlockNetherOres.checkExplosionChances(this, var1, var2, var3, var4);
      }
   }

   public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
      return var5 == ForgeDirection.UP;
   }
}
