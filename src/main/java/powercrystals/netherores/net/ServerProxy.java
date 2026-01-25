package powercrystals.netherores.net;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Post;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Pre;

public class ServerProxy {
   private static HashMap<World, HashSet<ChunkCoordIntPair>> chunks = new HashMap<>();

   public static boolean isChunkPopulating(World var0, int var1, int var2, int var3) {
      return chunks.containsKey(var0) && chunks.get(var0).contains(new ChunkCoordIntPair(var1 >> 4, var3 >> 4));
   }

   public void load() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void evt(Pre var1) {
      if (!chunks.containsKey(var1.world)) {
         chunks.put(var1.world, new HashSet<>());
      }

      chunks.get(var1.world).add(new ChunkCoordIntPair(var1.chunkX, var1.chunkZ));
   }

   @SubscribeEvent
   public void evt(Post var1) {
      if (chunks.containsKey(var1.world)) {
         chunks.get(var1.world).remove(new ChunkCoordIntPair(var1.chunkX, var1.chunkZ));
      }
   }

   @SubscribeEvent
   public void evt(cofh.asmhooks.event.ModPopulateChunkEvent.Pre var1) {
      if (!chunks.containsKey(var1.world)) {
         chunks.put(var1.world, new HashSet<>());
      }

      chunks.get(var1.world).add(new ChunkCoordIntPair(var1.chunkX, var1.chunkZ));
   }

   @SubscribeEvent
   public void evt(cofh.asmhooks.event.ModPopulateChunkEvent.Post var1) {
      if (chunks.containsKey(var1.world)) {
         chunks.get(var1.world).remove(new ChunkCoordIntPair(var1.chunkX, var1.chunkZ));
      }
   }
}
