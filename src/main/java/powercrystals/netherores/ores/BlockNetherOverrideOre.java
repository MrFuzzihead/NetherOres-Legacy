package powercrystals.netherores.ores;

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

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.world.BlockHellfish;

public class BlockNetherOverrideOre extends Block implements INetherOre {

    protected Block _override;
    private ThreadLocal<Boolean> calling = new ThreadLocal<>();
    private ThreadLocal<Boolean> explode = new ThreadLocal<>();
    private ThreadLocal<Boolean> willAnger = new ThreadLocal<>();

    public BlockNetherOverrideOre(Block var1) {
        super(var1.getMaterial());
        this._override = var1;
        this.setStepSound(var1.stepSound);
        ObfuscationReflectionHelper.setPrivateValue(
            ItemBlock.class,
            (ItemBlock) Item.getItemFromBlock(this._override),
            this,
            "field_150939_a");
    }

    public boolean isAssociatedBlock(Block var1) {
        return var1 == this | var1 == this._override || this._override.isAssociatedBlock(var1);
    }

    @Override
    public boolean equals(Object var1) {
        return var1 == this | var1 == this._override;
    }

    @Override
    public int hashCode() {
        return this._override.hashCode();
    }

    public CreativeTabs getCreativeTabToDisplayOn() {
        return this._override.getCreativeTabToDisplayOn();
    }

    public boolean canHarvestBlock(EntityPlayer var1, int var2) {
        return this._override.canHarvestBlock(var1, var2);
    }

    public Item getItemDropped(int var1, Random var2, int var3) {
        return this._override.getItemDropped(var1, var2, var3);
    }

    public int quantityDropped(Random var1) {
        return this._override.quantityDropped(var1);
    }

    public int quantityDroppedWithBonus(int var1, Random var2) {
        return this._override.quantityDroppedWithBonus(var1, var2);
    }

    public void dropBlockAsItemWithChance(World var1, int var2, int var3, int var4, int var5, float var6, int var7) {
        this._override.dropBlockAsItemWithChance(var1, var2, var3, var4, var5, var6, var7);
    }

    public int getExpDrop(IBlockAccess var1, int var2, int var3) {
        return this._override.getExpDrop(var1, var2, var3);
    }

    public void dropXpOnBlockBreak(World var1, int var2, int var3, int var4, int var5) {
        this._override.dropXpOnBlockBreak(var1, var2, var3, var4, var5);
    }

    public int damageDropped(int var1) {
        return this._override.damageDropped(var1);
    }

    public ArrayList<ItemStack> getDrops(World var1, int var2, int var3, int var4, int var5, int var6) {
        return this._override.getDrops(var1, var2, var3, var4, var5, var6);
    }

    public String getUnlocalizedName() {
        return this._override.getUnlocalizedName();
    }

    public String getLocalizedName() {
        return this._override.getLocalizedName();
    }

