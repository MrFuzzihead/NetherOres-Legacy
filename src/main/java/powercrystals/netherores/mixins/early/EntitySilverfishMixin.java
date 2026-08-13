package powercrystals.netherores.mixins.early;

import net.minecraft.entity.monster.EntitySilverfish;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link EntitySilverfish}'s private {@code allySummonCooldown} field so
 * {@code EntityHellfish} can read/write it without an access transformer. Cast
 * instances to this interface and use the accessor methods.
 */
@Mixin(EntitySilverfish.class)
public interface EntitySilverfishMixin {

    @Accessor("allySummonCooldown")
    int getAllySummonCooldown();

    @Accessor("allySummonCooldown")
    void setAllySummonCooldown(int value);
}
