package net.skds.wpo.util.interfaces;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public interface IFlowingFluid {
    public void beforeReplacingBlockCustom(LevelAccessor worldIn, BlockPos pos, BlockState state);
}