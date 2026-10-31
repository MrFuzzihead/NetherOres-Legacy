package powercrystals.netherores.ores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Regression guard for the metadata contract behind mining XP and pick-block.
 * <p>
 * {@code Block.getDamageValue(world, x, y, z)} is {@code damageDropped(metadata)}, and
 * Forge's {@code BlockEvent.BreakEvent} feeds that value to {@code getExpDrop()} as the
 * metadata. A {@code damageDropped()} override that returned the drop item's damage made
 * the XP lookup use 0 (raw ore/dust items have damage 0), so Tin resolved as Coal and
 * Sulfur resolved as Platinum. {@code damageDropped()} must therefore keep returning the
 * block metadata; {@code getDrops()} builds the correct item stacks itself.
 * <p>
 * Forge's registries cannot be initialised without a LaunchClassLoader, so this test
 * inserts the few entries the ore constructor and vanilla-special drops need through the
 * registries' internal raw-add path.
 */
public class BlockNetherOresMetadataTest {

    private static BlockNetherOres block0;
    private static BlockNetherOres block1;

    @BeforeClass
    public static void setUp() throws Exception {
        rawAdd(Block.blockRegistry, 1, "minecraft:netherrack", new Block(Material.rock) {});
        // Vanilla-special ores resolve through Items.*; make those fields non-null so a
        // re-added damageDropped() override fails as a clean assertion, not an NPE.
        rawAdd(Item.itemRegistry, 1, "minecraft:diamond", new Item() {});
        rawAdd(Item.itemRegistry, 2, "minecraft:coal", new Item() {});
        rawAdd(Item.itemRegistry, 3, "minecraft:dye", new Item() {});
        rawAdd(Item.itemRegistry, 4, "minecraft:redstone", new Item() {});
        rawAdd(Item.itemRegistry, 5, "minecraft:emerald", new Item() {});

        block0 = new BlockNetherOres(0);
        block1 = new BlockNetherOres(1);
    }

    private static void rawAdd(Object registry, int id, String name, Object value) throws Exception {
        Method addRaw = registry.getClass()
            .getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class);
        addRaw.setAccessible(true);
        addRaw.invoke(registry, id, name, value);
    }

    @Test
    public void damageDroppedIsAlwaysTheBlockMetadata() {
        for (int meta = 0; meta < 16; meta++) {
            assertEquals(meta, block0.damageDropped(meta));
            assertEquals(meta, block1.damageDropped(meta));
        }
    }

    @Test
    public void expIsResolvedFromTheBlockMetadata() {
        for (Ores ore : Ores.values()) {
            BlockNetherOres block = ore.getBlockIndex() == 0 ? block0 : block1;
            // Exactly what Forge's BreakEvent passes to getExpDrop().
            int metadata = block.damageDropped(ore.getMetadata());
            int max = ore.getExp() + Math.max(1, ore.getExpSpread());
            for (int i = 0; i < 100; i++) {
                int xp = block.getExpDrop(null, metadata, 0);
                assertTrue(ore.name() + " xp=" + xp, xp >= ore.getExp() && xp < max);
            }
        }
    }

    @Test
    public void tinDropsNoXpAndSulfurDropsItsConfiguredRange() {
        int tinMeta = block0.damageDropped(Ores.Tin.getMetadata());
        for (int i = 0; i < 100; i++) {
            assertEquals(0, block0.getExpDrop(null, tinMeta, 0));
        }

        int sulfurMeta = block1.damageDropped(Ores.Sulfur.getMetadata());
        boolean sawXp = false;
        for (int i = 0; i < 1000 && !sawXp; i++) {
            int xp = block1.getExpDrop(null, sulfurMeta, 0);
            assertTrue("Sulfur xp=" + xp, xp >= 2 && xp <= 10);
            sawXp |= xp > 0;
        }
        assertTrue("Sulfur should be able to drop XP", sawXp);
    }
}
