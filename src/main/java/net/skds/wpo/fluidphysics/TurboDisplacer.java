package net.skds.wpo.fluidphysics;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.skds.wpo.WPOConfig;
import net.skds.wpo.fluidphysics.FFluidStatic.FluidDisplacer2;
import net.skds.wpo.util.interfaces.IBaseWL;

public class TurboDisplacer {


	public static void markForDisplace(ServerLevel w, BlockPos pos, BlockState oldState, BlockState newState) {
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
		if (nb instanceof SimpleWaterloggedBlock && f.isSame(Fluids.WATER)) {
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
