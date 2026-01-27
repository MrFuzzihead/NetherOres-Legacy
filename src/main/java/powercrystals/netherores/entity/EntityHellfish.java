package powercrystals.netherores.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.world.BlockHellfish;

public class EntityHellfish extends EntitySilverfish {

    public EntityHellfish(World var1) {
        super(var1);
        super.rand.setSeed(var1.getSeed() ^ this.getEntityId());
        super.isImmuneToFire = true;
        super.stepHeight = 1.0F;
        super.hurtTime = 15;
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth)
            .setBaseValue(NetherOresCore.hellFishMaxHealth.getDouble(12.5));
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
            .setBaseValue(0.925 + super.rand.nextDouble() * 0.1);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage)
            .setBaseValue(1.5 + super.rand.nextDouble());
        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance)
            .setBaseValue(super.rand.nextDouble());
    }

    protected void updateEntityActionState() {
        int var1 = super.allySummonCooldown;
        super.updateEntityActionState();
        super.allySummonCooldown = var1;
        if (!super.worldObj.isRemote) {
            if (super.allySummonCooldown > 0) {
                super.allySummonCooldown--;
                if (super.allySummonCooldown == 0) {
                    int var2 = MathHelper.floor_double(super.posX);
                    int var3 = MathHelper.floor_double(super.posY);
                    int var4 = MathHelper.floor_double(super.posZ);

                    label112: for (int var5 = 0; var5 <= 5 & var5 >= -5; var5 = var5 <= 0 ? 1 - var5 : -var5) {
                        for (int var6 = 0; var6 <= 10 & var6 >= -10; var6 = var6 <= 0 ? 1 - var6 : -var6) {
                            for (int var7 = 0; var7 <= 10 & var7 >= -10; var7 = var7 <= 0 ? 1 - var7 : -var7) {
                                Block var8 = super.worldObj.getBlock(var2 + var6, var3 + var5, var4 + var7);
                                if (var8 == NetherOresCore.blockHellfish) {
                                    if (!super.worldObj.getGameRules()
                                        .getGameRuleBooleanValue("mobGriefing")) {
                                        super.worldObj
                                            .setBlock(var2 + var6, var3 + var5, var4 + var7, Blocks.netherrack, 0, 3);
                                    } else {
                                        super.worldObj.func_147480_a(var2 + var6, var3 + var5, var4 + var7, false);
                                    }

                                    BlockHellfish.spawnHellfish(super.worldObj, var2 + var6, var3 + var5, var4 + var7);
                                    if (super.rand.nextBoolean()) {
                                        break label112;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (super.entityToAttack == null && !this.hasPath()) {
                int var9 = MathHelper.floor_double(super.posX);
                int var10 = MathHelper.floor_double(super.posY + 0.5);
                int var11 = MathHelper.floor_double(super.posZ);
                int var12 = super.rand.nextInt(6);
                Block var13 = super.worldObj.getBlock(
                    var9 + Facing.offsetsXForSide[var12],
                    var10 + Facing.offsetsYForSide[var12],
                    var11 + Facing.offsetsZForSide[var12]);
                if (var13 == Blocks.netherrack && super.rand.nextInt(3) == 0) {
                    super.worldObj.setBlock(
                        var9 + Facing.offsetsXForSide[var12],
                        var10 + Facing.offsetsYForSide[var12],
                        var11 + Facing.offsetsZForSide[var12],
                        NetherOresCore.blockHellfish,
                        0,
                        0);
                    this.spawnExplosionParticle();
                    this.setDead();
                } else {
                    this.updateWanderPath();
                }
            } else if (super.entityToAttack != null && !this.hasPath()) {
                super.entityToAttack = null;
            }
        }
    }

    public boolean attackEntityAsMob(Entity var1) {
        var1.setFire(3);
        return super.attackEntityAsMob(var1);
    }

    public float getBlockPathWeight(int var1, int var2, int var3) {
        float var4 = 0.0F;
        if (super.worldObj.getBlock(var1, var2 - 1, var3) == Blocks.netherrack) {
            var4 = 10.0F;
        }

        return var4 + super.worldObj.getLightBrightness(var1, var2, var3) - 0.5F;
    }
}
