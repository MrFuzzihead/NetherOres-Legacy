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
