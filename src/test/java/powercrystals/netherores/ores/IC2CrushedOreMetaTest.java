package powercrystals.netherores.ores;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Pure-JVM guard for the IC2 Macerator metadata mapping in {@link Ores}. The layout
 * mirrors IC2's {@code ItemCrushedOre} (0 Iron, 1 Copper, 2 Gold, 3 Tin, 4 Uranium,
 * 5 Silver, 6 Lead) and must match the legacy CraftTweaker recipes the native registration
 * replaces. No Minecraft/Forge runtime is required.
 */
public class IC2CrushedOreMetaTest {

    @Test
    public void ic2CrushedOreMeta_matchesKnownIC2Layout() {
        assertEquals(0, Ores.Iron.getIC2CrushedOreMeta());
        assertEquals(1, Ores.Copper.getIC2CrushedOreMeta());
        assertEquals(2, Ores.Gold.getIC2CrushedOreMeta());
        assertEquals(3, Ores.Tin.getIC2CrushedOreMeta());
        assertEquals(4, Ores.Uranium.getIC2CrushedOreMeta());
        assertEquals(5, Ores.Silver.getIC2CrushedOreMeta());
        assertEquals(6, Ores.Lead.getIC2CrushedOreMeta());
    }

    @Test
    public void ic2CrushedOreMeta_returnsMinusOneForUnsupported() {
        assertEquals(-1, Ores.Coal.getIC2CrushedOreMeta());
        assertEquals(-1, Ores.Diamond.getIC2CrushedOreMeta());
        assertEquals(-1, Ores.Platinum.getIC2CrushedOreMeta());
    }
}
