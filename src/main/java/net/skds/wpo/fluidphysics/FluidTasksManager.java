package net.skds.wpo.fluidphysics;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.skds.core.api.IWorldExtended;
import net.skds.core.util.blockupdate.WWSGlobal;

public class FluidTasksManager {

	public static void addFluidTask(ServerWorld w, BlockPos pos, BlockState state) {		
		WWSGlobal wwsg = ((IWorldExtended) w).getWWS();
		WorldWorkSet wws = (WorldWorkSet) wwsg.getTyped(WorldWorkSet.class);

		FluidTask task = new FluidTask.DefaultTask(wws, pos.toLong());
		WorldWorkSet.pushTask(task);
		//System.out.println(pos);
	}
}