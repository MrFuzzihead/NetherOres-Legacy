package powercrystals.netherores.mixins.early;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPigZombie;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@link EntityPigZombie#becomeAngryAt(Entity)} so NetherOres can anger
 * pigmen without an access transformer. Cast instances to this interface and call
 * {@link #becomeAngryAt(Entity)}.
 */
@Mixin(EntityPigZombie.class)
public interface EntityPigZombieMixin {

    @Invoker("becomeAngryAt")
    void becomeAngryAt(Entity entity);
}
