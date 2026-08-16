package powercrystals.netherores.mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // Read the Javadoc of IMixins and MixinBuilder for further information
    // You should declare all of your mixins early and late in this same enum
    // All NetherOres mixins target vanilla Minecraft classes, so they must be
    // applied early and are common to both client and server.
    MINECRAFT(new MixinBuilder().setPhase(Phase.EARLY)
        .addCommonMixins("EntityPigZombieMixin", "EntitySilverfishMixin", "ItemBlockMixin"));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
