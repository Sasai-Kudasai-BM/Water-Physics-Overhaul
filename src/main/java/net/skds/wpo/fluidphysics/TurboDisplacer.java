package net.skds.wpo.fluidphysics;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.skds.wpo.WPOConfig;
import net.skds.wpo.fluidphysics.FFluidStatic.FluidDisplacer2;
import net.skds.wpo.util.interfaces.IBaseWL;

public class TurboDisplacer {


	public static void markForDisplace(ServerWorld w, BlockPos pos, BlockState oldState, BlockState newState) {
		//World w = (World) w;
		//BlockPos pos = e.getPos();
		//BlockState oldState = w.getBlockState(pos);
		FluidState fs = oldState.getFluidState();
		//FluidState nfs = newState.getFluidState();
		Fluid f = fs.getType();
		//BlockState newState = e.getPlacedBlock();
		Block nb = newState.getBlock();
		int level = fs.getAmount();
		//int nlevel = nfs.getLevel();
		if (fs.isEmpty()) {
			return;
		}
		if (nb instanceof IWaterLoggable && f.isSame(Fluids.WATER)) {
			if (level == WPOConfig.MAX_FLUID_LEVEL) {
				w.setBlock(pos, FFluidStatic.getUpdatedState(newState, level, f), 3);
				return;
			} else if (nb instanceof IBaseWL) {				
				w.setBlock(pos, FFluidStatic.getUpdatedState(newState, level, f), 3);
				return;
			}
		}
		
		if (!FFluidStatic.canOnlyFullCube(newState) && nb instanceof IBaseWL && f.isSame(Fluids.WATER)) {
			newState = FFluidStatic.getUpdatedState(newState, fs.getAmount(), Fluids.WATER);
			w.setBlockAndUpdate(pos, newState);
			return;
		}

		FluidDisplacer2 displacer = new FluidDisplacer2(w, oldState);
		FFluidStatic.iterateFluidWay(10, pos, displacer);
	}
    
}
