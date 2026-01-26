package powercrystals.netherores.ores;

import appeng.api.AEApi;
import appeng.api.features.IGrinderEntry;
import appeng.api.features.IGrinderRegistry;
import cofh.api.modhelpers.ThermalExpansionHelper;
import cofh.asm.relauncher.Strippable;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.Recipes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import powercrystals.netherores.NetherOresCore;

public enum Ores {
   Coal(8, 16, 2, 5),
   Diamond(4, 3, 2, 5, true),
   Gold(8, 6, 2, 4),
   Iron(8, 8, 2, 4),
   Lapis(6, 6, 2, 24, true),
   Redstone(6, 8, 2, 21, "dust", true),
   Copper(8, 8, 2, 4),
   Tin(8, 8, 2, 4),
   Emerald(3, 2, 2, 5, true),
   Silver(6, 4, 2, 4),
   Lead(6, 6, 2, 4),
   Uranium(3, 2, 2, 4, "crushed"),
   Nikolite(8, 4, 2, 21, "dust", true),
   Ruby(6, 3, 2, 5, true),
   Peridot(6, 3, 2, 5, true),
   Sapphire(6, 3, 2, 5, true),
   Platinum(1, 3, 2, 4),
   Nickel(4, 6, 2, 4),
   Steel(3, 4, 2, 4),
   Iridium(1, 2, 2, 4, "drop"),
   Osmium(8, 7, 2, 4),
   Sulfur(12, 12, 2, 24, false),
   Titanium(3, 2, 2, 4),
   Mithril(6, 6, 2, 4),
   Adamantium(5, 4, 2, 4),
   Rutile(3, 4, 2, 4),
   Tungsten(8, 8, 2, 4),
   Amber(5, 6, 2, 5, true),
   Tennantite(8, 8, 2, 4),
   Salt(5, 5, 2, 12, "food", true),
   Saltpeter(6, 4, 2, 10, false),
   Magnesium(4, 5, 2, 8, "crushed");

   private int _blockIndex;
   private int _metadata;
   private String _primary;
   private String _secondary;
   private boolean _registeredSmelting;
   private boolean _registeredMacerator;
   private boolean _oreGenDisable = false;
   private boolean _oreGenForced = false;
   private boolean _retroGenEnabled = true;
   private int _oreGenMinY = 1;
   private int _oreGenMaxY = 127;
   private int _oreGenGroupsPerChunk = 6;
   private int _oreGenBlocksPerGroup = 14;
   private int _smeltCount;
   private int _pulvCount;
   private int _miningLevel;

   private Ores(int var3, int var4, int var5, int var6, boolean var7) {
      this(var3, var4, var5, var6, var7 ? "gem" : "crystal", var7);
   }

   private Ores(int var3, int var4, int var5, int var6, String var7, boolean var8) {
      this(var3, var4, var5, var6, var7, var7);
   }

   private Ores(int var3, int var4, int var5, int var6) {
      this(var3, var4, var5, var6, null, null);
   }

   private Ores(int var3, int var4, int var5, int var6, String var7) {
      this(var3, var4, var5, var6, null, var7);
   }

   private Ores(int var3, int var4, int var5, int var6, String var7, String var8) {
      int var9 = this.ordinal();
      this._blockIndex = var9 / 16;
      this._metadata = var9 % 16;
      this._oreGenGroupsPerChunk = var3;
      this._oreGenBlocksPerGroup = var4;
      this._smeltCount = var5;
      this._pulvCount = var6;
      this._miningLevel = 2;
      this._primary = var7 != null ? var7 : "ingot";
      this._secondary = var8 != null ? var8 : "crystalline";
   }

   public int getBlockIndex() {
      return this._blockIndex;
   }

   public int getMetadata() {
      return this._metadata;
   }

   public String getOreName() {
      return "ore" + this.name();
   }

   public String getSmeltName() {
      return this._primary + this.name();
   }

   public String getDustName() {
      return "dust" + this.name();
   }

   public String getAltName() {
      return this._secondary + this.name();
   }

   public boolean isRegisteredSmelting() {
      return this._registeredSmelting;
   }

   public boolean isRegisteredMacerator() {
      return this._registeredMacerator;
   }

