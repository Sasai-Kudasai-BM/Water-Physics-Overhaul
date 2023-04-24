package net.skds.wpo.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.registry.FBlocks;
import net.skds.wpo.util.api.IConnectionSides;

public class PipePumpTileEntity extends BasicTankEntity implements IConnectionSides {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public Direction facing = Direction.UP;
	public boolean powered = false;
	public int anim = 0;
	public int animSpeed = 10;

	private static final float WJUH = 0.1f;
	private static final float MAX_PRESSURE = 3f;
	// private FluidTank tank = new FluidTank(500);
	public float pressureIn = ATM_PRESSURE;
	public float pressureOut = ATM_PRESSURE;
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

	//private int timer = 0;

	public PipePumpTileEntity() {
		super(Entities.PIPE_PUMP.get());
	}

	public PipePumpTileEntity(BlockState state) {
		super(Entities.PIPE_PUMP.get());
		facing = state.getValue(BlockStateProperties.FACING);
	}

	@Override
	public void tick() {
		//timer++;

		if (pressureIn < 0) {
			pressureIn = 0;
		}
		if (pressureOut < 0) {
			pressureOut = 0;
		}

		if (pressureIn > pressureOut) {
			pressureOut = pressureIn;
		}

		clearCache();
		BlockState bs = getBlockState();
		// facing = bs.get(BlockStateProperties.FACING);
		if (bs.getBlock() == FBlocks.PIPE_PUMP.get()) {
			powered = bs.getValue(POWERED);
			if (powered) {
				if (pressureOut < MAX_PRESSURE && pressureIn > 0) {
					pressureOut += (MAX_PRESSURE - pressureOut) * WJUH;
					pressureIn -= pressureIn * WJUH;
				}

				if (level.isClientSide) {
					if (anim < 0) {
						anim = 0;
					}
					anim++;
					if (anim > animSpeed) {
						anim = 0;
					}
				}

			} else {

				if (level.isClientSide) {
					if (anim > animSpeed / 2) {
						anim = animSpeed - anim;
					}
					if (anim > -1) {
						anim--;
					}
				}

				float dp = (pressureIn - pressureOut) / 2;
				pressureOut += dp;
				pressureIn -= dp;
			}
		}
	}

	@Override
	public boolean canBeConnected(Direction dir) {
		return dir == facing || dir == facing.getOpposite();
	}

	@Override
	public boolean canBeConnected(int dir) {
		Direction d = Direction.from3DDataValue(dir);
		return canBeConnected(d);
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
		if (side == facing) {
			return pressureOut;
		}
		return pressureIn;
	}

	@Override
	public float getPressure(Direction side) {
		return side == facing ? (pressureOut + (PipeTileEntity.getPressurePerStack(tank.getFluid()) * 2)) : pressureIn;
	}

	@Override
	public void setPressure(float pressure, Direction side) {
		if (side == facing) {
			this.pressureOut = pressure;
		} else {
			this.pressureIn = pressure;
		}
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);
		tank.readFromNBT(tag);
		if (tag.contains("PressureIn")) {
			pressureIn = tag.getFloat("PressureIn");
		} else {
			pressureIn = ATM_PRESSURE;
		}
		if (tag.contains("PressureOut")) {
			pressureOut = tag.getFloat("PressureOut");
		} else {
			pressureOut = ATM_PRESSURE;
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag = super.save(tag);
		tank.writeToNBT(tag);
		tag.putFloat("PressureIn", pressureIn);
		tag.putFloat("PressureOut", pressureOut);
		return tag;
	}

}