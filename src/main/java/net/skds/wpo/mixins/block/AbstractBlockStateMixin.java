package net.skds.wpo.mixins.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.skds.wpo.WPOConfig;
import net.skds.wpo.fluidphysics.FFluidStatic;
import net.skds.wpo.registry.BlockStateProps;
import net.skds.wpo.util.interfaces.IBaseWL;

@Mixin(value = { AbstractBlockState.class })
public abstract class AbstractBlockStateMixin {

	@Inject(method = "getFluidState", at = @At(value = "HEAD"), cancellable = true)
	public void getFluidStateM(CallbackInfoReturnable<FluidState> ci) {
		BlockState bs = (BlockState) (Object) this;
		if (bs.getBlock() instanceof IBaseWL) {
			int level = bs.get(BlockStateProps.FFLUID_LEVEL);
			FluidState fs;
			if (bs.get(BlockStateProperties.WATERLOGGED)) {
				level = (level == 0) ? WPOConfig.MAX_FLUID_LEVEL : level;
				if (level >= WPOConfig.MAX_FLUID_LEVEL) {
					fs = ((FlowingFluid) Fluids.WATER).getStillFluidState(false);
				} else if (level <= 0) {
					fs = Fluids.EMPTY.getDefaultState();
				} else {
					fs = ((FlowingFluid) Fluids.WATER).getFlowingFluidState(level, false);
				}
			} else {
				fs = Fluids.EMPTY.getDefaultState();
			}
			ci.setReturnValue(fs);
		}

	}

	@Inject(method = "ticksRandomly", at = @At(value = "HEAD"), cancellable = true)
	public void ticksRandomlyM(CallbackInfoReturnable<Boolean> ci) {
	}

	@Inject(method = "neighborChanged", at = @At(value = "HEAD"), cancellable = false)
	public void neighborChangedM(World worldIn, BlockPos posIn, Block blockIn, BlockPos fromPosIn, boolean isMoving,
			CallbackInfo ci) {
		// super.neighborChanged(worldIn, posIn, blockIn, fromPosIn, isMoving);
		if (((BlockState) (Object) this).getBlock() instanceof IBaseWL) {
			BlockState s = (BlockState) (Object) this;
			fixFFLNoWL((World) worldIn, s, posIn);
			if (s.get(BlockStateProperties.WATERLOGGED))
				worldIn.getPendingFluidTicks().scheduleTick(posIn, s.getFluidState().getFluid(),
						FFluidStatic.getTickRate((FlowingFluid) s.getFluidState().getFluid(), worldIn));
		}
	}

	@Inject(method = "updatePostPlacement", at = @At(value = "HEAD"), cancellable = false)
	public void updatePostPlacementM(Direction face, BlockState queried, IWorld worldIn, BlockPos currentPos,
			BlockPos offsetPos, CallbackInfoReturnable<BlockState> ci) {
		if (((BlockState) (Object) this).getBlock() instanceof IBaseWL) {
			BlockState s = (BlockState) (Object) this;
			fixFFLNoWL(worldIn, s, currentPos);
			if (s.get(BlockStateProperties.WATERLOGGED))
				worldIn.getPendingFluidTicks().scheduleTick(currentPos, s.getFluidState().getFluid(),
						FFluidStatic.getTickRate((FlowingFluid) s.getFluidState().getFluid(), worldIn));
		}
	}

	private void fixFFLNoWL(IWorld w, BlockState s, BlockPos p) {
		if (!s.get(BlockStateProperties.WATERLOGGED) && s.get(BlockStateProps.FFLUID_LEVEL) > 0) {
			w.setBlockState(p, s.with(BlockStateProps.FFLUID_LEVEL, 0), 3);
		}
	}
}