   public int getMaxY() {
      return this._oreGenMaxY;
   }

   public int getMinY() {
      return this._oreGenMinY;
   }

   public boolean getRetroGen() {
      return this._retroGenEnabled;
   }

   public int getGroupsPerChunk() {
      return this._oreGenGroupsPerChunk;
   }

   public int getBlocksPerGroup() {
      return this._oreGenBlocksPerGroup;
   }

   public boolean getDisabled() {
      return this._oreGenDisable;
   }

   public boolean getForced() {
      return this._oreGenForced;
   }

   public int getSmeltCount() {
      return this._smeltCount;
   }

   public int getMaceCount() {
      return this._pulvCount;
   }

   public ItemStack getItemStack(int var1) {
      return new ItemStack(NetherOresCore.getOreBlock(this._blockIndex), var1, this._metadata);
   }

   public void load() {
      NetherOresCore.getOreBlock(this._blockIndex).setHarvestLevel("pickaxe", this._miningLevel, this._metadata);
      if (this._oreGenForced | !this._oreGenDisable) {
         ItemStack var1 = this.getItemStack(1);
         OreDictionary.registerOre("oreNether" + this.name(), var1);
         GameRegistry.registerCustomItemStack("netherOresBlock" + this.name(), var1);
         GameRegistry.registerCustomItemStack(this.name(), var1);
      }
   }

   public void registerSmelting(ItemStack var1) {
      if (!this._registeredSmelting) {
         this._registeredSmelting = true;
         if (NetherOresCore.enableStandardFurnaceRecipes.getBoolean(true)) {
            ItemStack var2 = var1.copy();
            var2.stackSize = this._smeltCount;
            FurnaceRecipes.smelting().func_151394_a(this.getItemStack(1), var2, 1.0F);
         }

         if (NetherOresCore.enableInductionSmelterRecipes.getBoolean(true) && Loader.isModLoaded("ThermalExpansion")) {
            ItemStack var10 = this.getItemStack(1);
            ItemStack var3 = new ItemStack(Blocks.sand);
            ItemStack var4 = GameRegistry.findItemStack("ThermalExpansion", "slagRich", 1);
            ItemStack var5 = GameRegistry.findItemStack("ThermalExpansion", "slag", 1);
            ItemStack var6 = var1.copy();
            int var7 = this._smeltCount;
            if (!NetherOresCore.enableSmeltToOres.getBoolean(true)) {
               var7 *= 2;
            }

            var6.stackSize = var7;
            ItemStack var8 = var1.copy();
            int var9 = var7 + (int)Math.ceil(var7 / 3.0F);
            var8.stackSize = var9;
            ThermalExpansionHelper.addSmelterRecipe(1600 * var7, var10, var3, var6, var4, 10);
            ThermalExpansionHelper.addSmelterRecipe(2400 * var9, var10, var4, var8, var5, 100);
         }
      }
   }

   public void registerPulverizing(ItemStack var1) {
      if (!this._registeredMacerator) {
         this._registeredMacerator = true;
         if (NetherOresCore.enableMaceratorRecipes.getBoolean(true) && Loader.isModLoaded("IC2")) {
            this.registerMacerator(var1);
         }

         if (NetherOresCore.enablePulverizerRecipes.getBoolean(true) && Loader.isModLoaded("ThermalExpansion")) {
            ItemStack var2 = this.getItemStack(1);
            ItemStack var3 = var1.copy();
            ItemStack var4 = new ItemStack(Blocks.netherrack);
            var3.stackSize = this._pulvCount;
            var4.stackSize = 1;
            ThermalExpansionHelper.addPulverizerRecipe(3200, var2, var3, var4, 15);
         }

         if (NetherOresCore.enableGrinderRecipes.getBoolean(true) && Loader.isModLoaded("appliedenergistics2")) {
            this.registerAEGrinder(var1.copy());
         }
      }
   }

   @Strippable({"mod:IC2"})
   private void registerMacerator(ItemStack var1) {
      ItemStack var2 = this.getItemStack(1);
      ItemStack var3 = var1.copy();
      var3.stackSize = this._pulvCount;
      Recipes.macerator.addRecipe(new RecipeInputItemStack(var2), null, new ItemStack[]{var3.copy()});
   }

