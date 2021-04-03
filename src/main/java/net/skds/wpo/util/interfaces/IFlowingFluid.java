package net.skds.wpo.util.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface IFlowingFluid {
    public void beforeReplacingBlockCustom(IWorld worldIn, BlockPos pos, BlockState state);
}