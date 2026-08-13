package powercrystals.netherores.mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // Read the Javadoc of IMixins and MixinBuilder for further information
    // You should declare all of your mixins early and late in this same enum
    MINECRAFT(new MixinBuilder().setPhase(Phase.EARLY)
        .addCommonMixins("MinecraftMixin")),

    NOTENOUGHITEMS(new MixinBuilder().setPhase(Phase.LATE)
        .addClientMixins("examplemod.ExampleMixin")
        .addRequiredMod(TargetMods.EXAMPLEMOD));

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
