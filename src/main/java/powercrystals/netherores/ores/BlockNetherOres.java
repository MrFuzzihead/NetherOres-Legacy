package powercrystals.netherores.ores;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.entity.EntityArmedOre;
import powercrystals.netherores.gui.NOCreativeTab;
import powercrystals.netherores.mixins.early.EntityPigZombieMixin;
import powercrystals.netherores.world.BlockHellfish;

public class BlockNetherOres extends Block implements INetherOre {

    private int _blockIndex = 0;
    private final IIcon[] _netherOresIcons = new IIcon[16];

    // Handed from removedByPlayer / onBlockExploded to the synchronous breakBlock call.
    // ThreadLocal (rather than plain fields) so a nested break of this same block
    // instance on the same world thread cannot clobber an outer break's state.
    // An unset value means "default": explode = true, willAnger = false.
    private final ThreadLocal<Boolean> explode = new ThreadLocal<>();
    private final ThreadLocal<Boolean> willAnger = new ThreadLocal<>();

    // Fallback RNG for getExpDrop when the IBlockAccess is not a World (vanilla uses a
    // bare Random here too).
    private static final Random XP_RAND = new Random();

    private static final Ores[] ALL = Ores.values();

    // Raw-ore drop cache, keyed by the global ore index (blockIndex * 16 + metadata).
    // Resolution happens on the world thread and is single-pass (prefill + on-demand),
    // so a plain array with a resolved-flag avoids map and boxing overhead.
    private static final ItemStack[] rawCache = new ItemStack[ALL.length];
    private static final boolean[] rawCacheResolved = new boolean[ALL.length];

    private static volatile String[] preferredMods = { "etfuturum", "thermalfoundation", "projred|core", "thaumcraft" };
    private static volatile String[] preferredModsLower;

    static {
        preferredModsLower = new String[preferredMods.length];
        for (int i = 0; i < preferredMods.length; i++) {
            preferredModsLower[i] = preferredMods[i].toLowerCase();
        }
    }

