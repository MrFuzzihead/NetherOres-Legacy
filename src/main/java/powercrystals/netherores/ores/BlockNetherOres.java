package powercrystals.netherores.ores;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    private final ThreadLocal<Boolean> explode = new ThreadLocal<>();
    private final ThreadLocal<Boolean> willAnger = new ThreadLocal<>();
    private static final Ores[] ALL = Ores.values();
    private static final ConcurrentMap<Integer, Optional<ItemStack>> rawCache = new ConcurrentHashMap<>();
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
            int key = ore.getBlockIndex() * 16 + ore.getMetadata();
            // Use computeIfAbsent to populate atomically and avoid races / double-resolution.
            rawCache.computeIfAbsent(key, k -> {
                try {
                    return Optional.ofNullable(resolveRawForOre(ore));
                } catch (Throwable t) {
                    return Optional.empty();
                }
            });
        }
    }

    // Sets the preferred-mod ordering parsed once at config load (avoids re-parsing
    // the comma-separated config string on every ore-dict lookup).
    public static void setPreferredModOrder(String configValue) {
        if (configValue == null || configValue.trim()
            .isEmpty()) {
            return;
        }
        String[] parts = configValue.split(",");
        String[] lower = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
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

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        int base = quantityDropped(rand);
        return base + fortuneBonus(fortune, rand);
    }

    private ItemStack findRawOreStack(int metadata) {
        int cacheKey = this._blockIndex * 16 + metadata;
        // Atomically compute if absent and *always* retain the result (including
        // empty) to avoid re-resolving on every block break when no ore-dict entry
        // is available.
        Optional<ItemStack> opt = rawCache.computeIfAbsent(cacheKey, k -> {
            int oreIndex = k; // same encoding used for the key
            if (oreIndex < 0 || oreIndex >= ALL.length) {
                return Optional.empty();
            }
            Ores ore = ALL[oreIndex];
            try {
                return Optional.ofNullable(resolveRawForOre(ore));
            } catch (Throwable t) {
                return Optional.empty();
            }
        });
        return opt == null ? null : opt.orElse(null);
    }

    // Return a vanilla Minecraft base-item for special ores (Coal, Diamond, Emerald, Lapis, Redstone).
    // Returns null for ores that shouldn't use vanilla base drops.
    private ItemStack getVanillaBaseForSpecial(Ores ore) {
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
        if (fortune <= 0) return 0;
        return Math.max(0, rand.nextInt(fortune + 2) - 1);
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
                checkExplosionChances(var1, var3, var4, var5);
            }
        }

        return var8;
    }

    public void breakBlock(World var1, int var2, int var3, int var4, Block var5, int var6) {
        if (this.explode.get() != Boolean.FALSE) {
            checkExplosionChances(var1, var2, var3, var4);
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
            checkExplosionChances(var1, var2, var3, var4);
        }
    }

    public boolean isFireSource(World var1, int var2, int var3, int var4, ForgeDirection var5) {
        return var5 == ForgeDirection.UP;
    }

    public static void checkExplosionChances(World var1, int var2, int var3, int var4) {
        if (!var1.isRemote && NetherOresCore.enableExplosions.getBoolean(true)) {
            for (int var5 = -1; var5 <= 1; var5++) {
                for (int var6 = -1; var6 <= 1; var6++) {
                    for (int var7 = -1; var7 <= 1; var7++) {
                        if ((var5 | var6 | var7) != 0) {
                            int var8 = var2 + var5;
                            int var9 = var3 + var6;
                            int var10 = var4 + var7;
                            Block var0 = var1.getBlock(var8, var9, var10);
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
            int _aggroRange = 32;
            List<EntityPigZombie> var5 = var1.getEntitiesWithinAABB(
                EntityPigZombie.class,
                AxisAlignedBB.getBoundingBox(
                    var2 - _aggroRange,
                    var3 - _aggroRange,
                    var4 - _aggroRange,
                    var2 + _aggroRange + 1,
                    var3 + _aggroRange + 1,
                    var4 + _aggroRange + 1));

            for (EntityPigZombie o : var5) {
                ((EntityPigZombieMixin) o).becomeAngryAt(var0);
            }
        }
    }

    public static void angerPigmen(World var0, int var1, int var2, int var3) {
        angerPigmen(null, var0, var1, var2, var3);
    }
}
