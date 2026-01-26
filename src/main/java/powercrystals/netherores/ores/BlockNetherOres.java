package powercrystals.netherores.ores;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.entity.EntityArmedOre;
import powercrystals.netherores.gui.NOCreativeTab;
import powercrystals.netherores.world.BlockHellfish;

public class BlockNetherOres extends Block implements INetherOre {

    private static int _aggroRange = 32;
    private int _blockIndex = 0;
    private IIcon[] _netherOresIcons = new IIcon[16];
    private ThreadLocal<Boolean> explode = new ThreadLocal<>();
    private ThreadLocal<Boolean> willAnger = new ThreadLocal<>();

    public BlockNetherOres(int var1) {
        super(Blocks.netherrack.getMaterial());
        this.setHardness(5.0F);
        this.setResistance(1.0F);
        this.setBlockName("netherores.ore." + var1);
        this.setStepSound(Block.soundTypeStone);
        this.setCreativeTab(NOCreativeTab.tab);
        this._blockIndex = var1;
    }

    public int getBlockIndex() {
        return this._blockIndex;
    }

    public void registerBlockIcons(IIconRegister var1) {
        Ores[] var2 = Ores.values();
        int var3 = this._blockIndex * 16;
        int var4 = 0;

        for (int var5 = Math.min(var3 + 15, var2.length - 1) % 16; var4 <= var5; var4++) {
            this._netherOresIcons[var4] = var1.registerIcon("netherores:" + var2[var3 + var4].name());
        }
    }

    public IIcon getIcon(int var1, int var2) {
        return this._netherOresIcons[var2];
    }

    public int damageDropped(int var1) {
        return var1;
    }

    public int quantityDropped(Random var1) {
        return 1;
    }

    public boolean removedByPlayer(World var1, EntityPlayer var2, int var3, int var4, int var5, boolean var6) {
        boolean var7 = var2 == null || !EnchantmentHelper.getSilkTouchModifier(var2);
        this.explode.set(var7);
        this.willAnger.set(true);
        boolean var8 = super.removedByPlayer(var1, var2, var3, var4, var5, var6);
        if (var7 || NetherOresCore.silkyStopsPigmen.getBoolean(true)) {
            angerPigmen(var2, var1, var3, var4, var5);
        }

        this.willAnger.set(false);
        this.explode.set(true);
        if (NetherOresCore.enableFortuneExplosions.getBoolean(true)) {
            int var9 = EnchantmentHelper.getFortuneModifier(var2);
            var9 = var9 > 0 ? var1.rand.nextInt(var9) : 0;

            while (var9-- > 0) {
                checkExplosionChances(this, var1, var3, var4, var5);
            }
        }

        return var8;
    }

    public void breakBlock(World var1, int var2, int var3, int var4, Block var5, int var6) {
        if (this.explode.get() != Boolean.FALSE) {
            checkExplosionChances(this, var1, var2, var3, var4);
        }

        if (this.willAnger.get() != Boolean.TRUE) {
            angerPigmen(var1, var2, var3, var4);
        }

        if (NetherOresCore.hellFishFromOre.getBoolean(false)
            && var1.rand.nextInt(10000) < NetherOresCore.hellFishFromOreChance.getInt()) {
            BlockHellfish.spawnHellfish(var1, var2, var3, var4);
        }

        super.breakBlock(var1, var2, var3, var4, var5, var6);
    }

    public void onBlockExploded(World var1, int var2, int var3, int var4, Explosion var5) {
        this.explode.set(false);
        this.willAnger.set(
            NetherOresCore.enableMobsAngerPigmen.getBoolean(true) || var5 == null
                || !(var5.getExplosivePlacedBy() instanceof EntityLiving));
        super.onBlockExploded(var1, var2, var3, var4, var5);
        this.willAnger.set(true);
        this.explode.set(true);
        if (NetherOresCore.enableExplosionChainReactions.getBoolean(true)) {
            checkExplosionChances(this, var1, var2, var3, var4);
        }
    }

    public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
        return var5 == ForgeDirection.UP;
    }

    public static void checkExplosionChances(Block var0, World var1, int var2, int var3, int var4) {
        if (!var1.isRemote && NetherOresCore.enableExplosions.getBoolean(true)) {
            for (int var5 = -1; var5 <= 1; var5++) {
                for (int var6 = -1; var6 <= 1; var6++) {
                    for (int var7 = -1; var7 <= 1; var7++) {
                        if ((var5 | var6 | var7) != 0) {
                            int var8 = var2 + var5;
                            int var9 = var3 + var6;
                            int var10 = var4 + var7;
                            var0 = var1.getBlock(var8, var9, var10);
                            if (var0 instanceof INetherOre
                                && var1.rand.nextInt(1000) < NetherOresCore.explosionProbability.getInt()) {
                                EntityArmedOre var11 = new EntityArmedOre(
                                    var1,
                                    var8 + 0.5,
                                    var9 + 0.5,
                                    var10 + 0.5,
                                    var0);
                                var1.spawnEntityInWorld(var11);
                                var1.playSoundEffect(var2 + 0.5, var3 + 0.5, var4 + 0.5, "game.tnt.primed", 1.0F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void angerPigmen(EntityPlayer var0, World var1, int var2, int var3, int var4) {
        if (NetherOresCore.enableAngryPigmen.getBoolean(true)) {
            List var5 = var1.getEntitiesWithinAABB(
                EntityPigZombie.class,
                AxisAlignedBB.getBoundingBox(
                    var2 - _aggroRange,
                    var3 - _aggroRange,
                    var4 - _aggroRange,
                    var2 + _aggroRange + 1,
                    var3 + _aggroRange + 1,
                    var4 + _aggroRange + 1));

            for (int var6 = 0; var6 < var5.size(); var6++) {
                ((EntityPigZombie) var5.get(var6)).becomeAngryAt(var0);
            }
        }
    }

    public static void angerPigmen(World var0, int var1, int var2, int var3) {
        angerPigmen(null, var0, var1, var2, var3);
    }
}
