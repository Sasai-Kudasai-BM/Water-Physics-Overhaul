package net.skds.wpo.fluidphysics;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.skds.core.Events;
import net.skds.core.api.IWWS;
import net.skds.core.api.IWorldExtended;
import net.skds.core.api.multithreading.ITaskRunnable;
import net.skds.core.multithreading.MTHooks;
import net.skds.core.network.PacketHandler;
import net.skds.core.util.blockupdate.WWSGlobal;
import net.skds.core.util.data.capability.ChunkCapabilityData;
import net.skds.wpo.network.ChunkUpdatePacket;

public class WorldWorkSet implements IWWS {
	public final WWSGlobal glob;
	public final ServerWorld world;

	public ConcurrentSet<Long> updatedChunks = new ConcurrentSet<>();

	private ConcurrentSet<Long> lockedEq = new ConcurrentSet<>();
	private static ConcurrentSkipListSet<FluidTask> TASKS = new ConcurrentSkipListSet<>(WorldWorkSet::compare);
	private static ConcurrentSkipListSet<FluidTask> TASKS_TO_EXECUTE = new ConcurrentSkipListSet<>(
			WorldWorkSet::compare);

	public WorldWorkSet(ServerWorld w, WWSGlobal owner) {
		world = (ServerWorld) w;
		glob = owner;
	}

	public static int compare(FluidTask k1, FluidTask k2) {
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

	public static void removeTasks(Collection<FluidTask> tasks) {
		TASKS.removeAll(tasks);
	}

	public void clearEqLock(long l) {
		lockedEq.remove(l);
	}

	public void addEqLock(long l) {
		lockedEq.add(l);
	}

	public boolean isEqLocked(long l) {
		return lockedEq.contains(l);
	}

	public static ITaskRunnable nextTask(int i) {
		if (i > 3) {
			return null;
		}
		if (MTHooks.COUNTS > 0 || Events.getRemainingTickTimeMilis() > MTHooks.TIME) {
			FluidTask task;
			while ((task = TASKS_TO_EXECUTE.pollFirst()) != null) {
				MTHooks.COUNTS--;
				task.worker = i;
				return task;

			}
		}
		return null;
	}

	public static void pushTask(FluidTask task) {
		TASKS_TO_EXECUTE.add(task);
	}

	// =========== Override ==========	

	@Override
	public void tickPreMTH() {
		TASKS.parallelStream().forEach(task -> {
			if (world.getChunkProvider().canTick(BlockPos.fromLong(task.pos))) {
				task.tick();
				if (task.tickNow()) {
					TASKS.remove(task);
					TASKS_TO_EXECUTE.add(task);
				}
			} else {
				
			}
		});
	}

	@Override
	public void tickPostMTH() {
	}

	@Override
	public void tickIn() {
	}

	@Override
	public void tickOut() {

		if (!world.isRemote) {
			ServerChunkProvider acp = world.getChunkProvider();

			for (long l : updatedChunks) {
				ChunkPos chunkPos = new ChunkPos(l);
				Chunk chunk = acp.getChunkNow(chunkPos.x, chunkPos.z);
				if (chunk != null && !chunk.isEmpty()) {
					ChunkCapabilityData.apply(chunk, cap -> {
						acp.chunkManager.getTrackingPlayers(chunkPos, false).parallel().forEach(p -> {
							PacketHandler.send(p, new ChunkUpdatePacket(cap));
						});
					});
				}
			}
			updatedChunks.clear();
		}

		//if (world.canSeeSky(new BlockPos(0, 255, 0))) {
		//	long t = System.currentTimeMillis() - net.skds.wpo.Events.t - 4;
		//	if (t > 0)
		//		System.out.println(net.skds.wpo.Events.c / t);
		//}
	}

	@Override
	public void close() {
		lockedEq.clear();
		TASKS.forEach(t -> t.revoke(world));
		TASKS.clear();
	}

	@Override
	public WWSGlobal getG() {
		return glob;
	}

	public static WorldWorkSet get(World w) {
		WWSGlobal wwsg = ((IWorldExtended) w).getWWS();
		return wwsg.getTyped(WorldWorkSet.class);
	}
}