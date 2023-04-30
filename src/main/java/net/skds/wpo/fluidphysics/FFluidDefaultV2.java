package net.skds.wpo.fluidphysics;

import static net.skds.wpo.WPOConfig.MAX_FLUID_LEVEL;

import java.util.Random;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.skds.wpo.WPOConfig;

public class FFluidDefaultV2 extends FFluidBasic {

	boolean dcFlag = false;
	boolean dc = false;
	boolean sc = false;

	FFluidDefaultV2(ServerLevel w, BlockPos pos, WorldWorkSet owner, FFluidBasic.Mode mode, int worker) {
		super(w, pos, mode, owner, worker);
	}

	@Override
	protected void execute() {
		Random r = new Random();

		BlockPos posD = pos.below();
		if (posD.getY() < 0) {
			if (validate(pos)) {
				state = getUpdatedState(state, 0);
				sc = true;
				setState(pos, state);
				unban(pos);
			}
			return;
		}
		BlockState downstate = getBlockState(posD);
		if (downstate != null) {
			if (canFlow(pos, posD, state, downstate, true, false)) {
				if (FFluidStatic.canOnlyFullCube(state) || FFluidStatic.canOnlyFullCube(downstate)) {
					int l = state.getFluidState().getAmount();
					int ld = downstate.getFluidState().getAmount();
					if (ld == 0 && l == MAX_FLUID_LEVEL && flowFullCubeV2(pos, posD)) {
						dc = true;
						return;
					}
				} else {
					if (flow(pos, posD, -1)) {
						addPassedEq(posD);
						dc = true;
						sc = true;
					}
				}
			}
		}

		if (!dc && FFluidStatic.canOnlyFullCube(state)) {
			for (Direction dir : FFluidStatic.getRandomizedDirections(r, false)) {
				BlockPos pos2 = pos.relative(dir);
				BlockState state2 = getBlockState(pos2);
				if (state2.getFluidState().isEmpty() && !FFluidStatic.canOnlyFullCube(state2)
						&& canReach(pos, pos2, state, state2)) {
					if (flowFullCubeV2(pos, pos2)) {
						sc = true;
						return;
					}
				}
			}
		}

		for (Direction dir : FFluidStatic.getRandomizedDirections(r, false)) {
			BlockPos pos2 = pos.relative(dir);
			BlockState state2 = getBlockState(pos2);
			if (FFluidStatic.canOnlyFullCube(state2) && canFlow(pos, pos2, state, state2, true, false) && !dc) {
				BlockPos posu = pos.above();
				BlockState stateu = getBlockState(posu);
				if (stateu.getFluidState().getAmount() > 0 && canFlow(posu, pos, stateu, state, true, true)) {
					if (flowFullCubeV2(pos, pos2)) {
						sc = true;
						return;
					}
				}
			} else {
				sc = !dc && canFlow(pos, pos2, state, state2, false, false) && flow(pos, pos2, 0);
			}
		}

		if (getBlockState(pos.above()).getFluidState().isEmpty() && !FFluidStatic.canOnlyFullCube(state) && !dc && !sc
				&& !cancel) {
			//castOwner.addEQTask(longpos, (FlowingFluid) fluid);
			if (!isPassedEq(pos)) {
				new FluidTask.EQTask(castOwner, longpos).run();
			}
		}
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
		if (FFluidStatic.canOnlyFullCube(state2) && state1.getFluidState().getAmount() < WPOConfig.MAX_FLUID_LEVEL) {
			return false;
		}

		if (!canReach(pos1, pos2, state1, state2)) {
			return false;
		}

		FluidState fs2 = state2.getFluidState();
		FluidState fs1 = state1.getFluidState();

		int level2 = fs2.getAmount();
		int level1 = fs1.getAmount();
		if (level2 >= MAX_FLUID_LEVEL && !ignoreLevels && fluid.isSame(fs2.getType())) {
			return false;
		}

		if (level1 == 1 && !down && !ignoreLevels) {
			if (fs2.isEmpty()) {
				pos1 = pos2;
				pos2 = pos2.below();
				state1 = state2;
				state2 = getBlockState(pos2);
				if (isThisFluid(state2.getFluidState().getType()) || state2.getFluidState().isEmpty()) {
					return canFlow(pos1, pos2, state1, state2, true, false);
				} else {
					return false;
				}
			} else {
				return (level2 + 2 < level1);
			}
		} else if (!down && level2 + 1 >= level1) {
			return false;
		}

		return true;
	}
}