   @Strippable({"mod:appliedenergistics2"})
   private void registerAEGrinder(ItemStack var1) {
      ItemStack var2 = var1.copy();
      var2.stackSize = this._pulvCount;
      IGrinderRegistry var3 = AEApi.instance().registries().grinder();

      for (ItemStack var5 : OreDictionary.getOres(this.getOreName())) {
         IGrinderEntry var6 = var3.getRecipeForInput(var5);
         if (var6 != null) {
            var3.addRecipe(this.getItemStack(1), var2, var6.getEnergyCost() * 2);
            return;
         }
      }

      var3.addRecipe(this.getItemStack(1), var2, 16);
   }

   public void loadConfig(Configuration var1) {
      String var2 = "WorldGen.Ores." + this.name();
      this._oreGenMaxY = var1.get(var2, "MaxY", this._oreGenMaxY).setRequiresMcRestart(true).getInt();
      this._oreGenMinY = var1.get(var2, "MinY", this._oreGenMinY).setRequiresMcRestart(true).getInt();
      if (this._oreGenMinY >= this._oreGenMaxY) {
         this._oreGenMinY = this._oreGenMaxY - 1;
         var1.get(var2, "MinY", this._oreGenMinY).set(this._oreGenMinY);
      }

      this._oreGenGroupsPerChunk = var1.get(var2, "GroupsPerChunk", this._oreGenGroupsPerChunk).setRequiresMcRestart(true).getInt();
      this._oreGenBlocksPerGroup = var1.get(var2, "BlocksPerGroup", this._oreGenBlocksPerGroup).setRequiresMcRestart(true).getInt();
      this._oreGenDisable = var1.get(var2, "Disable", false, "Disables generation of " + this.name() + " (overrides global ForceOreSpawn)")
         .setRequiresMcRestart(true)
         .getBoolean(false);
      this._oreGenForced = var1.get(var2, "Force", false, "Force " + this.name() + " to generate (overrides Disable)")
         .setRequiresMcRestart(true)
         .getBoolean(false);
      this._miningLevel = var1.get(var2, "MiningLevel", this._miningLevel, "The pickaxe level required to mine " + this.name())
         .setRequiresMcRestart(true)
         .getInt();
      this._retroGenEnabled = var1.get(var2, "Retrogen", true, "Retroactively generate " + this.name() + " if enabled in CoFHCore")
         .setRequiresMcRestart(true)
         .getBoolean(true);
      var2 = "Processing.Ores." + this.name();
      this._smeltCount = var1.get(var2, "SmeltedCount", this._smeltCount, "Output from smelting " + this.name()).setRequiresMcRestart(true).getInt();
      this._primary = var1.get(
            var2,
            "PrimaryOrePrefix",
            this._primary,
            "Output from smelting "
               + this.name()
               + " if ore"
               + this.name()
               + " is not found or SmeltToOre is false (i.e., "
               + this._primary
               + this.name()
               + ")"
         )
         .setRequiresMcRestart(true)
         .getString();
      this._pulvCount = var1.get(var2, "PulverizedCount", this._pulvCount, "Output from grinding " + this.name()).setRequiresMcRestart(true).getInt();
      this._secondary = var1.get(
            var2,
            "AlternateOrePrefix",
            this._secondary,
            "Output from grinding " + this.name() + " if dust" + this.name() + " is not found (i.e., " + this._secondary + this.name() + ")"
         )
         .setRequiresMcRestart(true)
         .getString();
   }

   public void postConfig(Configuration var1) {
      String var2 = "WorldGen.Ores." + this.name();
      this._oreGenDisable = this._oreGenDisable | !(this._registeredSmelting | this._registeredMacerator);
      if (!var1.get(var2, "Disable", this._oreGenDisable, "Disables generation of " + this.name() + " (overrides global ForceOreSpawn)").wasRead()) {
         var1.get(var2, "Disable", this._oreGenDisable, "Disables generation of " + this.name() + " (overrides global ForceOreSpawn)").set(this._oreGenDisable);
      }
   }
}
