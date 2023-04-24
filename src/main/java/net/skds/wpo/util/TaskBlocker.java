package net.skds.wpo.util;

import net.minecraft.util.math.BlockPos;
import net.skds.wpo.fluidphysics.FluidTask;

public class TaskBlocker {

	private static FluidTask[] blockMap = new FluidTask[4];

	public static void finish(int i) {
		blockMap[i] = null;
	}

	public static boolean test(int i, FluidTask task) {
		blockMap[i] = null;
		if (task == null) {
			return false;
		}
		int ind = -1;
		synchronized (blockMap) {
			for (FluidTask t2 : blockMap) {
				ind++;
				if (ind == i || t2 == null) {
					continue;
				}
				// System.out.println(task.owner);
				// System.out.println(ind);
				boolean busy = task.owner == t2.owner;
				busy = busy && testBusyPos(task.pos, t2.pos);
				// System.out.println(busy + " " + ind + " " + BlockPos.fromLong(task.pos));
				if (busy) {
					// System.out.println(busy + " " + ind + " " + BlockPos.fromLong(task.pos));
					return false;
				}
			}
			blockMap[i] = task;
		}

		return true;
	}

	private static boolean testBusyPos(long n, long o) {
		if (n == o) {
			return true;
		}
		BlockPos np = BlockPos.of(n);
		BlockPos op = BlockPos.of(o);
		int dx = Math.abs(np.getX() - op.getX());
		if (dx > 2) {
			return false;
		}
		int dz = Math.abs(np.getZ() - op.getZ());
		if (dx + dz > 2) {
			return false;
		}
		int dy = Math.abs(np.getY() - op.getY());

		return dx + dy + dz <= 2;
	}
}