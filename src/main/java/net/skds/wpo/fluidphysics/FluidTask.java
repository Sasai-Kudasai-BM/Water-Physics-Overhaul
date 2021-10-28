package net.skds.wpo.fluidphysics;

import com.google.common.util.concurrent.AtomicDouble;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.skds.core.api.multithreading.ITaskRunnable;
import net.skds.wpo.util.TaskBlocker;

public class FluidTask implements ITaskRunnable {

	private static AtomicDouble uuid = new AtomicDouble();

	public int worker = -1;

	public final WorldWorkSet owner;
	public final long pos;
	public final double priority;

	private int time = 0;

	public FluidTask(WorldWorkSet owner, long pos, int time) {
		this.owner = owner;
		this.pos = pos;

		double id = uuid.addAndGet(1E-6);

		this.priority = owner.glob.getSqDistToNBP(BlockPos.fromLong(pos)) + id;
	}

	public FluidTask(WorldWorkSet owner, CompoundNBT nbt) {
		this.owner = owner;
		this.pos = nbt.getLong("p");
		this.priority = nbt.getDouble("w");
		this.time = nbt.getInt("t");
	}

	public void tick() {
		time--;
	}

	public boolean tickNow() {
		return time <= 0;
	}

	public CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putDouble("w", priority);
		nbt.putLong("p", pos);
		nbt.putInt("t", time);
		return nbt;
	}

	@Override
	public boolean revoke(World wr) {
		World w = owner.world;
		if (w != wr) {
			return false;
		}
		BlockPos pos2 = BlockPos.fromLong(pos);
		w.getPendingFluidTicks().scheduleTick(pos2, w.getFluidState(pos2).getFluid(), 2);
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

	public int getTime() {
		return time;
	}

	@Override
	public void run() {
		FFluidDefault t = new FFluidDefault(owner.world, BlockPos.fromLong(pos), owner, FFluidBasic.Mode.DEFAULT, worker);
		t.run();
		if (worker != -1) {
			TaskBlocker.finish(worker);
		}
	}
}