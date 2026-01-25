package powercrystals.netherores.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import powercrystals.netherores.NetherOresCore;

public class EntityArmedOre extends Entity {
   private int _fuse = 80;
   private Block _target;

   public EntityArmedOre(World var1) {
      super(var1);
      super.field_70145_X = true;
      super.field_70156_m = false;
      this.func_70105_a(0.0F, 0.0F);
      super.field_70129_M = super.field_70131_O / 2.0F;
   }

   public EntityArmedOre(World var1, double var2, double var4, double var6) {
      this(var1, var2, var4, var6, null);
   }

   public EntityArmedOre(World var1, double var2, double var4, double var6, Block var8) {
      this(var1);
      this.func_70107_b(var2, var4, var6);
      super.field_70159_w = 0.0;
      super.field_70181_x = 0.0;
      super.field_70179_y = 0.0;
      this._fuse = 80;
      super.field_70169_q = var2;
      super.field_70167_r = var4;
      super.field_70166_s = var6;
      this._target = var8;
      if (this._target != null) {
         this.func_70050_g(Block.func_149682_b(this._target));
      } else {
         this.func_70050_g(-1);
      }
   }

   protected void func_70088_a() {
   }

   public boolean func_70041_e_() {
      return false;
   }

   public boolean func_70067_L() {
      return false;
   }

   public void func_70071_h_() {
      if (this._fuse-- <= 0) {
         this.func_70106_y();
         if (!super.field_70170_p.field_72995_K) {
            this.func_82142_c(true);
            this.explode();
         }
      } else if (super.field_70170_p.field_72995_K) {
         if (this.func_82150_aj()) {
            this.func_70106_y();
         }

         Block var1 = super.field_70170_p
            .func_147439_a(
               MathHelper.func_76128_c(super.field_70165_t), MathHelper.func_76128_c(super.field_70163_u), MathHelper.func_76128_c(super.field_70161_v)
            );
         if (Block.func_149682_b(var1) == this.func_70086_ai()) {
            super.field_70170_p.func_72869_a("smoke", super.field_70165_t, super.field_70163_u + 0.5, super.field_70161_v, 0.0, 0.0, 0.0);
         }
      }
   }

   private void explode() {
      Block var1 = super.field_70170_p
         .func_147439_a(
            MathHelper.func_76128_c(super.field_70165_t), MathHelper.func_76128_c(super.field_70163_u), MathHelper.func_76128_c(super.field_70161_v)
         );
      if (var1 == this._target) {
         super.field_70170_p
            .func_72885_a(null, super.field_70165_t, super.field_70163_u, super.field_70161_v, NetherOresCore.explosionPower.getInt(), true, true);
      }
   }

   protected void func_70014_b(NBTTagCompound var1) {
      var1.func_74774_a("Fuse", (byte)this._fuse);
      var1.func_74778_a("STarget", Block.field_149771_c.func_148750_c(this._target));
   }

   protected void func_70037_a(NBTTagCompound var1) {
      this._fuse = var1.func_74771_c("Fuse");
      this._target = Block.func_149684_b(var1.func_74764_b("Target") ? Integer.toString(var1.func_74762_e("Target")) : var1.func_74779_i("STarget"));
      this.func_70050_g(Block.func_149682_b(this._target));
   }

   @SideOnly(Side.CLIENT)
   public float func_70053_R() {
      return 0.0F;
   }
}
