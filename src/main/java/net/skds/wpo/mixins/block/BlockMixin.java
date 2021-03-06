package net.skds.wpo.mixins.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.skds.wpo.util.interfaces.IBaseWL;

@Mixin(value = Block.class)
public class BlockMixin {

	@Shadow
	private BlockState defaultState;

	@Overwrite
	protected final void setDefaultState(BlockState state) {
		if (this instanceof IBaseWL && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			this.defaultState = state.with(BlockStateProperties.WATERLOGGED, false);
		} else {
			this.defaultState = state;
		}
	}
}