package powercrystals.netherores.net;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Post;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Pre;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ServerProxy {

    private static HashMap<World, HashSet<ChunkCoordIntPair>> chunks = new HashMap<>();

    public static boolean isChunkPopulating(World world, int x, int y, int z) {
        HashSet<ChunkCoordIntPair> populating = chunks.get(world);
        return populating != null && populating.contains(new ChunkCoordIntPair(x >> 4, z >> 4));
    }

    public void load() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Drop the per-world bookkeeping when a world unloads so the World (and its chunk
    // set) can be GC'd instead of living forever in the static map.
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        chunks.remove(event.world);
    }

    @SubscribeEvent
    public void onPopulate(Pre event) {
        chunks.computeIfAbsent(event.world, k -> new HashSet<>())
            .add(new ChunkCoordIntPair(event.chunkX, event.chunkZ));
    }

    @SubscribeEvent
    public void onPopulate(Post event) {
        HashSet<ChunkCoordIntPair> populating = chunks.get(event.world);
        if (populating != null) {
            populating.remove(new ChunkCoordIntPair(event.chunkX, event.chunkZ));
        }
    }

    @SubscribeEvent
    public void onModPopulate(cofh.asmhooks.event.ModPopulateChunkEvent.Pre event) {
        chunks.computeIfAbsent(event.world, k -> new HashSet<>())
            .add(new ChunkCoordIntPair(event.chunkX, event.chunkZ));
    }

    @SubscribeEvent
    public void onModPopulate(cofh.asmhooks.event.ModPopulateChunkEvent.Post event) {
        HashSet<ChunkCoordIntPair> populating = chunks.get(event.world);
        if (populating != null) {
            populating.remove(new ChunkCoordIntPair(event.chunkX, event.chunkZ));
        }
    }
}
