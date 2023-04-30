package net.skds.wpo.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.registry.FBlocks;
import net.skds.wpo.util.api.IConnectionSides;

public class PipePumpBlockEntity extends BasicTankBlockEntity implements IConnectionSides {

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

	public PipePumpBlockEntity(BlockPos pos, BlockState state) {
		super(Entities.PIPE_PUMP.get(), pos, state);
		facing = state.getValue(BlockStateProperties.FACING);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, PipePumpBlockEntity be) {
		//timer++;

		if (be.pressureIn < 0) {
			be.pressureIn = 0;
		}
		if (be.pressureOut < 0) {
			be.pressureOut = 0;
		}

		if (be.pressureIn > be.pressureOut) {
			be.pressureOut = be.pressureIn;
		}

//		be.clearCache();  // TODO find out if needed
		BlockState bs = be.getBlockState();
		// facing = bs.get(BlockStateProperties.FACING);
		if (bs.getBlock() == FBlocks.PIPE_PUMP.get()) {
			be.powered = bs.getValue(POWERED);
			if (be.powered) {
				if (be.pressureOut < MAX_PRESSURE && be.pressureIn > 0) {
					be.pressureOut += (MAX_PRESSURE - be.pressureOut) * WJUH;
					be.pressureIn -= be.pressureIn * WJUH;
				}

				if (level.isClientSide) {
					if (be.anim < 0) {
						be.anim = 0;
					}
					be.anim++;
					if (be.anim > be.animSpeed) {
						be.anim = 0;
					}
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

				float dp = (be.pressureIn - be.pressureOut) / 2;
				be.pressureOut += dp;
				be.pressureIn -= dp;
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
		return side == facing ? (pressureOut + (PipeBlockEntity.getPressurePerStack(tank.getFluid()) * 2)) : pressureIn;
	}

	@Override
	public void setPressure(float pressure, Direction side) {
		if (side == facing) {
			this.pressureOut = pressure;
		} else {
			this.pressureIn = pressure;
		}
	}

	public void load(CompoundTag tag) {
		super.load(tag);
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
	public CompoundTag save(CompoundTag tag) {
		tag = super.save(tag);
		tank.writeToNBT(tag);
		tag.putFloat("PressureIn", pressureIn);
		tag.putFloat("PressureOut", pressureOut);
		return tag;
	}

}