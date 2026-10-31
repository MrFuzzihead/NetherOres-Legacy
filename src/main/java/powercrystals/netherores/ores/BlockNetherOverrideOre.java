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
import net.minecraft.entity.EntityLivingBase;
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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.mixins.early.ItemBlockMixin;
import powercrystals.netherores.world.BlockHellfish;

/**
 * A {@link Block} that transparently wraps another block ({@link #_override}) so it
 * behaves like a "NetherOre" while preserving the original block's behavior.
 * <p>
 * Delegation surface: behavior members (drop methods, harvest, light, activation, color,
 * sounds, states, collision/drops, etc.) forward to {@link #_override}; the NetherOre
 * break/explosion/anger behavior is layered on top in {@link #removedByPlayer},
 * {@link #breakBlock} and {@link #onBlockExploded}. When adding a forwarded method keep
 * it in the delegating style below and remember to forward ALL arguments (see the fixed
 * {@link #onBlockActivated}).
 */
public class BlockNetherOverrideOre extends Block implements INetherOre {

    protected Block _override;

    // State handed between removedByPlayer / onBlockExploded and the synchronous
    // breakBlock call. ThreadLocal so nested breaks on the same world thread cannot
    // clobber an outer break (see BlockNetherOres). Unset = default: explode true,
    // willAnger false. `calling` guards re-entrant light/harvest delegation.
    private final ThreadLocal<Boolean> calling = new ThreadLocal<>();
    private final ThreadLocal<Boolean> explode = new ThreadLocal<>();
    private final ThreadLocal<Boolean> willAnger = new ThreadLocal<>();

    public BlockNetherOverrideOre(Block var1) {
        super(var1.getMaterial());
        this._override = var1;
        this.setStepSound(var1.stepSound);
        // Only blocks that have an ItemBlock can be retargeted. Technical blocks (or
        // custom non-ItemBlock items) have nothing to place, so don't crash on them.
        Item item = Item.getItemFromBlock(this._override);
        if (item instanceof ItemBlock) {
            ((ItemBlockMixin) item).setBlockInstance(this);
        }
        this.copyBoundsFromOverride();
    }

    /** Copies the wrapped block's current bounds onto the wrapper for rendering/collision. */
    private void copyBoundsFromOverride() {
        this.setBlockBounds(
            (float) this._override.getBlockBoundsMinX(),
            (float) this._override.getBlockBoundsMinY(),
            (float) this._override.getBlockBoundsMinZ(),
            (float) this._override.getBlockBoundsMaxX(),
            (float) this._override.getBlockBoundsMaxY(),
            (float) this._override.getBlockBoundsMaxZ());
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

    /**
     * Forward neighbor updates to the wrapped block. {@link #onBlockAdded} explicitly
     * notifies a neighbor change, so without this delegation that call (and every world
     * neighbor update) hit the inherited no-op instead of the original block logic.
     */
    public void onNeighborBlockChange(World var1, int var2, int var3, int var4, Block var5) {
        this._override.onNeighborBlockChange(var1, var2, var3, var4, var5);
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

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        return this._override.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
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

    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        if (this.calling.get() != Boolean.TRUE) {
            this.calling.set(Boolean.TRUE);
            try {
                this._override.harvestBlock(world, player, x, y, z, meta);
            } finally {
                this.calling.remove();
            }
        }
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        if (this.calling.get() == Boolean.TRUE) {
            return this._override.getLightValue();
        }
        this.calling.set(Boolean.TRUE);
        try {
            return this._override.getLightValue(world, x, y, z);
        } finally {
            this.calling.remove();
        }
    }

    public int getLightValue() {
        return this._override.getLightValue();
    }

    public int getDamageValue(World world, int x, int y, int z) {
        return this._override.getDamageValue(world, x, y, z);
    }

    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean isHarvest) {
        boolean notSilk = player == null || !EnchantmentHelper.getSilkTouchModifier(player);
        this.explode.set(notSilk);
        this.willAnger.set(Boolean.TRUE);
        final boolean removed;
        try {
            removed = this._override.removedByPlayer(world, player, x, y, z, isHarvest);
        } finally {
            this.willAnger.remove();
            this.explode.remove();
        }

        if (notSilk || NetherOresCore.silkyAngersPigmen.getBoolean(false)) {
            BlockNetherOres.angerPigmen(player, world, x, y, z);
        }

        if (NetherOresCore.enableFortuneExplosions.getBoolean(true)) {
            int fortune = EnchantmentHelper.getFortuneModifier(player);
            fortune = fortune > 0 ? world.rand.nextInt(fortune) : 0;
            while (fortune-- > 0) {
                BlockNetherOres.checkExplosionChances(world, x, y, z);
            }
        }
        return removed;
    }

