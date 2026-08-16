package powercrystals.netherores.ores;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        assertEquals(null, DropMath.parsePreferredModOrder(""));
        assertEquals(null, DropMath.parsePreferredModOrder("   "));
        assertEquals(null, DropMath.parsePreferredModOrder(null));
    }

    @Test
    public void parsePreferredModOrder_preservesCaseForLoweringElsewhere() {
        String[] parsed = DropMath.parsePreferredModOrder("ProjRed|Core,IC2");
        assertNotNull(parsed);
        assertArrayEquals(new String[] { "ProjRed|Core", "IC2" }, parsed);
        // Callers (setPreferredModOrder) lowercase before matching.
        assertEquals("projred|core", parsed[0].toLowerCase());
    }
}
