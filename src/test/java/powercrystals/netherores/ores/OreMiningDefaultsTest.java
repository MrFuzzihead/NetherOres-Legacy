package powercrystals.netherores.ores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

/**
 * Regression harness for the {@code Mining.Ores.<Ore>} config defaults.
 * <p>
 * The reported bug was that the drop path hardcoded "2 items" and ignored the config.
 * These tests pin the defaults to the behaviour the hardcoded implementation used to
 * produce, so a future refactor cannot silently change or un-wire them again:
 * <ul>
 * <li>every ore defaults to Silk Touch returning the ore block,</li>
 * <li>every ore defaults to no experience (the pre-fix behaviour),</li>
 * <li>fixed ores drop exactly 2 items,</li>
 * <li>Lapis drops 8-16 and Redstone/Nikolite drop 8-10, as before.</li>
 * </ul>
 */
public class OreMiningDefaultsTest {

    private static boolean hasVanillaStyleSpread(Ores ore) {
        return ore == Ores.Lapis || ore == Ores.Redstone || ore == Ores.Nikolite;
    }

    @Test
    public void fixedOresDefaultToClassicDoubleDrop() {
        for (Ores ore : Ores.values()) {
            if (hasVanillaStyleSpread(ore)) {
                continue;
            }
            assertEquals(ore.name() + " DropCount", 2, ore.getDropCount());
            assertEquals(ore.name() + " DropSpread", 0, ore.getDropSpread());
        }
    }

    @Test
    public void spreadOresDefaultToPreConfigRanges() {
        assertEquals(8, Ores.Lapis.getDropCount());
        assertEquals(9, Ores.Lapis.getDropSpread());

        assertEquals(8, Ores.Redstone.getDropCount());
        assertEquals(3, Ores.Redstone.getDropSpread());

        assertEquals(8, Ores.Nikolite.getDropCount());
        assertEquals(3, Ores.Nikolite.getDropSpread());
    }

    @Test
    public void defaultQuantitiesStayWithinPreConfigRanges() {
        Random rng = new Random(1234);
        boolean lapisMin = false, lapisMax = false;
        boolean redstoneMin = false, redstoneMax = false;

        for (int i = 0; i < 10000; i++) {
            for (Ores ore : Ores.values()) {
                int qty = DropMath.baseQuantity(ore.getDropCount(), ore.getDropSpread(), rng);
                if (ore == Ores.Lapis) {
                    assertTrue("Lapis qty=" + qty, qty >= 8 && qty <= 16);
                    lapisMin |= qty == 8;
                    lapisMax |= qty == 16;
                } else if (ore == Ores.Redstone || ore == Ores.Nikolite) {
                    assertTrue(ore.name() + " qty=" + qty, qty >= 8 && qty <= 10);
                    redstoneMin |= qty == 8;
                    redstoneMax |= qty == 10;
                } else {
                    assertEquals(ore.name(), 2, qty);
                }
            }
        }

        assertTrue("Lapis should still be able to roll its minimum", lapisMin);
        assertTrue("Lapis should still be able to roll its maximum", lapisMax);
        assertTrue("Redstone/Nikolite should still be able to roll their minimum", redstoneMin);
        assertTrue("Redstone/Nikolite should still be able to roll their maximum", redstoneMax);
    }

    @Test
    public void allOresDefaultToSilkTouchBlockForm() {
        for (Ores ore : Ores.values()) {
            assertTrue(ore.name() + " should require Silk Touch for block form", ore.isSilkTouchRequired());
        }
    }

    /** Double of the vanilla ore XP range: coal 0-2, diamond/emerald 3-7, lapis 2-5, redstone 1-5. */
    @Test
    public void xpDefaultsMatchDoubledVanillaRanges() {
        assertEquals(0, Ores.Coal.getExp());
        assertEquals(5, Ores.Coal.getExpSpread()); // 0-4

        assertEquals(6, Ores.Diamond.getExp());
        assertEquals(9, Ores.Diamond.getExpSpread()); // 6-14
        assertEquals(6, Ores.Emerald.getExp());
        assertEquals(9, Ores.Emerald.getExpSpread());

        assertEquals(4, Ores.Lapis.getExp());
        assertEquals(7, Ores.Lapis.getExpSpread()); // 4-10

        assertEquals(2, Ores.Redstone.getExp());
        assertEquals(9, Ores.Redstone.getExpSpread()); // 2-10
    }

    @Test
    public void dustAndGemOresFollowTheirVanillaReferenceOre() {
        // Dust-style ores follow redstone (1-5 -> 2-10).
        for (Ores ore : new Ores[] { Ores.Nikolite, Ores.Sulfur, Ores.Saltpeter, Ores.Salt }) {
            assertEquals(ore.name(), 2, ore.getExp());
            assertEquals(ore.name(), 9, ore.getExpSpread());
        }
        // Gem-style ores follow emerald (3-7 -> 6-14).
        for (Ores ore : new Ores[] { Ores.Ruby, Ores.Peridot, Ores.Sapphire, Ores.Amber }) {
            assertEquals(ore.name(), 6, ore.getExp());
            assertEquals(ore.name(), 9, ore.getExpSpread());
        }
    }

    @Test
    public void metalOresFollowModernIronAndDropNoExperience() {
        for (Ores ore : new Ores[] { Ores.Iron, Ores.Gold, Ores.Copper, Ores.Tin, Ores.Silver, Ores.Lead, Ores.Uranium,
            Ores.Platinum, Ores.Nickel, Ores.Steel, Ores.Iridium, Ores.Osmium, Ores.Titanium, Ores.Mithril,
            Ores.Adamantium, Ores.Rutile, Ores.Tungsten, Ores.Tennantite, Ores.Magnesium }) {
            assertEquals(ore.name() + " Exp", 0, ore.getExp());
            assertEquals(ore.name() + " ExpSpread", 0, ore.getExpSpread());
        }
    }

    @Test
    public void xpDefaultsStayWithinTheDoubledVanillaBounds() {
        Random rng = new Random(4242);
        for (int i = 0; i < 5000; i++) {
            for (Ores ore : Ores.values()) {
                int xp = DropMath.experience(ore.getExp(), ore.getExpSpread(), rng);
                assertTrue(ore.name() + " xp=" + xp, xp >= ore.getExp() && xp <= expectedMaxXp(ore));
            }
        }
    }

    private static int expectedMaxXp(Ores ore) {
        switch (ore.name()) {
            case "Coal":
                return 4;
            case "Diamond":
            case "Emerald":
            case "Ruby":
            case "Peridot":
            case "Sapphire":
            case "Amber":
                return 14;
            case "Lapis":
                return 10;
            case "Redstone":
            case "Nikolite":
            case "Sulfur":
            case "Saltpeter":
            case "Salt":
                return 10;
            default:
                return 0;
        }
    }
}
