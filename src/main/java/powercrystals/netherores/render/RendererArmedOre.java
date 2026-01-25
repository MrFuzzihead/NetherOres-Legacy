package powercrystals.netherores.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import powercrystals.netherores.entity.EntityArmedOre;

@SideOnly(Side.CLIENT)
public class RendererArmedOre extends Render {
   public RendererArmedOre() {
      super.field_76989_e = 0.0F;
   }

   public void renderArmedOre(EntityArmedOre var1, double var2, double var4, double var6, float var8, float var9) {
   }

   public void func_76986_a(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      this.renderArmedOre((EntityArmedOre)var1, var2, var4, var6, var8, var9);
   }

   protected ResourceLocation func_110775_a(Entity var1) {
      return null;
   }
}
