package net.skds.wpo.fluidphysics;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.skds.core.util.blockupdate.BasicExecutor;
import net.skds.core.util.blockupdate.WWSGlobal;
import net.skds.wpo.WPOConfig;
import net.skds.wpo.util.interfaces.IFlowingFluid;

public abstract class FFluidBasic extends BasicExecutor {

	protected final int worker;
	protected final Mode mode;
	protected final int MFL = WPOConfig.MAX_FLUID_LEVEL;
	protected final Fluid fluid;
	protected final ServerWorld w;
	protected final BlockPos pos;
	protected final long longpos;

	protected final WorldWorkSet castOwner;

	protected int level = 0;
	protected FluidState fs;
	protected BlockState state;

	protected FFluidBasic(ServerWorld w, BlockPos pos, Mode mode, WorldWorkSet owner, int worker) {
		super(w, owner);
		this.castOwner = owner;
		this.worker = worker;
		this.w = w;
		this.mode = mode;
		this.state = getBlockState(pos);
		this.fs = this.state.getFluidState();
		this.fluid = fs.getFluid();
		this.pos = pos;
		this.longpos = pos.toLong();
		this.level = fs.getLevel();
	}


	@SuppressWarnings("deprecation")
	@Override
	protected void applyAction(BlockPos pos, BlockState newState, BlockState oldState, ServerWorld world) {
		if (newState == oldState) {
			return;
		}
		IChunk ichunk = getChunk(pos);
		if (!(ichunk instanceof Chunk)) {
			return;
		}
		Chunk chunk = (Chunk) ichunk;
		Block block = newState.getBlock();

		BlockPos posu = pos.up();
		if (getBlockState(posu).getFluidState().isEmpty()) {
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				BlockPos posu2 = posu.offset(dir);
				if (!getBlockState(posu2).getFluidState().isEmpty()) {
					WorldWorkSet.pushTask(new FluidTask.DefaultTask(castOwner, posu2.toLong()));
				}
			}
		}

		Fluid fluid = newState.getFluidState().getFluid();
		if (fluid != Fluids.EMPTY) {
			castOwner.excludedTasks.add(longpos);
		}
		synchronized (world) {

			if (fluid != Fluids.EMPTY && !oldState.isAir() && !fluid.isEquivalentTo(oldState.getFluidState().getFluid())
					&& !(oldState.getBlock() instanceof IWaterLoggable)) {
				((IFlowingFluid) fluid).beforeReplacingBlockCustom(world, pos, oldState);
			}
			// world.markBlockRangeForRenderUpdate(pos, oldState, newState);

			if (chunk.getLocationType() != null
					&& chunk.getLocationType().isAtLeast(ChunkHolder.LocationType.TICKING)) {
				world.notifyBlockUpdate(pos, oldState, newState, 3);
			}

			world.notifyNeighborsOfStateChange(pos, block);
			if (state.hasComparatorInputOverride()) {
				world.updateComparatorOutputLevel(pos, block);
			}

			// world.onBlockStateChange(pos, oldState, newState);

			newState.updateNeighbours(world, pos, 0);

			newState.onBlockAdded(world, pos, oldState, false);

			// ServerTickList<Fluid> stl = world.getPendingFluidTicks();
			// if (oldState.getFluidState().getFluid() != fluid) {
			// stl.scheduleTick(pos, fluid, FFluidStatic.getTickRate((FlowingFluid) fluid,
			// world));
			// }

		}

