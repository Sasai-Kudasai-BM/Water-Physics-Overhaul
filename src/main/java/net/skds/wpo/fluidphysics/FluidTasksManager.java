package net.skds.wpo.fluidphysics;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class FluidTasksManager {

	public static void onBlockAdded(ServerWorld w, BlockPos pos, BlockState state, BlockState oldState, Chunk chunk, int flags) {
		WorldWorkSet wws = WorldWorkSet.get(w);
		addTask(wws, pos);
	}

	public static void addTask(WorldWorkSet wws, BlockPos pos) {
		FluidTask task = new FluidTask(wws, pos.toLong(), 5);
		WorldWorkSet.pushTask(task);
	}

}