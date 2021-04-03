package net.skds.wpo.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.skds.wpo.fluidphysics.FFluidStatic;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.registry.FBlocks;
import net.skds.wpo.util.api.IConnectionSides;

public class FluidGateTileEntity extends BasicTankEntity
		implements IConnectionSides {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	//private FluidTank tank = new FluidTank(500);
	public float pressure = ATM_PRESSURE;
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

	private boolean atm = true;
	private int timer = 0;

	public FluidGateTileEntity() {
		super(Entities.GATE.get());
	}

	private Direction[] getDirections() {

		Direction[] dirs = new Direction[6];

		dirs = new Direction[6];
		dirs[0] = Direction.DOWN;
		dirs[5] = Direction.UP;

		for (int index = 0; index < 4; ++index) {
			Direction dir = Direction.byHorizontalIndex((index));
			dirs[index + 1] = dir;
		}

		return dirs;
	}

	@Override
	public void tick() {
		timer++;
		if (world.isRemote) {
			return;
		}

		if (pressure < 0) {
			pressure = 0;
		}

		if (atm) {
			pressure = ATM_PRESSURE;
		}

		updateContainingBlockInfo();
		BlockState bs = getBlockState();
		if (bs.getBlock() == FBlocks.GATE.get() && !bs.get(POWERED)) {

			if (timer % 4 == 0) {
				tickGate(bs);
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
			if (dir.getOpposite() == state.get(BlockStateProperties.FACING)) {
				continue;
			}

			BlockPos flowPos = pos.offset(dir);
			FluidState flowFs = world.getFluidState(flowPos);
			BlockState flowState = world.getBlockState(flowPos);

			Fluid flowF = flowFs.getFluid();
			Fluid tF = fst.getFluid();
			if ((!flowFs.isEmpty() && !tF.isEquivalentTo(flowF)) || FFluidStatic.canOnlyFullCube(flowState)) {
				return;
			}

			if (FFluidStatic.canReach(pos, flowPos, Blocks.AIR.getDefaultState(), flowState, tF, world)) {
				int dl = am / 125;
				int lvl = flowFs.getLevel();
				dl = 8 - lvl >= dl ? dl : 8 - lvl;

				if (dl > 0) {
					lvl += dl;
					BlockState bs2 = FFluidStatic.getUpdatedState(flowState, lvl, tF);
					FluidStack nfst = new FluidStack(tF, am - (dl * 125));
					tank.setFluid(nfst);

					world.setBlockState(flowPos, bs2);
					atm = true;
					break;
				}
			}
		}
	}

	@Override
	public boolean canBeConnected(Direction dir) {
		return dir == getBlockState().get(BlockStateProperties.FACING);
	}

	@Override
	public boolean canBeConnected(int dir) {
		Direction d = Direction.byIndex(dir);
		return d == getBlockState().get(BlockStateProperties.FACING);
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
		return pressure + (PipeTileEntity.getPressurePerStack(tank.getFluid()) * 2);
	}

	@Override
	public void setPressure(float pressure, Direction side) {
		this.pressure = pressure;
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		super.read(state, tag);
		tank.readFromNBT(tag);
		if (tag.contains("Pressure")) {
			pressure = tag.getFloat("Pressure");
		} else {
			pressure = ATM_PRESSURE;
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag = super.write(tag);
		tank.writeToNBT(tag);
		tag.putFloat("Pressure", pressure);
		return tag;
	}

}