		if (newState.getFluidState().isEmpty() ^ oldState.getFluidState().isEmpty()
						&& (newState.getLightValue(world, pos) != oldState.getLightValue(world, pos))) {
			world.getChunkProvider().getLightManager().checkBlock(pos);
		}
	}

	protected int getAbsoluteLevel(int y, int l) {
		return (y * MFL) + l;
	}

	@Override
	public void run() {
		if (level > 0 && (fluid instanceof FlowingFluid)) {
			execute();
		}
		WWSGlobal wwsg = owner.getG();
		banPoses.forEach(p -> wwsg.unbanPos(p.toLong()));
	}

	protected boolean flow(BlockPos pos1, BlockPos pos2, int h) {
		if (!validate(pos1)) {
			return false;
		}
		if (!validate(pos2)) {
			return false;
		}
		boolean ss = true;
		BlockState state1 = getBlockState(pos1);
		BlockState state2 = getBlockState(pos2);
		if (canOnlyFillCube(state1) || canOnlyFillCube(state2)) {
			unban(pos1);
			unban(pos2);
			return false;
		}
		point1: if (!trySwap(pos1, pos2, h, state1, state2)) {
			FluidState fs1 = state1.getFluidState();
			FluidState fs2 = state2.getFluidState();
			int l1 = fs1.getLevel();
			int l2 = fs2.getLevel();

			Fluid f1 = fs1.getFluid();
			Fluid f2 = fs2.getFluid();
			if (!f1.isEquivalentTo(f2) && !(fs1.isEmpty() || fs2.isEmpty())) {

				Direction dir = dirFromVec(pos1, pos2);
				if (fs1.canDisplace(w, pos2, f2, dir)) {

					state2 = Blocks.AIR.getDefaultState();
					fs2 = state2.getFluidState();
					l2 = 0;

					// } else if (fs2.canDisplace(w, pos1, f1, dir.getOpposite())) {
				} else {
					ss = false;
					break point1;
				}
			}

			if (h == 0) {
				int d2 = (l1 - l2) / 2;
				if (d2 == 0) {
					if (l1 == 1 && l2 == 0) {
						d2 = 1;
					} else {
						ss = false;
						break point1;
					}
				}
				l1 -= d2;
				l2 += d2;
				BlockState sn1 = getUpdatedState(state1, l1);
				BlockState sn2 = getUpdatedState(state2, l2);
				setState(pos1, sn1);
				setState(pos2, sn2);
			} else {
				int sum = l1 + l2;
				if (sum > MFL) {
					if (h > 0) {
						l1 = MFL;
						l2 = sum - MFL;
					} else {
						l2 = MFL;
						l1 = sum - MFL;
					}
				} else {
					if (h > 0) {
						l1 = sum;
						l2 = 0;
					} else {
						boolean bl = l1 > 1;
						l2 = sum;
						l1 = 0;
						if (bl) {
							l2--;
							l1++;
						}
					}
				}
				BlockState sn1 = getUpdatedState(state1, l1);
				BlockState sn2 = getUpdatedState(state2, l2);
				setState(pos1, sn1);
				setState(pos2, sn2);
			}
		}

		unban(pos1);
		unban(pos2);
		return ss;
	}

	protected boolean trySwap(BlockPos pos1, BlockPos pos2, int h, BlockState state1, BlockState state2) {
		// if (h == 0) {
		// return false;
		// }
		// FluidState fs1 = state1.getFluidState();
		// FluidState fs2 = state2.getFluidState();
		// if (fs1.isEmpty() || fs2.isEmpty()) {
		// return false;
		// }
		// Fluid f1 = fs1.getFluid();
		// Fluid f2 = fs2.getFluid();
		// if (f1.isEquivalentTo(f2)) {
		// return false;
		// } else {
		// FluidAttributes fa1 = f1.getAttributes();
		// FluidAttributes fa2 = f2.getAttributes();
		// boolean b = fa1.getDensity() > fa2.getDensity();
		// if (b && h > 0) {
		// return false;
		// } else {
		// BlockState ns1 = state2;
		// BlockState ns2 = state1;
		// setState(pos1, ns1);
		// setState(pos2, ns2);
		// return true;
		// }
		// }
		return false;
	}

	protected abstract void execute();

	protected boolean canOnlyFillCube(BlockState bs) {
		return FFluidStatic.canOnlyFullCube(bs);
	}

	protected boolean canOnlyFillCube(Block b) {
		return FFluidStatic.canOnlyFullCube(b);
	}

	protected boolean validate(BlockPos p) {
		long l = p.toLong();
		boolean ss = owner.getG().banPos(l);
		if (ss) {
			banPoses.add(p);
		} else {
			// System.out.println(p);
			// castOwner.addNTTask(l, 2);
		}
		return ss;
	}

	protected void unban(BlockPos p) {
		long l = p.toLong();
		owner.getG().unbanPos(l);
		banPoses.remove(p);
	}

	protected void addPassedEq(BlockPos addPos) {
		long l = addPos.toLong();
		castOwner.addEqLock(l);
		castOwner.addNTTask(l, FFluidStatic.getTickRate((FlowingFluid) fluid, w));
	}

	protected boolean isPassedEq(BlockPos isPos) {
		long l = isPos.toLong();
		return castOwner.isEqLocked(l);
	}

	protected boolean flowFullCubeV2(BlockPos pos1, BlockPos pos2) {
		if (!validate(pos1)) {
			return false;
		}
		if (!validate(pos2)) {
			return false;
		}
		boolean bb = false;
		BlockState state1 = getBlockState(pos1);
		BlockState state2 = getBlockState(pos2);
		int l1 = state1.getFluidState().getLevel();
		int l2 = state2.getFluidState().getLevel();
		if ((l1 == MFL && l2 == 0) || (l1 == 0 && l2 == MFL)) {
			BlockState sn1 = getUpdatedState(state1, l2);
			BlockState sn2 = getUpdatedState(state2, l1);
			setState(pos1, sn1);
			setState(pos2, sn2);
			bb = true;
		}
		unban(pos1);
		unban(pos2);
		return bb;
	}

	protected void flowFullCube(BlockPos pos2, BlockState state2) {
		if (state2 == null) {
			cancel = true;
			return;
		}
		FluidState fs2 = state2.getFluidState();
		int level2 = fs2.getLevel();

		state = getUpdatedState(state, level2);
		state2 = getUpdatedState(state2, level);
		setState(pos, state);
		setState(pos2, state2);
	}

	protected BlockState getUpdatedState(BlockState state0, int newLevel) {
		return FFluidStatic.getUpdatedState(state0, newLevel, fluid);
	}

	// ================ UTIL ================== //

	protected boolean isThisFluid(Fluid f2) {
		if (fluid == Fluids.EMPTY)
			return false;
		if (f2 == Fluids.EMPTY)
			return false;
		return fluid.isEquivalentTo(f2);
	}

	protected boolean canReach(BlockPos pos1, BlockPos pos2, BlockState state1, BlockState state2) {
		if (state1 == nullreturnstate || state2 == nullreturnstate) {
			return false;
		}
		return FFluidStatic.canReach(pos1, pos2, state1, state2, fluid, w);
	}

	public static enum Mode {
		DEFAULT, EQUALIZER;
	}

}