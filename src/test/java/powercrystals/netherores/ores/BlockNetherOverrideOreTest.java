package powercrystals.netherores.ores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import org.junit.Test;

import sun.misc.Unsafe;

/**
 * Regression guard for the override wrapper's constructor.
 * <p>
 * {@link Block}'s constructor calls {@link Block#isOpaqueCube()} virtually, before the
 * wrapper has assigned {@code _override}. The first version of the delegation returned
 * {@code _override.isOpaqueCube()} and crashed dedicated servers during preInit with an
 * NPE (the PR #6 CI crash). These tests allocate the wrapper without running the
 * constructor (Forge's block registry cannot be initialised without a LaunchClassLoader)
 * and exercise the exact call path.
 */
public class BlockNetherOverrideOreTest {

    @SuppressWarnings("unchecked")
    private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (T) ((Unsafe) field.get(null)).allocateInstance(type);
    }

    private static Block transparentBlock() {
        return new Block(Material.glass) {

            @Override
            public boolean isOpaqueCube() {
                return false;
            }

            @Override
            public int getLightOpacity() {
                return 0;
            }
        };
    }

    private static void setOverride(BlockNetherOverrideOre wrapper, Block block) throws Exception {
        Field field = BlockNetherOverrideOre.class.getDeclaredField("_override");
        field.setAccessible(true);
        field.set(wrapper, block);
    }

    @Test
    public void isOpaqueCubeIsSafeBeforeOverrideIsAssigned() throws Exception {
        BlockNetherOverrideOre wrapper = allocateWithoutConstructor(BlockNetherOverrideOre.class);

        // This is exactly what Block's constructor does before _override exists.
        assertTrue(wrapper.isOpaqueCube());
    }

    @Test
    public void isOpaqueCubeDelegatesAfterOverrideIsAssigned() throws Exception {
        BlockNetherOverrideOre wrapper = allocateWithoutConstructor(BlockNetherOverrideOre.class);
        setOverride(wrapper, transparentBlock());

        assertFalse(wrapper.isOpaqueCube());
        assertEquals(0, wrapper.getLightOpacity());
    }
}