    public void breakBlock(World world, int x, int y, int z, Block blockType, int metadata) {
        if (this.explode.get() != Boolean.FALSE) {
            BlockNetherOres.checkExplosionChances(world, x, y, z);
        }

        if (this.willAnger.get() != Boolean.TRUE) {
            BlockNetherOres.angerPigmen(world, x, y, z);
        }

        if (NetherOresCore.hellFishFromOre.getBoolean(false)
            && world.rand.nextInt(10000) < NetherOresCore.hellFishFromOreChance.getInt()) {
            BlockHellfish.spawnHellfish(world, x, y, z);
        }

        this._override.breakBlock(world, x, y, z, blockType, metadata);
    }

    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
        this.explode.set(Boolean.FALSE);
        this.willAnger.set(
            NetherOresCore.enableMobsAngerPigmen.getBoolean(true) || explosion == null
                || !(explosion.getExplosivePlacedBy() instanceof EntityLiving));
        try {
            this._override.onBlockExploded(world, x, y, z, explosion);
        } finally {
            this.willAnger.remove();
            this.explode.remove();
        }
        if (NetherOresCore.enableExplosionChainReactions.getBoolean(true)) {
            BlockNetherOres.checkExplosionChances(world, x, y, z);
        }
    }

    // ---- Rendering / lighting / culling ------------------------------------------------
    // Without these the wrapper kept Block's defaults (full opaque cube, render type 0,
    // no ambient occlusion, stale bounds), so transparent, non-cube or variable-bounds
    // overridden blocks rendered and lit incorrectly.

    public int getRenderType() {
        return this._override.getRenderType();
    }

    public boolean isOpaqueCube() {
        return this._override.isOpaqueCube();
    }

    public boolean renderAsNormalBlock() {
        return this._override.renderAsNormalBlock();
    }

    public boolean isNormalCube() {
        return this._override.isNormalCube();
    }

    public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
        return this._override.isNormalCube(world, x, y, z);
    }

    public int getLightOpacity() {
        return this._override.getLightOpacity();
    }

    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        return this._override.getLightOpacity(world, x, y, z);
    }

    public boolean getUseNeighborBrightness() {
        return this._override.getUseNeighborBrightness();
    }

    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return this._override.shouldSideBeRendered(world, x, y, z, side);
    }

    public float getAmbientOcclusionLightValue() {
        return this._override.getAmbientOcclusionLightValue();
    }

    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this._override.setBlockBoundsBasedOnState(world, x, y, z);
        // Rendering reads the bounds off the block in the world (the wrapper), so copy
        // the wrapped block's freshly computed bounds across.
        this.copyBoundsFromOverride();
    }

    public void setBlockBoundsForItemRender() {
        this._override.setBlockBoundsForItemRender();
        this.copyBoundsFromOverride();
    }

    // ---- Collision / shape / harvest ---------------------------------------------------

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return this._override.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    public String getHarvestTool(int metadata) {
        return this._override.getHarvestTool(metadata);
    }

    public int getHarvestLevel(int metadata) {
        return this._override.getHarvestLevel(metadata);
    }

    public boolean isToolEffective(String type, int metadata) {
        return this._override.isToolEffective(type, metadata);
    }

    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return this._override.isSideSolid(world, x, y, z, side);
    }

    public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
        return this._override.isReplaceable(world, x, y, z);
    }

    // ---- Placement / redstone / pistons ------------------------------------------------

    public int onBlockPlaced(World world, int x, int y, int z, int side, float subX, float subY, float subZ,
        int metadata) {
        return this._override.onBlockPlaced(world, x, y, z, side, subX, subY, subZ, metadata);
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        this._override.onBlockPlacedBy(world, x, y, z, placer, stack);
    }

    public void onBlockHarvested(World world, int x, int y, int z, int metadata, EntityPlayer player) {
        this._override.onBlockHarvested(world, x, y, z, metadata, player);
    }

    public void onBlockPreDestroy(World world, int x, int y, int z, int metadata) {
        this._override.onBlockPreDestroy(world, x, y, z, metadata);
    }

    public void fillWithRain(World world, int x, int y, int z) {
        this._override.fillWithRain(world, x, y, z);
    }

    public void onFallenUpon(World world, int x, int y, int z, Entity entity, float fallDistance) {
        this._override.onFallenUpon(world, x, y, z, entity, fallDistance);
    }

    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        this._override.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
    }

    public int getMobilityFlag() {
        return this._override.getMobilityFlag();
    }

    public boolean hasComparatorInputOverride() {
        return this._override.hasComparatorInputOverride();
    }

    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return this._override.getComparatorInputOverride(world, x, y, z, side);
    }

    public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
        return var5 == ForgeDirection.UP;
    }
}
