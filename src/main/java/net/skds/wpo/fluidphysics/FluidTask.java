package net.skds.wpo.fluidphysics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.skds.core.api.multithreading.ITaskRunnable;
import net.skds.wpo.util.TaskBlocker;

public abstract class FluidTask implements ITaskRunnable {

	private static double uuid = 0;

	public int worker = -1;

	public final WorldWorkSet owner;
	public final long pos;
	public final double priority;

	public FluidTask(WorldWorkSet owner, long pos) {
		//System.out.println(BlockPos.fromLong(pos));
		this.owner = owner;
		this.pos = pos;
		uuid = uuid + 1.0E-6;
		if (uuid >=1) {
			uuid = 0;
		}
		this.priority = owner.glob.getSqDistToNBP(BlockPos.of(pos)) + uuid;
	}

	@Override
	public boolean revoke(Level wr) {
		Level w = owner.world;
		if (w != wr) {
			return false;
		}
		BlockPos pos2 = BlockPos.of(pos);
		w.getLiquidTicks().scheduleTick(pos2, w.getFluidState(pos2).getType(), 2);
		return true;
	}
	
	@Override
	public double getPriority() {
		return priority;
	}

	@Override
	public int getSubPriority() {
		return 0;
	}

	public static class DefaultTask extends FluidTask {

		public DefaultTask(WorldWorkSet owner, long pos) {
			super(owner, pos);
		}

		@Override
		public void run() {
			if (owner.excludedTasks.contains(pos)) {
				return;
			}
			//Events.c++;
			
			//System.out.println(BlockPos.fromLong(pos));
			//FFluidDefaultV2 t = new FFluidDefaultV2(owner.world, BlockPos.fromLong(pos), owner, FFluidBasic.Mode.DEFAULT);
			FFluidDefault t = new FFluidDefault(owner.world, BlockPos.of(pos), owner, FFluidBasic.Mode.DEFAULT, worker);
			t.run();
			if (worker != -1) {
				TaskBlocker.finish(worker);
			}
			//t = null;
		}
	}

	public static class EQTask extends FluidTask {

		public EQTask(WorldWorkSet owner, long pos) {
			super(owner, pos);
		}

		@Override
		public void run() {
			//System.out.println(BlockPos.fromLong(pos));
			//FFluidEQV2 t = new FFluidEQV2(owner.world, BlockPos.fromLong(pos), owner, FFluidBasic.Mode.DEFAULT);
			FFluidEQ t = new FFluidEQ(owner.world, BlockPos.of(pos), owner, FFluidBasic.Mode.EQUALIZER, worker);
			t.run();
			if (worker != -1) {
				TaskBlocker.finish(worker);
			}
			//t = null;
		}
	}
}