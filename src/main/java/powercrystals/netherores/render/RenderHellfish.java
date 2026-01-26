package powercrystals.netherores.render;

import net.minecraft.client.renderer.entity.RenderSilverfish;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderHellfish extends RenderSilverfish {

    private static final ResourceLocation hellfishTextures = new ResourceLocation(
        "netherores:textures/mob/hellfish.png");

    protected ResourceLocation getEntityTexture(Entity var1) {
        return hellfishTextures;
    }
}
