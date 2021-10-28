package net.skds.wpo.fluidphysics;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;

public class FluidCallback {

	public static final FluidCallback EMPTY = new FluidCallback(Fluids.EMPTY.getDefaultState(), Fluids.EMPTY.getDefaultState(), Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState());

	public final FluidState oldFS, newFS;
	public final BlockState oldBS, newBS;

	public FluidCallback(FluidState oldFS, FluidState newFS, BlockState oldBS, BlockState newBS) {
		this.newBS = newBS;
		this.newFS = newFS;
		this.oldBS = oldBS;
		this.oldFS = oldFS;
	}
}
