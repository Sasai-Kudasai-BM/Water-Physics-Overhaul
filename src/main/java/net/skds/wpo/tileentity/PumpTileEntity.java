package net.skds.wpo.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowingFluid;
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

public class PumpTileEntity extends BasicTankEntity implements IConnectionSides {

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
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

	public int timer = 0;

	public PumpTileEntity() {
		super(Entities.PUMP.get());
	}

	public PumpTileEntity(BlockState state) {
		super(Entities.PUMP.get());
		facing = state.get(BlockStateProperties.FACING);
	}

	@Override
	public void tick() {
		timer++;

		if (pressure < 0) {
			pressure = 0;
		}

		updateContainingBlockInfo();
		BlockState bs = getBlockState();
		powered = bs.getBlock() == FBlocks.PUMP.get() && bs.get(POWERED);
		if (powered) {
			if (pressure < MAX_PRESSURE) {
				pressure += (MAX_PRESSURE - pressure) * WJUH;
			}

			if (world.isRemote) {
				if (anim < 0) {
					anim = 0;
				}
				anim++;
				if (anim > animSpeed) {
					anim = 0;
				}
				return;
			}

			if (timer % 4 == 0) {
				tickPump(bs);
			}
		} else {

			if (world.isRemote) {
				if (anim > animSpeed / 2) {
					anim = animSpeed - anim;
				}
				if (anim > -1) {
					anim--;
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

		Direction dir = state.get(BlockStateProperties.FACING);
		BlockPos suckPos = pos.offset(dir);
		FluidState suckFs = world.getFluidState(suckPos);
		BlockState suckState = world.getBlockState(suckPos);

		Fluid sucF = suckFs.getFluid();
		if (sucF instanceof FlowingFluid) {
			sucF = ((FlowingFluid) sucF).getStillFluid();
		}
		Fluid tF = fst.getFluid();
		if (suckFs.isEmpty() || (!tank.isEmpty() && !tF.isEquivalentTo(sucF))
				|| FFluidStatic.canOnlyFullCube(suckState)) {
			return;
		}
		// System.out.println(timer);

		if (FFluidStatic.canReach(suckPos, pos, suckState, Blocks.AIR.getDefaultState(), sucF, world)) {
			int dl = (500 - am) / 125;
			int lvl = suckFs.getLevel();
			dl = lvl >= dl ? dl : lvl;

			lvl -= dl;
			BlockState bs2 = FFluidStatic.getUpdatedState(suckState, lvl, sucF);
			FluidStack nfst = new FluidStack(sucF, am + (dl * 125));
			tank.setFluid(nfst);

			world.setBlockState(suckPos, bs2);
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