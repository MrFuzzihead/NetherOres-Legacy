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
      super.noClip = true;
      super.preventEntitySpawning = false;
      this.setSize(0.0F, 0.0F);
      super.yOffset = super.height / 2.0F;
   }

   public EntityArmedOre(World var1, double var2, double var4, double var6) {
      this(var1, var2, var4, var6, null);
   }

   public EntityArmedOre(World var1, double var2, double var4, double var6, Block var8) {
       this(var1);
       this.setPosition(var2, var4, var6);
       super.motionX = (double)0.0F;
       super.motionY = (double)0.0F;
       super.motionZ = (double)0.0F;
       this._fuse = 80;
       super.prevPosX = var2;
       super.prevPosY = var4;
       super.prevPosZ = var6;
       this._target = var8;
       if (this._target != null) {
           this.setAir(Block.getIdFromBlock(this._target));
       } else {
           this.setAir(-1);
       }
   }

   protected void entityInit() {
   }

   public boolean canTriggerWalking() {
      return false;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public void onUpdate() {
      if (this._fuse-- <= 0) {
         this.setDead();
         if (!super.worldObj.isRemote) {
            this.setInvisible(true);
            this.explode();
         }
      } else if (super.worldObj.isRemote) {
         if (this.isInvisible()) {
            this.setDead();
         }

         Block var1 = super.worldObj
            .getBlock(
               MathHelper.floor_double(super.posX), MathHelper.floor_double(super.posY), MathHelper.floor_double(super.posZ)
            );
         if (Block.getIdFromBlock(var1) == this.getAir()) {
            super.worldObj.spawnParticle("smoke", super.posX, super.posY + 0.5, super.posZ, 0.0, 0.0, 0.0);
         }
      }
   }

   private void explode() {
      Block var1 = super.worldObj
         .getBlock(
            MathHelper.floor_double(super.posX), MathHelper.floor_double(super.posY), MathHelper.floor_double(super.posZ)
         );
      if (var1 == this._target) {
         super.worldObj
            .newExplosion(null, super.posX, super.posY, super.posZ, NetherOresCore.explosionPower.getInt(), true, true);
      }
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      var1.setByte("Fuse", (byte)this._fuse);
      var1.setString("STarget", Block.blockRegistry.getNameForObject(this._target));
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      this._fuse = var1.getByte("Fuse");
      this._target = Block.getBlockFromName(var1.hasKey("Target") ? Integer.toString(var1.getInteger("Target")) : var1.getString("STarget"));
      this.setAir(Block.getIdFromBlock(this._target));
   }

   @SideOnly(Side.CLIENT)
   public float getShadowSize() {
      return 0.0F;
   }
}
