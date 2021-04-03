package net.skds.wpo.fluidphysics;

import static net.skds.wpo.WPOConfig.COMMON;
import static net.skds.wpo.WPOConfig.MAX_FLUID_LEVEL;

import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.skds.wpo.WPOConfig;

public class FFluidEQ extends FFluidBasic {

	FFluidEQ(ServerWorld w, BlockPos pos, WorldWorkSet owner, FFluidBasic.Mode mode, int worker) {
		super(w, pos, mode, owner, worker);
	}

	@Override
	public void execute() {
		if (getBlockState(pos.up()).getFluidState().isEmpty() && !FFluidStatic.canOnlyFullCube(state)
				&& !canFlow(pos, pos.down(), state, getBlockState(pos.down()), true, false)) {
			equalize();
		}
	}
	
	public void equalize() {
		boolean slide = WPOConfig.COMMON.maxSlideDist.get() > 0;
		// boolean slide = false;
		// setState(pos.add(0, 16, 0), Blocks.STONE.getDefaultState());
		boolean slided = false;
		int i0 = w.getRandom().nextInt(4);
		if (slide && !canReach(pos, pos.down(), state, getBlockState(pos.down())) && level == 1) {
			slided = slide();
		}
		int dist = COMMON.maxEqDist.get();
		if (!slided && dist > 0) {
			// if (isPassedEq(pos)) {
			// return;
			// }
			for (int index = 0; index < 4; ++index) {
				if (level <= 0) {
					break;
				}
				if (cancel) {
					return;
				}
				Direction dir = Direction.byHorizontalIndex((index + i0) % 4);
				equalizeLine(dir, false, dist);
			}
		}
	}

	public boolean slide() {
		// setState(pos.add(0, 16, 0), Blocks.STONE.getDefaultState());
		// System.out.println("x");
		int slideDist = WPOConfig.COMMON.maxSlideDist.get();
		int lenmin = slideDist;

		boolean selPosb = false;
		BlockPos selPos = pos;
		BlockState selState = state;

		boolean[] diag2 = { false, true };

		/// System.out.println("len");
		for (Direction dir : FFluidStatic.getRandomizedDirections(w.getRandom(), false)) {
			for (boolean diag : diag2) {

				boolean selPosb2 = false;
				BlockPos selPos2 = pos;
				int dist = 0;
				int len = lenmin;
				BlockPos pos2 = pos;
				BlockPos pos1 = pos;
				boolean cont = true;
				boolean side = false;
				BlockState state1 = state;
				BlockState state2 = state;
				boolean bl = false;

				// System.out.println(len);
				wh: while (cont && len > 0) {
					pos1 = pos2;
					state1 = state2;
					if (diag) {
						if (side) {
							dir = dir.rotateY();
							side = !side;
						} else {
							dir = dir.rotateYCCW();
							side = !side;
						}
					}
					pos2 = pos1.offset(dir);
					state2 = getBlockState(pos2);
					FluidState fs2 = state2.getFluidState();
					if (canReach(pos1, pos2, state1, state2)
							&& (fs2.isEmpty() || (fs2.getLevel() < 2 && fs2.getFluid().isEquivalentTo(fluid)))) {
						if ((state1.getBlock() instanceof IWaterLoggable || state2.getBlock() instanceof IWaterLoggable)
								&& !(fluid instanceof WaterFluid)) {
							break wh;
						}
						if (dist > 0 && !selPosb2 && fs2.isEmpty()) {
							selPosb2 = true;
							selPos2 = pos1;
						}
						bl = (canFlow(pos1, pos1.down(), state1, getBlockState(pos1.down()), true, false))
								&& !FFluidStatic.canOnlyFullCube(state2);
					} else {
						break wh;
					}
					--len;
					if (bl && !cancel && selPosb2) {
						lenmin = Math.min(dist, lenmin);
						selPos = selPos2;
						selState = state1;
						selPosb = true;
					}
					++dist;
				}
			}
		}
		if (selPosb && validate(selPos)) {
			//System.out.println("bl");
			selState = getBlockState(selPos);
			selState = flowToPosEq(pos, selPos, selState, -1);
			setState(selPos, selState);
			setState(pos, state);
			return true;
		}
		return false;
	}

