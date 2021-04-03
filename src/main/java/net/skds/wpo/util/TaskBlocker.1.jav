package net.skds.wpo.util;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.math.BlockPos;
import net.skds.wpo.fluidphysics.FluidTask;

public class TaskBlocker {

	private static ConcurrentHashMap<Integer, FluidTask> blockMap = new ConcurrentHashMap<>();

	public static void finish(int i) {
		blockMap.remove(i);
	}

	public static boolean test(int i, FluidTask task) {
		if (task == null) {
			return false;
		}

		for (Entry<Integer, FluidTask> e : blockMap.entrySet()) {
			int ind = e.getKey();
			FluidTask t2 = e.getValue();
			if (ind == i || t2 == null) {
				continue;
			}
			//System.out.println(task.owner);
			//System.out.println(ind);
			boolean busy = task.owner == t2.owner;
			busy = busy && testBusyPos(task.pos, t2.pos);
			if (busy) {
				return false;
			}
		}
		blockMap.put(i, task);	

		return true;
	}

	private static boolean testBusyPos(long n, long o) {
		if (n == o) {
			return true;
		}
		BlockPos np = BlockPos.fromLong(n);
		BlockPos op = BlockPos.fromLong(o);
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