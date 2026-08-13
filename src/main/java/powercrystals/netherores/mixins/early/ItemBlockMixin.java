package powercrystals.netherores.mixins.early;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link ItemBlock}'s private {@code field_150939_a} block field so
 * {@code BlockNetherOverrideOre} can retarget the ItemBlock without reflection.
 * Cast instances to this interface and call {@link #setBlockInstance(Block)}.
 */
@Mixin(ItemBlock.class)
public interface ItemBlockMixin {

    @Accessor("field_150939_a")
    void setBlockInstance(Block block);
}
