package powercrystals.netherores.client.nei;

import net.minecraft.item.ItemStack;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import powercrystals.netherores.NetherOresCore;
import powercrystals.netherores.Tags;
import powercrystals.netherores.ores.BlockNetherOres;
import powercrystals.netherores.ores.Ores;

/**
 * Optional client-side Not Enough Items integration.
 * <p>
 * GTNH NEI auto-discovers {@link IConfigureNEI} implementations and calls
 * {@link #loadConfig()}. Every NetherOre variant (and its representative raw drop) is
 * registered into the NEI item list so it is always discoverable in search.
 * <p>
 * NetherOres has no hard dependency on NEI; this class is only instantiated by NEI on
 * the client, and NEI is a {@code compileOnly} dependency.
 */
public class NetherOresNEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        for (Ores ore : Ores.values()) {
            ItemStack oreStack = new ItemStack(NetherOresCore.getOreBlock(ore.getBlockIndex()), 1, ore.getMetadata());
            API.addItemListEntry(oreStack.copy());

            ItemStack drop = BlockNetherOres.getRepresentativeDrop(ore);
            if (drop != null && !drop.isItemEqual(oreStack)) {
                API.addItemListEntry(drop.copy());
            }
        }
    }

    @Override
    public String getName() {
        return "NetherOres";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
