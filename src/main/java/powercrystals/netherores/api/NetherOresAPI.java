package powercrystals.netherores.api;

import net.minecraft.block.Block;

import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.ores.BlockNetherOres;

/**
 * Public, stable access point for other mods to integrate with NetherOres without
 * reaching into the mod's internal statics. Settings that are config-file driven are
 * exposed read-only; {@link #setPreferredModOrder(String)} is the one mutable binding.
 *
 * <p>
 * This package is published as the mod's {@code apiPackage} (see gradle.properties).
 */
public final class NetherOresAPI {

    private NetherOresAPI() {}

    /** Returns the NetherOres block for the given block index, or {@code null}. */
    public static Block getOreBlock(int blockIndex) {
        return NetherOresCore.getOreBlock(blockIndex);
    }

    /** Returns the Hellfish disguise block (netherrack look-alike). */
    public static Block getHellfishBlock() {
        return NetherOresCore.blockHellfish;
    }

    /** Whether ore generation is enabled under the current config. */
    public static boolean isOreGenEnabled() {
        return NetherOresCore.enableWorldGen.getBoolean(true);
    }

    /** Whether NetherOres can explode when mined under the current config. */
    public static boolean areExplosionsEnabled() {
        return NetherOresCore.enableExplosions.getBoolean(true);
    }

    /** Whether mining a NetherOre angers nearby pigmen under the current config. */
    public static boolean arePigmenAngered() {
        return NetherOresCore.enableAngryPigmen.getBoolean(true);
    }

    /** The configured pigman aggro radius in blocks (clamped to at least 1). */
    public static int getAngryPigmenRange() {
        return Math.max(1, NetherOresCore.angryPigmenRange.getInt());
    }

    /**
     * Sets the preferred-mod ordering used when several OreDictionary entries exist for
     * an ore. Takes effect immediately for subsequent lookups; callers may also persist
     * it by writing the {@code compat.PreferredModOrder} config value.
     */
    public static void setPreferredModOrder(String order) {
        BlockNetherOres.setPreferredModOrder(order);
    }
}
