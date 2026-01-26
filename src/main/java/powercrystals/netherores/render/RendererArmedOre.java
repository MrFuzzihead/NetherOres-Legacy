package powercrystals.netherores.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import powercrystals.netherores.entity.EntityArmedOre;

@SideOnly(Side.CLIENT)
public class RendererArmedOre extends Render {

    public RendererArmedOre() {
        super.shadowSize = 0.0F;
    }

    public void renderArmedOre(EntityArmedOre var1, double var2, double var4, double var6, float var8, float var9) {}

    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9) {
        this.renderArmedOre((EntityArmedOre) var1, var2, var4, var6, var8, var9);
    }

    protected ResourceLocation getEntityTexture(Entity var1) {
        return null;
    }
}