    public boolean getEnableStats() {
        return this._override.getEnableStats();
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149735_b(int var1, int var2) {
        return this._override.func_149735_b(var1, var2);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess var1, int var2, int var3, int var4, int var5) {
        return this._override.getIcon(var1, var2, var3, var4, var5);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int var1, int var2) {
        return this._override.getIcon(var1, var2);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister var1) {
        this._override.registerBlockIcons(var1);
    }

    @SideOnly(Side.CLIENT)
    public String getItemIconName() {
        return this._override.getItemIconName();
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

    public float getExplosionResistance(Entity var1, World var2, int var3, int var4, int var5, double var6, double var8,
        double var10) {
        return this._override.getExplosionResistance(var1, var2, var3, var4, var5, var6, var8, var10);
    }

    public void onBlockClicked(World var1, int var2, int var3, int var4, EntityPlayer var5) {
        this._override.onBlockClicked(var1, var2, var3, var4, var5);
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World var1, int var2, int var3, int var4, Random var5) {
        this._override.randomDisplayTick(var1, var2, var3, var4, var5);
    }

    public void onBlockDestroyedByPlayer(World var1, int var2, int var3, int var4, int var5) {
        this._override.onBlockDestroyedByPlayer(var1, var2, var3, var4, var5);
    }

    public float getBlockHardness(World var1, int var2, int var3, int var4) {
        return this._override.getBlockHardness(var1, var2, var3, var4);
    }

    public float getExplosionResistance(Entity var1) {
        return this._override.getExplosionResistance(var1);
    }

    public int tickRate(World var1) {
        return this._override.tickRate(var1);
    }

    public boolean getTickRandomly() {
        return this._override.getTickRandomly();
    }

    public void velocityToAddToEntity(World var1, int var2, int var3, int var4, Entity var5, Vec3 var6) {
        this._override.velocityToAddToEntity(var1, var2, var3, var4, var5, var6);
    }

    @SideOnly(Side.CLIENT)
    public int getMixedBrightnessForBlock(IBlockAccess var1, int var2, int var3, int var4) {
        return this._override.getMixedBrightnessForBlock(var1, var2, var3, var4);
    }

    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return this._override.getRenderBlockPass();
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World var1, int var2, int var3, int var4) {
        return this._override.getSelectedBoundingBoxFromPool(var1, var2, var3, var4);
    }

    public boolean isCollidable() {
        return this._override.isCollidable();
    }

    public boolean canCollideCheck(int var1, boolean var2) {
        return this._override.canCollideCheck(var1, var2);
    }

    public boolean canPlaceBlockAt(World var1, int var2, int var3, int var4) {
        return this._override.canPlaceBlockAt(var1, var2, var3, var4);
    }

    public void onBlockAdded(World var1, int var2, int var3, int var4) {
        this.onNeighborBlockChange(var1, var2, var3, var4, Blocks.air);
        this._override.onBlockAdded(var1, var2, var3, var4);
    }

    public void onEntityWalking(World var1, int var2, int var3, int var4, Entity var5) {
        this._override.onEntityWalking(var1, var2, var3, var4, var5);
    }

    public void onEntityCollidedWithBlock(World var1, int var2, int var3, int var4, Entity var5) {
        this._override.onEntityCollidedWithBlock(var1, var2, var3, var4, var5);
    }

    public void updateTick(World var1, int var2, int var3, int var4, Random var5) {
        this._override.updateTick(var1, var2, var3, var4, var5);
    }

    public boolean onBlockActivated(World var1, int var2, int var3, int var4, EntityPlayer var5, int var6, float var7,
        float var8, float var9) {
        return this._override.onBlockActivated(var1, var2, var3, var4, var5, 0, 0.0F, 0.0F, 0.0F);
    }

    public void onBlockDestroyedByExplosion(World var1, int var2, int var3, int var4, Explosion var5) {
        this._override.onBlockDestroyedByExplosion(var1, var2, var3, var4, var5);
    }

    public MapColor getMapColor(int var1) {
        return this._override.getMapColor(var1);
    }

    public int isProvidingWeakPower(IBlockAccess var1, int var2, int var3, int var4, int var5) {
        return this._override.isProvidingWeakPower(var1, var2, var3, var4, var5);
    }

    public boolean canProvidePower() {
        return this._override.canProvidePower();
    }

    public float getPlayerRelativeBlockHardness(EntityPlayer var1, World var2, int var3, int var4, int var5) {
        return this._override.getPlayerRelativeBlockHardness(var1, var2, var3, var4, var5);
    }

    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        return this._override.getBlockColor();
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(int var1) {
        return this._override.getRenderColor(var1);
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess var1, int var2, int var3, int var4) {
        return this._override.colorMultiplier(var1, var2, var3, var4);
    }

    public void harvestBlock(World var1, EntityPlayer var2, int var3, int var4, int var5, int var6) {
        if (this.calling.get() != Boolean.TRUE) {
            this.calling.set(Boolean.TRUE);
            this._override.harvestBlock(var1, var2, var3, var4, var5, var6);
            this.calling.remove();
        }
    }

    public int getLightValue(IBlockAccess var1, int var2, int var3, int var4) {
        if (this.calling.get() == Boolean.TRUE) {
            return this._override.getLightValue();
        } else {
            this.calling.set(Boolean.TRUE);
            int var5 = this._override.getLightValue(var1, var2, var3, var4);
            this.calling.remove();
            return var5;
        }
    }

    public boolean removedByPlayer(World var1, EntityPlayer var2, int var3, int var4, int var5, boolean var6) {
        boolean var7 = var2 == null || !EnchantmentHelper.getSilkTouchModifier(var2);
        this.explode.set(var7);
        this.willAnger.set(true);
        boolean var8 = this._override.removedByPlayer(var1, var2, var3, var4, var5, var6);
        if (var7 || NetherOresCore.silkyStopsPigmen.getBoolean(true)) {
            BlockNetherOres.angerPigmen(var2, var1, var3, var4, var5);
        }

        this.willAnger.set(false);
        this.explode.set(true);
        if (NetherOresCore.enableFortuneExplosions.getBoolean(true)) {
            int var9 = EnchantmentHelper.getFortuneModifier(var2);
            var9 = var9 > 0 ? var1.rand.nextInt(var9) : 0;

            while (var9-- > 0) {
                BlockNetherOres.checkExplosionChances(var1, var3, var4, var5);
            }
        }

        return var8;
    }

    public void breakBlock(World var1, int var2, int var3, int var4, Block var5, int var6) {
        if (this.explode.get() != Boolean.FALSE) {
            BlockNetherOres.checkExplosionChances(var1, var2, var3, var4);
        }

        if (this.willAnger.get() != Boolean.TRUE) {
            BlockNetherOres.angerPigmen(var1, var2, var3, var4);
        }

        if (NetherOresCore.hellFishFromOre.getBoolean(false)
            && var1.rand.nextInt(10000) < NetherOresCore.hellFishFromOreChance.getInt()) {
            BlockHellfish.spawnHellfish(var1, var2, var3, var4);
        }

        this._override.breakBlock(var1, var2, var3, var4, var5, var6);
    }

    public void onBlockExploded(World var1, int var2, int var3, int var4, Explosion var5) {
        this.explode.set(false);
        this.willAnger.set(
            NetherOresCore.enableMobsAngerPigmen.getBoolean(true) || var5 == null
                || !(var5.getExplosivePlacedBy() instanceof EntityLiving));
        this._override.onBlockExploded(var1, var2, var3, var4, var5);
        this.willAnger.set(true);
        this.explode.set(true);
        if (NetherOresCore.enableExplosionChainReactions.getBoolean(true)) {
            BlockNetherOres.checkExplosionChances(var1, var2, var3, var4);
        }
    }

    public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
        return var5 == ForgeDirection.UP;
    }
}
