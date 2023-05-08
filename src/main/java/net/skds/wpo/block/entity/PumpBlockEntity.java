package net.skds.wpo.block.entity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.skds.wpo.fluidphysics.FFluidStatic;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.registry.FBlocks;
import net.skds.wpo.util.api.IConnectionSides;

public class PumpBlockEntity extends BasicTankBlockEntity implements IConnectionSides {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public Direction facing = Direction.UP;
	public boolean powered = false;
	public int anim = 0;
	public int animSpeed = 10;

	private static final float WJUH = 0.1f;
	private static final float MAX_PRESSURE = 3f;
	private static final float ATM_PRESSURE = 1f;
	// private FluidTank tank = new FluidTank(500);
	public float pressure = ATM_PRESSURE;

	public int timer = 0;

	public PumpBlockEntity(BlockPos pos, BlockState state) {
		super(Entities.PUMP.get(), pos, state);
		facing = state.getValue(BlockStateProperties.FACING);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, PumpBlockEntity be) {
		be.timer++;

		if (be.pressure < 0) {
			be.pressure = 0;
		}

//		be.clearCache();  // TODO check if needed
		BlockState bs = be.getBlockState();
		be.powered = bs.getBlock() == FBlocks.PUMP.get() && bs.getValue(POWERED);
		if (be.powered) {
			if (be.pressure < MAX_PRESSURE) {
				be.pressure += (MAX_PRESSURE - be.pressure) * WJUH;
			}

			if (level.isClientSide) {
				if (be.anim < 0) {
					be.anim = 0;
				}
				be.anim++;
				if (be.anim > be.animSpeed) {
					be.anim = 0;
				}
				return;
			}

			if (be.timer % 4 == 0) {
				be.tickPump(bs);
			}
		} else {

			if (level.isClientSide) {
				if (be.anim > be.animSpeed / 2) {
					be.anim = be.animSpeed - be.anim;
				}
				if (be.anim > -1) {
					be.anim--;
				}
			}
		}
	}

	private void tickPump(BlockState state) {
		if (pressure > MAX_PRESSURE) {
			return;
		}

		FluidStack fst = tank.getFluid();
		int am = fst.getAmount();
		if (am > 500 - 125) {
			return;
		}

		Direction dir = state.getValue(BlockStateProperties.FACING);
		BlockPos suckPos = worldPosition.relative(dir);
		FluidState suckFs = level.getFluidState(suckPos);
		BlockState suckState = level.getBlockState(suckPos);

		Fluid sucF = suckFs.getType();
		if (sucF instanceof FlowingFluid) {
			sucF = ((FlowingFluid) sucF).getSource();
		}
		Fluid tF = fst.getFluid();
		if (suckFs.isEmpty() || (!tank.isEmpty() && !tF.isSame(sucF))
				|| FFluidStatic.canOnlyFullCube(suckState)) {
			return;
		}
		// System.out.println(timer);

		if (FFluidStatic.canReach(suckPos, worldPosition, suckState, Blocks.AIR.defaultBlockState(), sucF, level)) {
			int dl = (500 - am) / 125;
			int lvl = suckFs.getAmount();
			dl = lvl >= dl ? dl : lvl;

			lvl -= dl;
			BlockState bs2 = FFluidStatic.getUpdatedState(suckState, lvl, sucF);
			FluidStack nfst = new FluidStack(sucF, am + (dl * 125));
			tank.setFluid(nfst);

			level.setBlockAndUpdate(suckPos, bs2);
		}
	}

	@Override
	public boolean canBeConnected(Direction dir) {
		return dir == getBlockState().getValue(BlockStateProperties.FACING);
	}

	@Override
	public boolean canBeConnected(int dir) {
		Direction d = Direction.from3DDataValue(dir);
		return d == getBlockState().getValue(BlockStateProperties.FACING);
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return this.tank.getFluid();
	}

	@Override
	public int getTankCapacity(int tank) {
		return this.tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return this.tank.isFluidValid(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return this.tank.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return this.tank.drain(resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return this.tank.drain(maxDrain, action);
	}

	@Override
	public float getZeroPressure(Direction side) {
		return pressure;
	}

	@Override
	public float getPressure(Direction side) {
		return pressure + (PipeBlockEntity.getPressurePerStack(tank.getFluid()) * 2);
	}

	@Override
	public void setPressure(float pressure, Direction side) {
		this.pressure = pressure;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		tank.readFromNBT(tag);
		if (tag.contains("Pressure")) {
			pressure = tag.getFloat("Pressure");
		} else {
			pressure = ATM_PRESSURE;
		}
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag = super.save(tag);
		tank.writeToNBT(tag);
		tag.putFloat("Pressure", pressure);
		return tag;
	}

}