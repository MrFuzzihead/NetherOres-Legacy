package powercrystals.netherores.ores;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Wiring guard for the mining config.
 * <p>
 * The reported bug was a pure wiring failure: the config options existed (left over in
 * users' .cfg files) but no code ever read them, so the drop path kept using hardcoded
 * numbers. {@link DropMath} and {@link OreMiningDefaultsTest} cover the logic; this test
 * covers the glue, which cannot be instantiated in a plain JVM (it needs a live
 * Minecraft/Forge runtime).
 * <p>
 * These assertions are intentionally source-based. They fail loudly if a future change
 * re-introduces hardcoded drop counts or drops one of the config reads.
 */
public class ConfigWiringTest {

    private static String readSource(String relativePath) throws IOException {
        Path dir = Paths.get("")
            .toAbsolutePath();
        for (int depth = 0; depth < 5 && dir != null; depth++, dir = dir.getParent()) {
            Path candidate = dir.resolve(relativePath);
            if (Files.isRegularFile(candidate)) {
                return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
            }
        }
        throw new IOException(
            "Could not locate " + relativePath
                + " from "
                + Paths.get("")
                    .toAbsolutePath());
    }

    @Test
    public void oresReadsEveryMiningConfigKey() throws IOException {
        String source = readSource("src/main/java/powercrystals/netherores/ores/Ores.java");

        assertTrue("Mining.Ores category is not read", source.contains("\"Mining.Ores.\""));
        assertTrue("DropCount is not read from config", source.contains("\"DropCount\""));
        assertTrue("DropSpread is not read from config", source.contains("\"DropSpread\""));
        assertTrue("Exp is not read from config", source.contains("\"Exp\""));
        assertTrue("ExpSpread is not read from config", source.contains("\"ExpSpread\""));
        assertTrue("RequireSilkTouch is not read from config", source.contains("\"RequireSilkTouch\""));
    }

    @Test
    public void dropPathUsesTheConfiguredOreSettings() throws IOException {
        String source = readSource("src/main/java/powercrystals/netherores/ores/BlockNetherOres.java");

        assertTrue("getDrops does not use DropCount", source.contains("ore.getDropCount()"));
        assertTrue("getDrops does not use DropSpread", source.contains("ore.getDropSpread()"));
        assertTrue("getExpDrop does not use Exp", source.contains("ore.getExp()"));
        assertTrue("getExpDrop does not use ExpSpread", source.contains("ore.getExpSpread()"));
        assertTrue("canSilkHarvest does not use RequireSilkTouch", source.contains("ore.isSilkTouchRequired()"));
        assertTrue("drop quantity does not go through DropMath", source.contains("DropMath.dropQuantity("));
        assertTrue("experience does not go through DropMath", source.contains("DropMath.experience("));
    }

    @Test
    public void dropPathNoLongerHardcodesTheDoubleDrop() throws IOException {
        String source = readSource("src/main/java/powercrystals/netherores/ores/BlockNetherOres.java");

        assertFalse("Hardcoded double drop is back", source.contains("baseQty = 2"));
        assertFalse("Legacy vanilla-base doubling helper is back", source.contains("getVanillaBaseQuantity"));
    }
}
