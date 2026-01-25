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
      super.field_70146_Z.setSeed(var1.func_72905_C() ^ this.func_145782_y());
      super.field_70178_ae = true;
      super.field_70138_W = 1.0F;
      super.field_70737_aN = 15;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(NetherOresCore.hellFishMaxHealth.getDouble(12.5));
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.925 + super.field_70146_Z.nextDouble() * 0.1);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(1.5 + super.field_70146_Z.nextDouble());
      this.func_110148_a(SharedMonsterAttributes.field_111266_c).func_111128_a(super.field_70146_Z.nextDouble());
   }

   protected void func_70626_be() {
      int var1 = super.field_70843_d;
      super.func_70626_be();
      super.field_70843_d = var1;
      if (!super.field_70170_p.field_72995_K) {
         if (super.field_70843_d > 0) {
            super.field_70843_d--;
            if (super.field_70843_d == 0) {
               int var2 = MathHelper.func_76128_c(super.field_70165_t);
               int var3 = MathHelper.func_76128_c(super.field_70163_u);
               int var4 = MathHelper.func_76128_c(super.field_70161_v);

               label112:
               for (int var5 = 0; var5 <= 5 & var5 >= -5; var5 = var5 <= 0 ? 1 - var5 : 0 - var5) {
                  for (int var6 = 0; var6 <= 10 & var6 >= -10; var6 = var6 <= 0 ? 1 - var6 : 0 - var6) {
                     for (int var7 = 0; var7 <= 10 & var7 >= -10; var7 = var7 <= 0 ? 1 - var7 : 0 - var7) {
                        Block var8 = super.field_70170_p.func_147439_a(var2 + var6, var3 + var5, var4 + var7);
                        if (var8 == NetherOresCore.blockHellfish) {
                           if (!super.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                              super.field_70170_p.func_147465_d(var2 + var6, var3 + var5, var4 + var7, Blocks.field_150424_aL, 0, 3);
                           } else {
                              super.field_70170_p.func_147480_a(var2 + var6, var3 + var5, var4 + var7, false);
                           }

                           BlockHellfish.spawnHellfish(super.field_70170_p, var2 + var6, var3 + var5, var4 + var7);
                           if (super.field_70146_Z.nextBoolean()) {
                              break label112;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (super.field_70789_a == null && !this.func_70781_l()) {
            int var9 = MathHelper.func_76128_c(super.field_70165_t);
            int var10 = MathHelper.func_76128_c(super.field_70163_u + 0.5);
            int var11 = MathHelper.func_76128_c(super.field_70161_v);
            int var12 = super.field_70146_Z.nextInt(6);
            Block var13 = super.field_70170_p
               .func_147439_a(var9 + Facing.field_71586_b[var12], var10 + Facing.field_71587_c[var12], var11 + Facing.field_71585_d[var12]);
            if (var13 == Blocks.field_150424_aL && super.field_70146_Z.nextInt(3) == 0) {
               super.field_70170_p
                  .func_147465_d(
                     var9 + Facing.field_71586_b[var12],
                     var10 + Facing.field_71587_c[var12],
                     var11 + Facing.field_71585_d[var12],
                     NetherOresCore.blockHellfish,
                     0,
                     0
                  );
               this.func_70656_aK();
               this.func_70106_y();
            } else {
               this.func_70779_j();
            }
         } else if (super.field_70789_a != null && !this.func_70781_l()) {
            super.field_70789_a = null;
         }
      }
   }

   public boolean func_70652_k(Entity var1) {
      var1.func_70015_d(3);
      return super.func_70652_k(var1);
   }

   public float func_70783_a(int var1, int var2, int var3) {
      float var4 = 0.0F;
      if (super.field_70170_p.func_147439_a(var1, var2 - 1, var3) == Blocks.field_150424_aL) {
         var4 = 10.0F;
      }

      return var4 + super.field_70170_p.func_72801_o(var1, var2, var3) - 0.5F;
   }
}
