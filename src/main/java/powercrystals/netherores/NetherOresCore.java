package powercrystals.netherores;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

import cofh.core.world.WorldHandler;
import cofh.lib.util.RegistryUtils;
import cofh.mod.BaseMod;
import cofh.mod.updater.UpdateManager;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.CustomProperty;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import powercrystals.netherores.entity.EntityArmedOre;
import powercrystals.netherores.entity.EntityHellfish;
import powercrystals.netherores.net.ServerProxy;
import powercrystals.netherores.ores.BlockNetherOres;
import powercrystals.netherores.ores.BlockNetherOverrideOre;
import powercrystals.netherores.ores.ItemBlockNetherOre;
import powercrystals.netherores.ores.Ores;
import powercrystals.netherores.world.BlockHellfish;
import powercrystals.netherores.world.NetherOresWorldGenHandler;

@Mod(
    modid = NetherOresCore.MODID,
    name = NetherOresCore.MODNAME,
    version = Tags.VERSION,
    dependencies = NetherOresCore.DEPENDENCIES,
    customProperties = { @CustomProperty(k = "cofhversion", v = "true") })
public class NetherOresCore extends BaseMod {

    public static final String MODID = "NetherOres";
    public static final String MODNAME = "NetherOres";
    public static final String DEPENDENCIES = "required-after:CoFHCore@[1.7.10R3.1.0,1.7.10R3.2.0);";
    public static final String MOBTEXTUREFOLDER = "netherores:textures/mob/";
    public static Block[] blockNetherOres = new Block[(Ores.values().length + 15) / 16];
    public static Block blockHellfish;
    public static Property enableWorldGen;
    public static Property enableExplosions;
    public static Property explosionPower;
    public static Property explosionProbability;
    public static Property enableExplosionChainReactions;
    public static Property enableFortuneExplosions;
    public static Property enableAngryPigmen;
    public static Property silkyStopsPigmen;
    public static Property enableMobsAngerPigmen;
    public static Property enableHellfish;
    public static Property enableSmeltToOres;
    public static Property enableStandardFurnaceRecipes;
    public static Property enableMaceratorRecipes;
    public static Property enablePulverizerRecipes;
    public static Property enableInductionSmelterRecipes;
    public static Property enableGrinderRecipes;
    public static Property forceOreSpawn;
    public static Property worldGenAllDimensions;
    public static Property enableHellQuartz;
    public static Property hellFishFromOre;
    public static Property hellFishFromOreChance;
    public static Property hellFishPerChunk;
    public static Property hellFishPerGroup;
    public static Property hellFishMinY;
    public static Property hellFishMaxY;
    public static Property hellFishRetrogen;
    public static Property hellFishMaxHealth;
    public static ConfigCategory overrideOres;
    private static Configuration config;
    @SidedProxy(
        clientSide = "powercrystals.netherores.net.ClientProxy",
        serverSide = "powercrystals.netherores.net.ServerProxy")
    public static ServerProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent var1) {
        this.setConfigFolderBase(var1.getModConfigurationDirectory());
        this.loadConfig(this.getCommonConfig());
        this.loadLang();
        int var2 = 0;

        for (int var3 = blockNetherOres.length; var2 < var3; var2++) {
            Block var4 = blockNetherOres[var2] = new BlockNetherOres(var2);
            GameRegistry.registerBlock(var4, ItemBlockNetherOre.class, var4.getUnlocalizedName());
        }

        blockHellfish = new BlockHellfish();
        GameRegistry.registerBlock(blockHellfish, ItemBlock.class, "netherOresBlockHellfish");
        GameRegistry.registerCustomItemStack("netherOresBlockHellfish", new ItemStack(blockHellfish));
        if (enableHellQuartz.getBoolean(true)) {
            BlockNetherOverrideOre var6 = new BlockNetherOverrideOre(Blocks.quartz_ore) {

                @Override
                public int quantityDroppedWithBonus(int var1, Random var2x) {
                    synchronized (Blocks.class) {
                        // Don't assign to Blocks.quartz_ore (final). Call the original block's implementation directly.
                        return super._override.quantityDroppedWithBonus(var1, var2x);
                    }
                }

                @Override
                public void dropBlockAsItemWithChance(World var1, int var2x, int var3, int var4, int var5, float var6x,
                    int var7) {
                    synchronized (Blocks.class) {
                        // Don't assign to Blocks.quartz_ore (final). Call the original block's implementation directly.
                        super._override.dropBlockAsItemWithChance(var1, var2x, var3, var4, var5, var6x, var7);
                    }
                }
            };
            // Use RegistryUtils.overwriteEntry instead of assigning to Blocks.quartz_ore.
            RegistryUtils.overwriteEntry(Block.blockRegistry, "minecraft:quartz_ore", var6);
        }

        for (Ores var5 : Ores.values()) {
            var5.load();
        }

        EntityRegistry.registerModEntity(EntityArmedOre.class, "ArmedOre", 0, this, 80, 5, false);
        EntityRegistry.registerModEntity(EntityHellfish.class, "netherOresHellfish", 1, this, 160, 5, true);
    }

    @EventHandler
    public void load(FMLInitializationEvent var1) {
        WorldHandler.instance.registerFeature(new NetherOresWorldGenHandler());
        proxy.load();
        UpdateManager.registerUpdater(new UpdateManager(this, null, "http://teamcofh.com/downloads/"));
    }

    @EventHandler
    public void handleIMC(IMCEvent var1) {
        this.processIMC(var1.getMessages());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent var1) {
        if (!enableSmeltToOres.getBoolean(true)) {
            Ores.Coal.registerSmelting(new ItemStack(Items.coal));
        }

        for (Ores var5 : Ores.values()) {
            String var6 = var5.getOreName();
            if (enableSmeltToOres.getBoolean(true) && !OreDictionary.getOres(var6)
                .isEmpty()) {
                this.registerOreDictOre(
                    var5,
                    var6,
                    (ItemStack) OreDictionary.getOres(var6)
                        .get(0));
            } else {
                var6 = var5.getSmeltName();
                if (!OreDictionary.getOres(var6)
                    .isEmpty()) {
                    this.registerOreDictSmelt(
                        var5,
                        var6,
                        (ItemStack) OreDictionary.getOres(var6)
                            .get(0));
                }
            }

            var6 = var5.getDustName();
            if (!OreDictionary.getOres(var6)
                .isEmpty()) {
                this.registerOreDictDust(
                    var5,
                    var6,
                    (ItemStack) OreDictionary.getOres(var6)
                        .get(0));
            }

            var6 = var5.getAltName();
            if (!OreDictionary.getOres(var6)
                .isEmpty()) {
                this.registerOreDictGem(
                    var5,
                    var6,
                    (ItemStack) OreDictionary.getOres(var6)
                        .get(0));
            }
        }

        Ores.Coal.registerPulverizing(new ItemStack(Items.coal));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent var1) {
        this.processIMC(FMLInterModComms.fetchRuntimeMessages(this));

        for (Entry var3 : overrideOres.getValues()
            .entrySet()) {
            String var4 = (String) var3.getKey();
            Block var5 = Block.getBlockFromName(var4);
            if (this.isBlockInvalid(var5)) {
                overrideOres.remove(var4);
            } else if (((Property) var3.getValue()).setRequiresMcRestart(true)
                .getBoolean(true)) {
                    RegistryUtils.overwriteEntry(Block.blockRegistry, var4, new BlockNetherOverrideOre(var5));
                }
        }

        for (Ores var9 : Ores.values()) {
            var9.postConfig(config);
        }

        super._log.info("Load Complete.");
        config.save();
    }

    private boolean isBlockInvalid(Block var1) {
        return Block.getIdFromBlock(var1) <= 175;
    }

    private void processIMC(List<IMCMessage> var1) {
        for (IMCMessage var3 : var1) {
            try {
                String var4 = var3.key;
                if ("registerOverrideOre".equals(var4)) {
                    String var5 = var3.getStringValue();
                    Block var6 = Block.getBlockFromName(var5);
                    if (this.isBlockInvalid(var6)) {
                        throw new IllegalArgumentException("Cannot override vanilla blocks via IMC.");
                    }

                    Property var7 = overrideOres.get(var5);
                    var7.getBoolean(true);
                    if (!var7.wasRead()) {
                        var7.comment = "Override the '" + var5 + "' block (registered by '" + var3.getSender() + "')";
                    }
                } else {
                    super._log.debug("Unknown IMC message (%s) from %s", new Object[] { var4, var3.getSender() });
                }
            } catch (Throwable var8) {
                super._log.error("Bad IMC message (%s) from %s", new Object[] { var3.key, var3.getSender(), var8 });
            }
        }
    }

    public static Block getOreBlock(int var0) {
        return var0 >= 0 && var0 < blockNetherOres.length ? blockNetherOres[var0] : null;
    }

    private void loadConfig(File var1) {
        Configuration var2 = new Configuration(var1);
        config = var2;
        var2.load();
        explosionPower = var2.get("general", "ExplosionPower", 2);
        explosionPower.comment = "How powerful an explosion will be. Creepers are 3, TNT is 4, electrified creepers are 6. This affects both the ability of the explosion to punch through blocks as well as the blast radius.";
        explosionProbability = var2.get("general", "ExplosionProbability", 75);
        explosionProbability.comment = "The likelyhood an adjacent netherore will turn into an armed ore when one is mined. Percent chance out of 1000 (lower is less likely).";
        enableExplosions = var2.get("general", "ExplosionEnable", true);
        enableExplosions.comment = "NetherOres have a chance to explode when mined if true.";
        enableExplosionChainReactions = var2.get("general", "ExplosionChainReactEnable", true);
        enableExplosionChainReactions.comment = "NetherOre explosions can trigger more explosions if true. Does nothing if ExplosionEnable is false.";
        enableFortuneExplosions = var2.get("general", "FortuneExplosionEnable", true);
        enableFortuneExplosions.comment = "NetherOres have a higher chance to explode when mined with fortune if true.";
        enableAngryPigmen = var2.get("general", "AngryPigmenEnable", true);
        enableAngryPigmen.comment = "If true, when NetherOres are mined, nearby pigmen become angry to the player.";
        silkyStopsPigmen = var2.get("general", "SilkyAngryPigmenEnable", false);
        silkyStopsPigmen.comment = "If true, when NetherOres are mined with Silk Touch, nearby pigmen become angry to the player.";
        enableMobsAngerPigmen = var2.get("general", "MobsAngerPigmen", true);
        enableMobsAngerPigmen.comment = "If true, any entity not a player exploding a NetherOre will anger nearby pigmen. This only accounts for exploding, entities breaking the blocks normally will still anger pigmen.";
        hellFishMaxHealth = var2.get("general", "HellFish.MaxHealth", 12.5, null, 8.0, Double.MAX_VALUE);
        hellFishMaxHealth.comment = "The maximum health a HellFish will have when spawned.";
        enableSmeltToOres = var2.get("Processing.Enable", "SmeltToOre", true);
        enableSmeltToOres.comment = "Set this to false to remove smelting NetherOres to ores (i.e., nether iron ore -> 2x normal iron ore).\nInstead, ores will smelt to ingots or some other appropriate item.";
        enableStandardFurnaceRecipes = var2.get("Processing.Enable", "StandardFurnaceRecipes", true);
        enableStandardFurnaceRecipes.comment = "Set this to false to remove the standard furnace recipes (i.e., nether iron ore -> normal iron ore).";
        enableMaceratorRecipes = var2.get("Processing.Enable", "MaceratorRecipes", true);
        enableMaceratorRecipes.comment = "Set this to false to remove the IC2 Macerator recipes (i.e., nether iron ore -> 4x iron dust).";
        enablePulverizerRecipes = var2.get("Processing.Enable", "PulverizerRecipes", true);
        enablePulverizerRecipes.comment = "Set this to false to remove the TE Pulvierzer recipes (i.e., nether iron ore -> 4x iron dust).";
        enableInductionSmelterRecipes = var2.get("Processing.Enable", "InductionSmelterRecipes", true);
        enableInductionSmelterRecipes.comment = "Set this to false to remove the TE Induction Smelter recipes (i.e., nether iron ore -> 2x normal iron ore).";
        enableGrinderRecipes = var2.get("Processing.Enable", "GrinderRecipes", true);
        enableGrinderRecipes.comment = "Set this to false to remove the AE Grind Stone recipes (i.e., nether iron ore -> 4x iron dust).";
        forceOreSpawn = var2.get("WorldGen.Enable", "ForceOreSpawn", false);
        forceOreSpawn.comment = "If true, will spawn nether ores regardless of if a furnace or macerator recipe was found. If false, at least one of those two must be found to spawn the ore.";
        worldGenAllDimensions = var2.get("WorldGen.Enable", "AllDimensionWorldGen", false);
        worldGenAllDimensions.comment = "If true, Nether Ores worldgen will run in all dimensions instead of just the Nether. It will still require netherrack to place ores.";
        enableWorldGen = var2.get("WorldGen.Enable", "OreGen", true);
        enableWorldGen.comment = "If true, Nether Ores oregen will run and places ores in the world where appropriate. Only disable this if you intend to use the ores with a custom ore generator. (overrides per-ore forcing; hellfish still generate if enabled)";
        enableHellQuartz = var2.get("WorldGen.Enable", "OverrideNetherQuartz", true)
            .setRequiresMcRestart(true);
        enableHellQuartz.comment = "If true, Nether Quartz ore will be a NetherOre and will follow the same rules as all other NetherOres.";
        hellFishFromOre = var2.get("WorldGen.HellFish", "EnableSpawningFromOre", false);
        hellFishFromOre.comment = "If true, Hellfish will spawn from broken NetherOres.";
        hellFishFromOreChance = var2.get("WorldGen.HellFish", "SpawningFromOreChance", 1000);
        hellFishFromOreChance.comment = "The chance out of 10000 that a broken ore will spawn a hellfish.";
        hellFishPerChunk = var2.get("WorldGen.HellFish", "GroupsPerChunk", 9);
        hellFishPerChunk.comment = "The maximum number of hellfish veins per chunk.";
        hellFishPerGroup = var2.get("WorldGen.HellFish", "BlocksPerGroup", 12);
        hellFishPerGroup.comment = "The maximum number of hellfish blocks per vein.";
        enableHellfish = var2.get("WorldGen.HellFish", "Enable", true);
        enableHellfish.comment = "If true, Hellfish will spawn in the Nether. Note that setting this false will not kill active Hellfish mobs.";
        hellFishMinY = var2.get("WorldGen.HellFish", "MinY", 1);
        hellFishMaxY = var2.get("WorldGen.HellFish", "MaxY", 127);
        if (hellFishMinY.getInt() >= hellFishMaxY.getInt()) {
            hellFishMinY.set(hellFishMaxY.getInt() - 1);
        }

        hellFishRetrogen = var2.get("WorldGen.HellFish", "Retrogen", true, "Retroactively generate HellFish");

        for (Ores var6 : Ores.values()) {
            var6.loadConfig(var2);
        }

        overrideOres = var2.getCategory("Overrides");
        overrideOres.setComment(
            "A set of blocks from other mods to override to act like NetherOres.\nThis does not include controlling oregen, or recipes; only behavior when mined or destroyed.");
        var2.save();
    }

    @SubscribeEvent
    public void registerOreEvent(OreRegisterEvent var1) {
        this.registerOreDictionaryEntry(var1.Name, var1.Ore);
    }

    private void registerOreDictionaryEntry(String var1, ItemStack var2) {
        for (Ores var6 : Ores.values()) {
            if (enableSmeltToOres.getBoolean(true)) {
                this.registerOreDictOre(var6, var1, var2);
            }

            this.registerOreDictSmelt(var6, var1, var2);
            this.registerOreDictDust(var6, var1, var2);
            this.registerOreDictGem(var6, var1, var2);
        }
    }

    private void registerOreDictOre(Ores var1, String var2, ItemStack var3) {
        if (!var1.isRegisteredSmelting() && var1.getOreName()
            .equals(var2)) {
            var1.registerSmelting(var3);
        }
    }

    private void registerOreDictSmelt(Ores var1, String var2, ItemStack var3) {
        if (!var1.isRegisteredSmelting() && var1.getSmeltName()
            .equals(var2)) {
            var1.registerSmelting(var3);
        }
    }

    private void registerOreDictDust(Ores var1, String var2, ItemStack var3) {
        if (!var1.isRegisteredMacerator() && var1.getDustName()
            .equals(var2)) {
            var1.registerPulverizing(var3);
        }
    }

    private void registerOreDictGem(Ores var1, String var2, ItemStack var3) {
        if (!var1.isRegisteredMacerator() && var1.getAltName()
            .equals(var2)) {
            var1.registerPulverizing(var3);
        }
    }

    public String getModId() {
        return MODID;
    }

    public String getModName() {
        return MODNAME;
    }

    public String getModVersion() {
        return Tags.VERSION;
    }
}
