package powercrystals.netherores.ores;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

/**
 * Pure-JVM unit tests for {@link DropMath}. No Minecraft/Forge runtime is required, so
 * these guard the drop-quantity and preferred-mod parsing logic that drives the most
 * customized part of the mod.
 */
public class DropMathTest {

    @Test
    public void fortuneBonus_returnsZeroWithoutFortune() {
        assertEquals(0, DropMath.fortuneBonus(0, new Random(1)));
        assertEquals(0, DropMath.fortuneBonus(-3, new Random(1)));
    }

    @Test
    public void fortuneBonus_isNeverNegativeAndStaysWithinFortune() {
        Random rng = new Random(42);
        for (int fortune = 1; fortune <= 50; fortune++) {
            for (int trial = 0; trial < 200; trial++) {
                int bonus = DropMath.fortuneBonus(fortune, rng);
                assertTrue("fortune=" + fortune + " bonus=" + bonus, bonus >= 0 && bonus <= fortune);
            }
        }
    }

    @Test
    public void parsePreferredModOrder_splitsAndTrims() {
        assertArrayEquals(
            new String[] { "etfuturum", "thermalfoundation", "ic2" },
            DropMath.parsePreferredModOrder(" etfuturum , thermalfoundation,ic2 "));
    }

    @Test
    public void parsePreferredModOrder_returnsNullForBlankInput() {
        assertNull(DropMath.parsePreferredModOrder(""));
        assertNull(DropMath.parsePreferredModOrder("   "));
        assertNull(DropMath.parsePreferredModOrder(null));
    }

    @Test
    public void parsePreferredModOrder_preservesCaseForLoweringElsewhere() {
        String[] parsed = DropMath.parsePreferredModOrder("ProjRed|Core,IC2");
        assertNotNull(parsed);
        assertArrayEquals(new String[] { "ProjRed|Core", "IC2" }, parsed);
        // Callers (setPreferredModOrder) lowercase before matching.
        assertEquals("projred|core", parsed[0].toLowerCase());
    }

    @Test
    public void baseQuantity_withoutSpreadIsExact() {
        for (int count = 0; count <= 64; count++) {
            assertEquals(count, DropMath.baseQuantity(count, 0, new Random(count)));
        }
    }

    @Test
    public void baseQuantity_negativeCountClampsToZero() {
        assertEquals(0, DropMath.baseQuantity(-5, 0, new Random(1)));
        for (int seed = 0; seed < 100; seed++) {
            int qty = DropMath.baseQuantity(-5, 5, new Random(seed));
            assertTrue("qty=" + qty, qty >= 0 && qty <= 4);
        }
    }

    @Test
    public void baseQuantity_spreadStaysWithinConfiguredBounds() {
        Random rng = new Random(7);
        for (int i = 0; i < 5000; i++) {
            int qty = DropMath.baseQuantity(8, 9, rng);
            assertTrue("qty=" + qty, qty >= 8 && qty <= 16);
        }
    }

    @Test
    public void dropQuantity_addsFortuneBonusOnTopOfBase() {
        // With spread 0 the base is deterministic and the only variance is Fortune.
        for (int fortune = 0; fortune <= 5; fortune++) {
            for (int seed = 0; seed < 200; seed++) {
                int qty = DropMath.dropQuantity(2, 0, fortune, new Random(seed));
                assertTrue("fortune=" + fortune + " qty=" + qty, qty >= 2 && qty <= 2 + fortune);
            }
        }
        assertEquals(0, DropMath.dropQuantity(0, 0, 0, new Random(1)));
        assertEquals("disabled ore must ignore Fortune", 0, DropMath.dropQuantity(0, 0, 5, new Random(1)));
    }

    @Test
    public void allowsSilkHarvest_followsConfigFlag() {
        assertTrue(DropMath.allowsSilkHarvest(true));
        assertFalse(DropMath.allowsSilkHarvest(false));
    }

    @Test
    public void experience_matchesBaseQuantityContract() {
        for (int exp = 0; exp <= 6; exp++) {
            for (int spread = 0; spread <= 9; spread++) {
                for (int seed = 0; seed < 20; seed++) {
                    int xp = DropMath.experience(exp, spread, new Random(seed));
                    assertEquals(DropMath.baseQuantity(exp, spread, new Random(seed)), xp);
                    assertTrue("xp=" + xp, xp >= exp && xp < exp + Math.max(1, spread));
                }
            }
        }
    }

    @Test
    public void experience_zeroIsZero() {
        assertEquals(0, DropMath.experience(0, 0, new Random(1)));
    }
}
