package powercrystals.netherores.ores;

import java.util.Random;

/**
 * Pure, Minecraft-agnostic math/parsing helpers used by the NetherOre drop path.
 * <p>
 * Deliberately free of {@code net.minecraft} dependencies so this class can be
 * unit-tested in a plain JVM without booting a Minecraft runtime.
 */
public final class DropMath {

    private DropMath() {}

    /**
     * Fortune bonus for a nether ore drop, clamped to {@code [0, fortune]} so nether
     * ores never reduce drops. Mirrors vanilla's fortune behavior for ores.
     */
    public static int fortuneBonus(int fortune, Random rand) {
        if (fortune <= 0) {
            return 0;
        }
        return Math.max(0, rand.nextInt(fortune + 2) - 1);
    }

    /**
     * Resolves the base (pre-Fortune) item count for a single broken block.
     * <p>
     * {@code dropCount} is the configured base amount and {@code dropSpread} adds a
     * uniform random bonus in {@code [0, dropSpread)}. A zero count means "drop
     * nothing"; a zero spread means "always exactly dropCount".
     *
     * @param dropCount  configured base drop count ({@code Mining.Ores.<Ore>.DropCount})
     * @param dropSpread configured random spread ({@code Mining.Ores.<Ore>.DropSpread})
     * @param rand       RNG to draw the spread from; only consumed when spread &gt; 0
     */
    public static int baseQuantity(int dropCount, int dropSpread, Random rand) {
        int count = Math.max(0, dropCount);
        if (dropSpread > 0) {
            count += rand.nextInt(dropSpread);
        }
        return count;
    }

    /**
     * Resolves the final item count for a single broken block, including the Fortune
     * bonus. Kept pure so the whole quantity contract is unit-testable without a
     * Minecraft runtime.
     */
    public static int dropQuantity(int dropCount, int dropSpread, int fortune, Random rand) {
        // Explicitly disabled ore: no drops, not even Fortune particles.
        if (dropCount <= 0 && dropSpread <= 0) {
            return 0;
        }
        return baseQuantity(dropCount, dropSpread, rand) + fortuneBonus(fortune, rand);
    }

    /**
     * Resolves the experience dropped by one block. Same min + spread contract as
     * {@link #baseQuantity} so vanilla-style XP ranges can be configured.
     */
    public static int experience(int exp, int expSpread, Random rand) {
        return baseQuantity(exp, expSpread, rand);
    }

    /**
     * Whether Silk Touch should yield the ore block itself instead of the configured
     * item drops. {@code requireSilkTouch == true} is vanilla behavior (silk harvests
     * the block); {@code false} makes Silk Touch behave like a normal break so the
     * configured drops are produced.
     */
    public static boolean allowsSilkHarvest(boolean requireSilkTouch) {
        return requireSilkTouch;
    }

    /**
     * Parses the comma-separated preferred-mod-ordering config value, trimming each
     * entry. Returns {@code null} (and leaves the current setting untouched) when the
     * input is blank. Callers separately lowercase the entries for case-insensitive
     * matching.
     */
    public static String[] parsePreferredModOrder(String configValue) {
        if (configValue == null || configValue.trim()
            .isEmpty()) {
            return null;
        }
        String[] parts = configValue.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }
}