    private static ItemStack choosePreferredFromOreDict(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) return null;
        for (int i = 0; i < preferredMods.length; i++) {
            String pref = preferredMods[i];
            if (pref == null || pref.isEmpty()) continue;
            String prefLower = preferredModsLower[i];
            for (ItemStack s : stacks) {
                if (s == null || s.getItem() == null) continue;
                try {
                    String registryName = Item.itemRegistry.getNameForObject(s.getItem());
                    if (registryName != null) {
                        String modId = registryName.contains(":") ? registryName.split(":", 2)[0] : registryName;
                        if (modId != null && (modId.equalsIgnoreCase(pref) || modId.toLowerCase()
                            .contains(prefLower))) {
                            return s.copy();
                        }
                    }
                } catch (Throwable t) {
                    // ignore and continue
                }
            }
        }
        ItemStack first = stacks.get(0);
        return first == null ? null : first.copy();
    }

    // Resolve raw item for a specific Ores value (no cache side effects).
    private static ItemStack resolveRawForOre(Ores ore) {
        if (ore == null) return null;

        String base = ore.name();

        // Direct IC2 lookup for Iridium shards when we know the exact registry name (no ore-dict entry).
        if (ore == Ores.Iridium) {
            try {
                ItemStack s = GameRegistry.findItemStack("IC2", "itemShardIridium", 1);
                if (s != null) return s.copy();
                // try lowercase mod id variant
                s = GameRegistry.findItemStack("ic2", "itemShardIridium", 1);
                if (s != null) return s.copy();
            } catch (Throwable ignored) {}
        }

        // No special-case direct mod lookups here; prefer items via the configurable OreDictionary preference list.

        // Build an ordered list of ore-dictionary keys to try for this ore
        List<String> keys = new ArrayList<>();
        switch (ore) {
            case Amber:
                keys.add("gemAmber");
                break;
            case Sulfur:
                keys.add("dustSulfur");
                break;
            case Saltpeter:
                keys.add("dustSaltpeter");
                break;
            case Ruby:
                keys.add("gemRuby");
                break;
            case Sapphire:
                keys.add("gemSapphire");
                break;
            case Peridot:
                keys.add("gemPeridot");
                break;
            case Nikolite:
                keys.add("dustElectrotine");
                break;
            default:
                // default raw fallback
                keys.add("raw" + base);
                break;
        }

        // Always also try the generic raw+base as a fallback (if not already added)
        String rawKey = "raw" + base;
        if (!keys.contains(rawKey)) keys.add(rawKey);

        // Try each key and select a preferred mod entry if available
        for (String key : keys) {
            try {
                List<ItemStack> stacks = OreDictionary.getOres(key);
                if (stacks != null && !stacks.isEmpty()) {
                    ItemStack chosen = choosePreferredFromOreDict(stacks);
                    if (chosen != null) return chosen;
                }
            } catch (Throwable ignored) {}
        }

        return null;
    }

    // Prefill the raw item cache for all known Ores to avoid first-hit overhead at runtime.
    public static void prefillRawCache() {
        for (Ores ore : ALL) {
            int index = ore.getBlockIndex() * 16 + ore.getMetadata();
            if (index < 0 || index >= ALL.length || rawCacheResolved[index]) {
                continue;
            }
            try {
                rawCache[index] = resolveRawForOre(ore);
            } catch (Throwable t) {
                rawCache[index] = null;
            }
            rawCacheResolved[index] = true;
        }
    }

    /**
     * Re-opens cache entries that previously resolved to "no item". Another mod may
     * register its OreDictionary entry after our postInit prefill (or after an earlier
     * negative lookup), and a stale negative cache would hide that item forever.
     */
    public static void invalidateUnresolvedRawCache() {
        for (int i = 0; i < rawCache.length; i++) {
            if (rawCache[i] == null) {
                rawCacheResolved[i] = false;
            }
        }
    }

    private static void clearRawCache() {
        for (int i = 0; i < rawCache.length; i++) {
            rawCache[i] = null;
            rawCacheResolved[i] = false;
        }
    }

    // Sets the preferred-mod ordering parsed once at config load (avoids re-parsing
    // the comma-separated config string on every ore-dict lookup).
    public static void setPreferredModOrder(String configValue) {
        String[] parts = DropMath.parsePreferredModOrder(configValue);
        if (parts == null) {
            return;
        }
        String[] lower = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            lower[i] = parts[i].toLowerCase();
        }
        preferredMods = parts;
        preferredModsLower = lower;
        // Cached stacks were chosen with the old preference list; re-resolve on demand.
        clearRawCache();
    }

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

    public void registerBlockIcons(IIconRegister register) {
        int start = this._blockIndex * 16;
        if (start >= ALL.length) {
            return;
        }
        int end = Math.min(start + 16, ALL.length);
        for (int i = 0; start + i < end; i++) {
            this._netherOresIcons[i] = register.registerIcon("netherores:" + ALL[start + i].name());
        }
    }

    public IIcon getIcon(int var1, int var2) {
        return this._netherOresIcons[var2];
    }

    /**
     * Damage of the dropped item. Our {@link #getDrops} builds the correct stack itself,
     * but automation mods that use the legacy {@code getItemDropped}/{@code damageDropped}
     * pair would otherwise get the ore metadata as the item damage (e.g. "emerald" with
     * damage 8). Fall back to the block metadata only when the ore block itself drops.
     */
    @Override
    public int damageDropped(int metadata) {
        ItemStack base = resolveBaseDropItem(metadata);
        return base != null ? base.getItemDamage() : metadata;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        int base = quantityDropped(rand);
        return base + fortuneBonus(fortune, rand);
    }

    @Override
    public int quantityDropped(int metadata, int fortune, Random rand) {
        Ores ore = oreAt(this._blockIndex, metadata);
        // When no item resolves, getDrops falls back to a single ore block; keep the
        // legacy quantity API consistent with that.
        if (ore == null || resolveBaseDropItem(metadata) == null) {
            return 1;
        }
        return DropMath.dropQuantity(ore.getDropCount(), ore.getDropSpread(), fortune, rand);
    }

    private static Ores oreAt(int blockIndex, int metadata) {
        int oreIndex = blockIndex * 16 + metadata;
        return oreIndex >= 0 && oreIndex < ALL.length ? ALL[oreIndex] : null;
    }

    private ItemStack findRawOreStack(int metadata) {
        int oreIndex = this._blockIndex * 16 + metadata;
        if (oreIndex < 0 || oreIndex >= ALL.length) {
            return null;
        }
        // Always retain the result (including null = "no ore-dict entry") so we never
        // re-resolve on every block break. Plain arrays are safe here: resolution happens
        // on the world thread and is single-pass (prefill + on-demand).
        if (!rawCacheResolved[oreIndex]) {
            try {
                rawCache[oreIndex] = resolveRawForOre(ALL[oreIndex]);
            } catch (Throwable t) {
                rawCache[oreIndex] = null;
            }
            rawCacheResolved[oreIndex] = true;
        }
        return rawCache[oreIndex];
    }

    // Return a vanilla Minecraft base-item for special ores (Coal, Diamond, Emerald, Lapis, Redstone).
    // Returns null for ores that shouldn't use vanilla base drops.
    private static ItemStack getVanillaBaseForSpecial(Ores ore) {
        if (ore == null) return null;
        return switch (ore) {
            case Coal -> new ItemStack(Items.coal, 1, 0);
            case Diamond -> new ItemStack(Items.diamond, 1);
            case Emerald -> new ItemStack(Items.emerald, 1);
            case Lapis ->
                // Lapis drops dye (blue) with damage 4 in 1.7.10
                new ItemStack(Items.dye, 1, 4);
            case Redstone -> new ItemStack(Items.redstone, 1);
            default -> null;
        };
    }

    // Resolves the base drop item for this ore: the special vanilla item
    // (Coal/Diamond/Emerald/Lapis/Redstone) if applicable, otherwise the
    // OreDictionary/raw item, or null when neither is available (callers fall back
    // to dropping the nether ore block itself). Shared by getItemDropped and getDrops.
    private ItemStack resolveBaseDropItem(int metadata) {
        Ores ore = oreAt(this._blockIndex, metadata);
        ItemStack vanilla = getVanillaBaseForSpecial(ore);
        if (vanilla != null) return vanilla;
        return findRawOreStack(metadata);
    }

    // Fortune bonus: clamped to [0, fortune] (nether ores never reduce drops).
    private static int fortuneBonus(int fortune, Random rand) {
        return DropMath.fortuneBonus(fortune, rand);
    }

    @Override
    public Item getItemDropped(int metadata, Random rand, int fortune) {
        ItemStack base = resolveBaseDropItem(metadata);
        if (base != null) return base.getItem();
        return Item.getItemFromBlock(this);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<>();
        Ores ore = oreAt(this._blockIndex, metadata);

        ItemStack base = resolveBaseDropItem(metadata);
        if (base != null) {
            int qty = ore != null ? DropMath.dropQuantity(ore.getDropCount(), ore.getDropSpread(), fortune, world.rand)
                : 1 + fortuneBonus(fortune, world.rand);
            // A large DropCount must not produce an oversized ItemStack; split it into
            // legal stacks just like a chest full of drops would.
            int maxStack = Math.max(1, base.getMaxStackSize());
            while (qty > 0) {
                ItemStack out = base.copy();
                out.stackSize = Math.min(qty, maxStack);
                ret.add(out);
                qty -= out.stackSize;
            }
            return ret;
        }

        // Fallback: no item could be resolved for this ore, so drop the nether ore
        // block itself exactly once (not affected by fortune or DropCount).
        ret.add(new ItemStack(Item.getItemFromBlock(this), 1, metadata));
        return ret;
    }

    /**
     * Silk Touch only returns the ore block when the ore opts in via
     * {@code Mining.Ores.<Ore>.RequireSilkTouch}. With the setting disabled, vanilla's
     * silk branch is bypassed and the configured item drops are produced instead.
     */
    @Override
    public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        Ores ore = oreAt(this._blockIndex, metadata);
        return ore == null || DropMath.allowsSilkHarvest(ore.isSilkTouchRequired());
    }

    @Override
    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        Ores ore = oreAt(this._blockIndex, metadata);
        if (ore == null) {
            return 0;
        }
        Random rand = world instanceof World ? ((World) world).rand : XP_RAND;
        return DropMath.experience(ore.getExp(), ore.getExpSpread(), rand);
    }

    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean isHarvest) {
        boolean notSilk = player == null || !EnchantmentHelper.getSilkTouchModifier(player);
        this.explode.set(notSilk);
        this.willAnger.set(Boolean.TRUE);
        final boolean removed;
        try {
            removed = super.removedByPlayer(world, player, x, y, z, isHarvest);
        } finally {
            this.willAnger.remove();
            this.explode.remove();
        }

        if (notSilk || NetherOresCore.silkyAngersPigmen.getBoolean(false)) {
            angerPigmen(player, world, x, y, z);
        }

        if (NetherOresCore.enableFortuneExplosions.getBoolean(true)) {
            int fortune = EnchantmentHelper.getFortuneModifier(player);
            fortune = fortune > 0 ? world.rand.nextInt(fortune) : 0;
            while (fortune-- > 0) {
                checkExplosionChances(world, x, y, z);
            }
        }
        return removed;
    }

    public void breakBlock(World world, int x, int y, int z, Block blockType, int metadata) {
        if (this.explode.get() != Boolean.FALSE) {
            checkExplosionChances(world, x, y, z);
        }

        if (this.willAnger.get() != Boolean.TRUE) {
            angerPigmen(world, x, y, z);
        }

        if (NetherOresCore.hellFishFromOre.getBoolean(false)
            && world.rand.nextInt(10000) < NetherOresCore.hellFishFromOreChance.getInt()) {
            BlockHellfish.spawnHellfish(world, x, y, z);
        }

        super.breakBlock(world, x, y, z, blockType, metadata);
    }

    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
        this.explode.set(Boolean.FALSE);
        this.willAnger.set(
            NetherOresCore.enableMobsAngerPigmen.getBoolean(true) || explosion == null
                || !(explosion.getExplosivePlacedBy() instanceof EntityLiving));
        try {
            super.onBlockExploded(world, x, y, z, explosion);
        } finally {
            this.willAnger.remove();
            this.explode.remove();
        }
        if (NetherOresCore.enableExplosionChainReactions.getBoolean(true)) {
            checkExplosionChances(world, x, y, z);
        }
    }

    public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
        return var5 == ForgeDirection.UP;
    }

    public static void checkExplosionChances(World world, int x, int y, int z) {
        if (!world.isRemote && NetherOresCore.enableExplosions.getBoolean(true)) {
            // Hoisted out of the 26-neighbor loop: Property#getInt parses the config
            // string on every call, and this runs on every single ore break.
            int probability = NetherOresCore.explosionProbability.getInt();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if ((dx | dy | dz) != 0) {
                            int nx = x + dx;
                            int ny = y + dy;
                            int nz = z + dz;
                            Block neighbor = world.getBlock(nx, ny, nz);
                            if (neighbor instanceof INetherOre && world.rand.nextInt(1000) < probability) {
                                EntityArmedOre armed = new EntityArmedOre(
                                    world,
                                    nx + 0.5,
                                    ny + 0.5,
                                    nz + 0.5,
                                    neighbor);
                                world.spawnEntityInWorld(armed);
                                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "game.tnt.primed", 1.0F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void angerPigmen(EntityPlayer player, World world, int x, int y, int z) {
        if (NetherOresCore.enableAngryPigmen.getBoolean(true)) {
            int range = Math.max(1, NetherOresCore.angryPigmenRange.getInt());
            List<EntityPigZombie> pigmen = world.getEntitiesWithinAABB(
                EntityPigZombie.class,
                AxisAlignedBB
                    .getBoundingBox(x - range, y - range, z - range, x + range + 1, y + range + 1, z + range + 1));

            for (EntityPigZombie o : pigmen) {
                ((EntityPigZombieMixin) o).invokeBecomeAngryAt(player);
            }
        }
    }

    public static void angerPigmen(World world, int x, int y, int z) {
        angerPigmen(null, world, x, y, z);
    }

    // Deterministic example output for a NetherOre, used by the NEI integration. Resolves
    // only the base item (no fortune / world RNG) so it is safe to call from display code.
    public static ItemStack getRepresentativeDrop(Ores ore) {
        if (ore == null) {
            return null;
        }
        ItemStack base = getVanillaBaseForSpecial(ore);
        if (base != null) {
            return base.copy();
        }
        ItemStack raw = resolveRawForOre(ore);
        if (raw != null) {
            return raw.copy();
        }
        Block oreBlock = NetherOresCore.getOreBlock(ore.getBlockIndex());
        return oreBlock == null ? null : new ItemStack(Item.getItemFromBlock(oreBlock), 1, ore.getMetadata());
    }
}
