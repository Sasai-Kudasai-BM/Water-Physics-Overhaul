package net.skds.wpo.block.entity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
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

import javax.annotation.Nullable;

public class FluidGateBlockEntity extends BasicTankBlockEntity
		implements IConnectionSides {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	//private FluidTank tank = new FluidTank(500);
	public float pressure = ATM_PRESSURE;
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

	private boolean atm = true;
	private int timer = 0;

	public FluidGateBlockEntity(BlockPos pos, BlockState state) {
		super(Entities.GATE.get(), pos, state);
	}

	private Direction[] getDirections() {

		Direction[] dirs = new Direction[6];

		dirs = new Direction[6];
		dirs[0] = Direction.DOWN;
		dirs[5] = Direction.UP;

		for (int index = 0; index < 4; ++index) {
			Direction dir = Direction.from2DDataValue((index));
			dirs[index + 1] = dir;
		}

		return dirs;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, FluidGateBlockEntity be) {
		be.timer++;
		if (level.isClientSide) {
			return;
		}

		if (be.pressure < 0) {
			be.pressure = 0;
		}

		if (be.atm) {
			be.pressure = ATM_PRESSURE;
		}

//		be.clearCache();  // TODO what is this for? where should it call to?
		BlockState bs = be.getBlockState();
		if (bs.getBlock() == FBlocks.GATE.get() && !bs.getValue(POWERED)) {

			if (be.timer % 4 == 0) {
				be.tickGate(bs);
			}
		}
	}

	private void tickGate(BlockState state) {
		if (pressure < ATM_PRESSURE) {
			return;
		}

		FluidStack fst = tank.getFluid();
		int am = fst.getAmount();
		if (am < 125) {
			return;
		}


		for (Direction dir : getDirections()) {
			if (dir.getOpposite() == state.getValue(BlockStateProperties.FACING)) {
				continue;
			}

			BlockPos flowPos = worldPosition.relative(dir);
			FluidState flowFs = level.getFluidState(flowPos);
			BlockState flowState = level.getBlockState(flowPos);

			Fluid flowF = flowFs.getType();
			Fluid tF = fst.getFluid();
			if ((!flowFs.isEmpty() && !tF.isSame(flowF)) || FFluidStatic.canOnlyFullCube(flowState)) {
				return;
			}

			if (FFluidStatic.canReach(worldPosition, flowPos, Blocks.AIR.defaultBlockState(), flowState, tF, level)) {
				int dl = am / 125;
				int lvl = flowFs.getAmount();
				dl = 8 - lvl >= dl ? dl : 8 - lvl;

				if (dl > 0) {
					lvl += dl;
					BlockState bs2 = FFluidStatic.getUpdatedState(flowState, lvl, tF);
					FluidStack nfst = new FluidStack(tF, am - (dl * 125));
					tank.setFluid(nfst);

					level.setBlockAndUpdate(flowPos, bs2);
					atm = true;
					break;
				}
			}
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
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return holder.cast();
		return super.getCapability(capability, facing);
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