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
    // Minecraft runs block breaks on the single world thread, so plain fields are
    // sufficient (and cheaper than per-access ThreadLocal lookups). The defaults match
    // the historical "unset" behavior that breakBlock relied on.
    private boolean explode = true;
    private boolean willAnger = false;

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
        // Prefill the raw item cache for all known Ores to avoid first-hit overhead at runtime.
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

    public int damageDropped(int var1) {
        return var1;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        int base = quantityDropped(rand);
        return base + fortuneBonus(fortune, rand);
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

    // Get vanilla base quantity (without fortune) for special ores. Used to compute 2x base.
    private int getVanillaBaseQuantity(Ores ore, Random rand) {
        if (ore == null) return 0;
        return switch (ore) {
            case Coal, Diamond, Emerald -> 1; // vanilla yields 1
            case Lapis ->
                // vanilla lapis drops 4-8 (4 + rand.nextInt(5))
                4 + rand.nextInt(5);
            case Redstone ->
                // vanilla redstone drops 4-5 (4 + rand.nextInt(2))
                4 + rand.nextInt(2);
            default -> 0;
        };
    }

    // True when the ore uses a vanilla base drop (Coal/Diamond/Emerald/Lapis/Redstone).
    private boolean isVanillaSpecialOre(Ores ore) {
        if (ore == null) return false;
        return switch (ore) {
            case Coal, Diamond, Emerald, Lapis, Redstone -> true;
            default -> false;
        };
    }

    // Resolves the base drop item for this ore: the special vanilla item
    // (Coal/Diamond/Emerald/Lapis/Redstone) if applicable, otherwise the
    // OreDictionary/raw item, or null when neither is available (callers fall back
    // to dropping the nether ore block itself). Shared by getItemDropped and getDrops.
    private ItemStack resolveBaseDropItem(int metadata) {
        int oreIndex = this._blockIndex * 16 + metadata;
        if (oreIndex >= 0 && oreIndex < ALL.length) {
            ItemStack vanilla = getVanillaBaseForSpecial(ALL[oreIndex]);
            if (vanilla != null) return vanilla;
        }
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
        int oreIndex = this._blockIndex * 16 + metadata;
        Ores ore = oreIndex >= 0 && oreIndex < ALL.length ? ALL[oreIndex] : null;

        ItemStack base = resolveBaseDropItem(metadata);
        if (base != null) {
            int baseQty;
            if (isVanillaSpecialOre(ore)) {
                // Priority 1: 2x the vanilla base quantity
                baseQty = getVanillaBaseQuantity(ore, world.rand) * 2;
            } else if (ore == Ores.Nikolite) {
                // Nikolite uses the vanilla redstone base (4-5) doubled
                baseQty = (4 + world.rand.nextInt(2)) * 2;
            } else {
                // Priority 2: Et Futurum raw items / OreDictionary entries (base 2)
                baseQty = 2;
            }
            int qty = baseQty + fortuneBonus(fortune, world.rand);
            ItemStack out = base.copy();
            out.stackSize = qty;
            ret.add(out);
            return ret;
        }

        // Fallback: drop the nether ore block itself exactly once (not affected by fortune)
        ret.add(new ItemStack(Item.getItemFromBlock(this), 1, metadata));
        return ret;
    }

    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean isHarvest) {
        boolean notSilk = player == null || !EnchantmentHelper.getSilkTouchModifier(player);
        this.explode = notSilk;
        this.willAnger = true;
        final boolean removed;
        try {
            removed = super.removedByPlayer(world, player, x, y, z, isHarvest);
        } finally {
            this.willAnger = false;
            this.explode = true;
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
        if (this.explode) {
            checkExplosionChances(world, x, y, z);
        }

        if (!this.willAnger) {
            angerPigmen(world, x, y, z);
        }

        if (NetherOresCore.hellFishFromOre.getBoolean(false)
            && world.rand.nextInt(10000) < NetherOresCore.hellFishFromOreChance.getInt()) {
            BlockHellfish.spawnHellfish(world, x, y, z);
        }

        super.breakBlock(world, x, y, z, blockType, metadata);
    }

    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
        this.explode = false;
        this.willAnger = NetherOresCore.enableMobsAngerPigmen.getBoolean(true) || explosion == null
            || !(explosion.getExplosivePlacedBy() instanceof EntityLiving);
        super.onBlockExploded(world, x, y, z, explosion);
        this.willAnger = true;
        this.explode = true;
        if (NetherOresCore.enableExplosionChainReactions.getBoolean(true)) {
            checkExplosionChances(world, x, y, z);
        }
    }

    public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
        return var5 == ForgeDirection.UP;
    }

    public static void checkExplosionChances(World world, int x, int y, int z) {
        if (!world.isRemote && NetherOresCore.enableExplosions.getBoolean(true)) {
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
