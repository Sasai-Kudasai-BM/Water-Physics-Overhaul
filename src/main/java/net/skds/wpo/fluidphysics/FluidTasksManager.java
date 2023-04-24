package net.skds.wpo.fluidphysics;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.skds.core.api.IWWSG;
import net.skds.core.api.IWorldExtended;

public class FluidTasksManager {

	public static void addFluidTask(ServerWorld w, BlockPos pos, BlockState state) {		
		IWWSG wwsg = ((IWorldExtended) w).getWWS();
		WorldWorkSet wws = (WorldWorkSet) wwsg.getTyped(WorldWorkSet.class);

		FluidTask task = new FluidTask.DefaultTask(wws, pos.asLong());
		WorldWorkSet.pushTask(task);
		//System.out.println(pos);
	}
}