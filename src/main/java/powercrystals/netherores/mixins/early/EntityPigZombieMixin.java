package powercrystals.netherores.mixins.early;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPigZombie;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@link EntityPigZombie#becomeAngryAt(Entity)} so NetherOres can anger
 * pigmen without an access transformer. Cast instances to this interface and call
 * {@link #invokeBecomeAngryAt(Entity)}.
 * <p>
 * NOTE: the invoker method name intentionally differs from the target
 * ({@code invokeBecomeAngryAt} vs {@code becomeAngryAt}). Mixin injects a bridge
 * into the target class named after the invoker method; if that name equals the
 * private target it exposes, the bridge calls itself and causes infinite recursion
 * (StackOverflowError) when invoked.
 */
@Mixin(EntityPigZombie.class)
public interface EntityPigZombieMixin {

    @Invoker("becomeAngryAt")
    void invokeBecomeAngryAt(Entity entity);
}
