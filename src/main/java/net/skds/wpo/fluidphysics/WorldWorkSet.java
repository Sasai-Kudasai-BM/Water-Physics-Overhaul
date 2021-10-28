package net.skds.wpo.fluidphysics;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.skds.core.Events;
import net.skds.core.api.IWWS;
import net.skds.core.api.multithreading.ITaskRunnable;
import net.skds.core.multithreading.MTHooks;
import net.skds.core.util.blockupdate.WWSGlobal;
import net.skds.wpo.util.TaskBlocker;

public class WorldWorkSet implements IWWS {
	public final WWSGlobal glob;
	public final ServerWorld world;

	public ConcurrentSet<Long> excludedTasks = new ConcurrentSet<>();

	private ConcurrentSet<Long> lockedEq = new ConcurrentSet<>();
	private ConcurrentHashMap<Long, Integer> ntt = new ConcurrentHashMap<>();
	private static final Comparator<FluidTask> comp = new Comparator<FluidTask>() {
		@Override
		public int compare(FluidTask k1, FluidTask k2) {
			if (k1.pos == k2.pos && k1.owner == k2.owner) {
				return 0;
			}
			double dcomp = (k1.getPriority() - k2.getPriority());
			int comp = (int) dcomp;
			if (comp == 0) {
				comp = dcomp > 0 ? 1 : -1;
			}
			return comp;
		}
	};
	private static ConcurrentSkipListSet<FluidTask> TASKS = new ConcurrentSkipListSet<>(comp);
	private static ConcurrentLinkedQueue<FluidTask> DELAYED_TASKS = new ConcurrentLinkedQueue<>();

	public WorldWorkSet(ServerWorld w, WWSGlobal owner) {
		world = (ServerWorld) w;
		glob = owner;
	}

	public void addNTTask(long l, int t) {
		ntt.put(l, t);
	}

	public void clearEqLock(long l) {
		lockedEq.remove(l);
	}

	public void addEQTask(long l, FlowingFluid fluid) {
		FluidTask task = new FluidTask.EQTask(this, l);
		// WWSGlobal.pushTask(task);
		TASKS.add(task);
	}

	public void addEqLock(long l) {
		lockedEq.add(l);
	}

	public boolean isEqLocked(long l) {
		return lockedEq.contains(l);
	}

	private void tickNTT(long pos, int t) {
		t--;
		if (t <= 0) {
			FluidTask task = new FluidTask.DefaultTask(this, pos);
			// WWSGlobal.pushTask(task);
			TASKS.add(task);
			ntt.remove(pos);
			clearEqLock(pos);
		} else {
			ntt.put(pos, t);
		}
	}

	public static ITaskRunnable nextTask(int i) {
		if (i > 3) {
			return null;
		}
		if (MTHooks.COUNTS > 0 || Events.getRemainingTickTimeMilis() > MTHooks.TIME) {
			MTHooks.COUNTS--;
			for (FluidTask t : DELAYED_TASKS) {
				if (TaskBlocker.test(i, t)) {
					DELAYED_TASKS.remove(t);
					t.worker = i;
					return t;
				}
			}
			boolean tested = false;
			FluidTask task;
			while ((task = TASKS.pollFirst()) != null && !tested) {
				tested = TaskBlocker.test(i, task);
				// System.out.println(tested);
				if (tested) {
					task.worker = i;
					return task;
				} else if (task != null) {
					DELAYED_TASKS.add(task);
				}
			}
		}
		return null;
	}

	public static void pushTask(FluidTask task) {
		TASKS.add(task);
	}

	// =========== Override ==========

	@Override
	public void tickIn() {
		// System.out.println(TASKS.size());
		excludedTasks.clear();
		ntt.forEach(this::tickNTT);
	}

	@Override
	public void tickOut() {
		//if (world.canSeeSky(new BlockPos(0, 255, 0))) {
		//	long t = System.currentTimeMillis() - net.skds.wpo.Events.t - 4;
		//	if (t > 0)
		//		System.out.println(net.skds.wpo.Events.c / t);
		//}
	}

	@Override
	public void close() {
		lockedEq.clear();
		ServerTickList<Fluid> stl = world.getPendingFluidTicks();
		ntt.forEach((lp, t) -> {
			BlockPos pos = BlockPos.fromLong(lp);
			stl.scheduleTick(pos, world.getFluidState(pos).getFluid(), t + 2);
		});
		ntt.clear();
		TASKS.forEach(t -> t.revoke(world));
		TASKS.clear();
		DELAYED_TASKS.forEach(t -> t.revoke(world));
		DELAYED_TASKS.clear();
	}

	@Override
	public WWSGlobal getG() {
		return glob;
	}

	@Override
	public void tickPostMTH() {		
	}

	@Override
	public void tickPreMTH() {		
	}
}