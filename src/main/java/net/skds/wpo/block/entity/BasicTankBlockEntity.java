package net.skds.wpo.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.skds.wpo.fluidphysics.FFluidStatic;
import net.skds.wpo.util.api.IPressuredTank;

public abstract class BasicTankBlockEntity extends BlockEntity implements IPressuredTank, IFluidHandler {

	public int capacity = 500;
	protected static final float ATM_PRESSURE = 1f;

	protected FluidTank tank;
	
	public BasicTankBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		tank = new FluidTank(capacity);
	}

	public BasicTankBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int capacity) {
		super(tileEntityTypeIn, pos, state);
		this.capacity = capacity;
		tank = new FluidTank(capacity);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (tank.isEmpty()) {
			return;
		}
		Fluid tf = tank.getFluid().getFluid();
		BlockState state0 = level.getBlockState(worldPosition);
		int lvl = tank.getFluid().getAmount() / 125;
		FluidState fs = state0.getFluidState();
		if (!fs.isEmpty() && fs.getType().isSame(tf)) {
			lvl += fs.getAmount();
			if (lvl > 8) {
				lvl = 8;
			}
		} else {
			state0 = Blocks.AIR.defaultBlockState();
		}
		//System.out.println(state0);
		BlockState ns = FFluidStatic.getUpdatedState(state0, lvl, tf);
		level.setBlockAndUpdate(worldPosition, ns);
	}


}