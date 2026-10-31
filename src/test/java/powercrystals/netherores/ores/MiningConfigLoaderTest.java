package powercrystals.netherores.ores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import net.minecraftforge.common.config.Configuration;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * End-to-end harness for the {@code Mining.Ores} config layer using Forge's own
 * {@link Configuration} parser.
 * <p>
 * The reported bug was that users edited {@code DropCount} in their existing
 * {@code NetherOres.cfg} and nothing happened. The pure-JVM tests cannot load Forge's
 * config without a Minecraft home, so this test fakes the single FML static field the
 * parser needs and then:
 * <ul>
 * <li>loads a legacy-style {@code mining.ores.coal.DropCount} value,</li>
 * <li>checks the parsed value reaches {@link Ores},</li>
 * <li>saves and reloads to prove the keys round-trip,</li>
 * <li>checks a fresh config receives the intended defaults.</li>
 * </ul>
 * The ore enum is globally mutable, so every test restores the defaults afterwards.
 */
public class MiningConfigLoaderTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static int resetCounter = 0;

    /**
     * Forge's {@link Configuration} constructor relativises the config path against
     * {@code FMLInjectionData.minecraftHome}; in a plain JVM that static field is null.
     */
    @BeforeClass
    public static void stubFmlMinecraftHome() throws Exception {
        Class<?> fmlData = Class.forName("cpw.mods.fml.relauncher.FMLInjectionData");
        Field home = fmlData.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        if (home.get(null) == null) {
            File temp = Files.createTempDirectory("netherores-fml-home")
                .toFile();
            temp.deleteOnExit();
            home.set(null, temp);
        }
    }

    private Configuration load(String name, String content) throws IOException {
        File file = new File(folder.getRoot(), name);
        if (content != null) {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        }
        return new Configuration(file);
    }

    /** Restores every ore to its built-in defaults so test order does not matter. */
    @After
    public void restoreDefaults() throws IOException {
        // Writing explicit values is required: loadConfig uses the current field as the
        // "default", so reloading a blank file would simply keep a previous test's values.
        StringBuilder cfg = new StringBuilder("mining {\n    ores {\n");
        for (Ores ore : Ores.values()) {
            cfg.append("    ")
                .append(
                    ore.name()
                        .toLowerCase())
                .append(" {\n")
                .append("        I:DropCount=")
                .append(Ores.defaultDropCount(ore))
                .append('\n')
                .append("        I:DropSpread=")
                .append(Ores.defaultDropSpread(ore))
                .append('\n')
                .append("        I:Exp=")
                .append(Ores.defaultExp(ore))
                .append('\n')
                .append("        I:ExpSpread=")
                .append(Ores.defaultExpSpread(ore))
                .append('\n')
                .append("        B:RequireSilkTouch=true\n")
                .append("    }\n");
        }
        cfg.append("    }\n}\n");

        Configuration defaults = load("reset-" + (resetCounter++) + ".cfg", cfg.toString());
        for (Ores ore : Ores.values()) {
            ore.loadConfig(defaults);
        }
    }

    @Test
    public void legacyConfigValuesAreRead() throws IOException {
        Configuration config = load(
            "Legacy.cfg",
            "mining {\n" + "    ores {\n"
                + "        coal {\n"
                + "            I:DropCount=1\n"
                + "            I:Exp=2\n"
                + "            B:RequireSilkTouch=false\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
        Ores.Coal.loadConfig(config);

        // The legacy cfg block from the bug report must now actually be honoured.
        assertEquals(1, Ores.Coal.getDropCount());
        assertEquals(2, Ores.Coal.getExp());
        // ExpSpread is new; the coal default spread (doubled vanilla 0-2 range) applies.
        assertEquals(5, Ores.Coal.getExpSpread());
        assertFalse(Ores.Coal.isSilkTouchRequired());

        // Ores that were not present in the file keep their defaults.
        Ores.Iron.loadConfig(config);
        assertEquals(2, Ores.Iron.getDropCount());
        assertEquals(0, Ores.Iron.getExp());
        assertEquals(0, Ores.Iron.getExpSpread());
        assertTrue(Ores.Iron.isSilkTouchRequired());
    }

    @Test
    public void configuredValuesSurviveSaveAndReload() throws IOException {
        Configuration config = load("RoundTrip.cfg", null);
        Ores.Iron.loadConfig(config);
        config.get("Mining.Ores.Iron", "DropCount", 2)
            .set(5);
        config.get("Mining.Ores.Iron", "DropSpread", 0)
            .set(3);
        config.get("Mining.Ores.Iron", "Exp", 0)
            .set(7);
        config.get("Mining.Ores.Iron", "ExpSpread", 0)
            .set(0);
        config.save();

        Configuration reloaded = load("RoundTrip.cfg", null);
        Ores.Iron.loadConfig(reloaded);
        assertEquals(5, Ores.Iron.getDropCount());
        assertEquals(3, Ores.Iron.getDropSpread());
        assertEquals(7, Ores.Iron.getExp());
        assertEquals(0, Ores.Iron.getExpSpread());
    }

    @Test
    public void freshConfigGetsTheIntendedDefaults() throws IOException {
        Configuration config = load("Defaults.cfg", null);
        for (Ores ore : Ores.values()) {
            ore.loadConfig(config);
        }

        assertEquals(2, Ores.Coal.getDropCount());
        assertEquals(0, Ores.Coal.getDropSpread());
        assertEquals(8, Ores.Lapis.getDropCount());
        assertEquals(9, Ores.Lapis.getDropSpread());
        assertEquals(8, Ores.Redstone.getDropCount());
        assertEquals(3, Ores.Redstone.getDropSpread());
        assertEquals(8, Ores.Nikolite.getDropCount());
        assertEquals(3, Ores.Nikolite.getDropSpread());
        assertEquals(6, Ores.Diamond.getExp());
        assertEquals(9, Ores.Diamond.getExpSpread());
        assertEquals(0, Ores.Coal.getExp());
        assertEquals(5, Ores.Coal.getExpSpread());
        assertTrue(Ores.Diamond.isSilkTouchRequired());

        config.save();
        String saved = new String(
            Files.readAllBytes(new File(folder.getRoot(), "Defaults.cfg").toPath()),
            StandardCharsets.UTF_8);
        // The section admins edit must actually be written with the intended keys.
        assertTrue(saved.contains("I:DropCount=2"));
        assertTrue(saved.contains("I:DropSpread=0"));
        assertTrue(saved.contains("I:Exp=0"));
        assertTrue(saved.contains("I:ExpSpread=5"));
        assertTrue(saved.contains("I:ExpSpread=9"));
        assertTrue(saved.contains("B:RequireSilkTouch=true"));
        assertTrue(
            saved.toLowerCase()
                .contains("lapis"));
    }
}
