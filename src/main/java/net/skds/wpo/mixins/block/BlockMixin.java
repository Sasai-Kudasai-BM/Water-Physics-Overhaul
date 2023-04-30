package net.skds.wpo.mixins.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.skds.wpo.util.interfaces.IBaseWL;

@Mixin(value = Block.class)
public class BlockMixin {

	@Shadow
	private BlockState defaultBlockState;

	@Overwrite
	protected final void registerDefaultState(BlockState state) {
		if (this instanceof IBaseWL && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			this.defaultBlockState = state.setValue(BlockStateProperties.WATERLOGGED, false);
		} else {
			this.defaultBlockState = state;
		}
	}
}