	public void equalizeLine(Direction dir, boolean diag, int len) {
		// len = (int) ((float) len * fluidWorker.eqSpeed);
		// System.out.println(fluidWorker.eqSpeed);
		// len=8;
		BlockPos pos2 = pos;
		BlockPos pos1 = pos;
		int len2 = len;
		boolean cont = true;
		boolean side = false;
		BlockState state1 = state;
		BlockState state2 = state;
		int hmod = 0;
		boolean bl = false;

		boolean blocked = false;

		while (cont && len > 0) {

			// if (diag) setState(pos1.down(), Blocks.BIRCH_LOG.getDefaultState());
			// setState(pos1.add(0, 16, 0), Blocks.STONE.getDefaultState());

			if (!diag && len2 - len == 1) {
				equalizeLine(dir, true, len);
			}

			if (diag) {
				if (side) {
					dir = dir.rotateY();
					side = !side;
				} else {
					dir = dir.rotateYCCW();
					side = !side;
				}
			}
			pos1 = pos2;
			state1 = state2;

			BlockPos pos1u = pos1.up();
			BlockState state1u = getBlockState(pos1u);
			FluidState fs1u = state1u.getFluidState();

			if (!blocked && canReach(pos1u, pos1, state1u, state1)
					&& (!fs1u.isEmpty() && isThisFluid(fs1u.getFluid()))) {
				// state1 = state1u;
				// System.out.println("x");
				pos2 = pos1u;
				state2 = state1u;
				++hmod;
				bl = true;
			} else {
				pos2 = pos1.offset(dir);
				state2 = getBlockState(pos2);
			}

			FluidState fs2 = state2.getFluidState();

			if (isPassedEq(pos2)) {
				// fluidWorker.addNTTask(pos2.toLong(), FFluidStatic.getTickRate((FlowingFluid)
				// fluid, w));
				// fluidWorker.addNTTask(pos.toLong(), FFluidStatic.getTickRate((FlowingFluid)
				// fluid, w));
				// FluidTasksManager.addNTTask(w, pos1, FFluidStatic.getTickRate((FlowingFluid)
				// fluid, w));
				// System.out.println(pos2);
				break;
			}

			if (canReach(pos1, pos2, state1, state2)
					&& (isThisFluid(fs2.getFluid()) || (fs2.isEmpty() && level > 1))) {
				if ((state1.getBlock() instanceof IWaterLoggable || state2.getBlock() instanceof IWaterLoggable)
						&& !(fluid instanceof WaterFluid)) {
					// System.out.println("dd");
					break;
				}
				bl = true;
				blocked = false;

			} else {
				// pos1 = pos2;
				pos2 = pos1.down();
				state1 = state2;
				state2 = getBlockState(pos2);
				fs2 = state2.getFluidState();
				if (canReach(pos1, pos2, state1, state2)
						&& (!fs2.isEmpty() && isThisFluid(fs2.getFluid()) || fs2.isEmpty())) {
					--hmod;
					bl = true;
					blocked = true;

				} else {
					break;
				}
			}

			if (bl && !cancel && validate(pos2)) {
				int level2 = fs2.getLevel();
					//boolean b = level2 == 8 && level == 1;
					//if (b) {
					//	System.out.println(hmod);
					//}
				//int hmod2 = hmod >= 1 ? 1 : hmod <= -1 ? -1 : 0;
				int l1 = getAbsoluteLevel(pos.getY(), level);
				int l2 = getAbsoluteLevel(pos2.getY(), level2);
				if (MathHelper.abs(l1 - l2) > 1
						&& !FFluidStatic.canOnlyFullCube(state2)) {
					state2 = flowToPosEq(pos, pos2, state2, hmod);
					setState(pos2, state2);
					setState(pos, state);
					// System.out.println(level + " ss: " + level2 + state2);
					addPassedEq(pos2);
					return;
				}
			}
			--len;
		}
	}

	private BlockState flowToPosEq(BlockPos pos1, BlockPos pos2, BlockState state2, int l) {

		BlockState state2n = state2;

		FluidState fs2 = state2.getFluidState();
		int level2 = fs2.getLevel();
		int delta = (level - level2) / 2;
		// l = 0;
		if (l != 0) {
			if (l == -1) {
				level2 += level;
				if (level2 > MAX_FLUID_LEVEL) {
					level = level2 - MAX_FLUID_LEVEL;
					level2 = MAX_FLUID_LEVEL;
				} else {
					level = 0;
				}
			} else {
				// System.out.println(l);
				level += level2;
				if (level > MAX_FLUID_LEVEL) {
					level2 = level - MAX_FLUID_LEVEL;
					level = MAX_FLUID_LEVEL;
				} else {
					level2 = 0;
				}
			}
			state = getUpdatedState(state, level);
			state2n = getUpdatedState(state2, level2);

		} else if (MathHelper.abs(delta) >= 1) {

			level -= delta;
			level2 += delta;
			// System.out.println("Delta " + level + " ss: " + level2);
			state = getUpdatedState(state, level);
			state2n = getUpdatedState(state2, level2);

		} else if (level2 == 0) {
			level2 = level;
			level = 0;
			state = getUpdatedState(state, level);
			state2n = getUpdatedState(state2, level2);
		}
		return state2n;

	}

	private boolean canFlow(BlockPos pos1, BlockPos pos2, BlockState state1, BlockState state2, boolean down,
			boolean ignoreLevels) {
		if (state2 == null) {
			cancel = true;
			return false;
		}
		if ((FFluidStatic.canOnlyFullCube(state2) || FFluidStatic.canOnlyFullCube(state)) && !down) {
			return false;
		}
		if (FFluidStatic.canOnlyFullCube(state2) && state1.getFluidState().getLevel() < WPOConfig.MAX_FLUID_LEVEL) {
			return false;
		}

		if ((state1.getBlock() instanceof IWaterLoggable || state2.getBlock() instanceof IWaterLoggable)
				&& !(fluid instanceof WaterFluid)) {
			return false;
		}

		if (!canReach(pos1, pos2, state1, state2)) {
			return false;
		}

		FluidState fs2 = state2.getFluidState();
		// if ((!fs2.isEmpty() && !isThisFluid(fs2.getFluid())) &&
		// !state1.getFluidState().canDisplace(w, pos2,
		// state2.getFluidState().getFluid(), FFluidStatic.dirFromVec(pos1, pos2)))
		// return false;

		int level2 = fs2.getLevel();
		if (level2 >= MAX_FLUID_LEVEL && !ignoreLevels) {
			return false;
		}

		if (level == 1 && !down && !ignoreLevels) {
			if (fs2.isEmpty()) {
				pos1 = pos2;
				pos2 = pos2.down();
				state1 = state2;
				state2 = getBlockState(pos2);
				if (isThisFluid(state2.getFluidState().getFluid()) || state2.getFluidState().isEmpty()) {
					return canFlow(pos1, pos2, state1, state2, true, false);
				} else {
					return false;
				}
			} else {
				return (level2 + 2 < level);
			}
		}

		return true;
	}
}