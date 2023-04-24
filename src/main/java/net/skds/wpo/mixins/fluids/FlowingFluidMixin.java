package net.skds.wpo.mixins.fluids;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.skds.wpo.fluidphysics.FFluidStatic;
import net.skds.wpo.fluidphysics.FluidTasksManager;
import net.skds.wpo.util.interfaces.IFlowingFluid;

@Mixin(value = { FlowingFluid.class })
public class FlowingFluidMixin implements IFlowingFluid {

    // private static final IntegerProperty F_LEVEL = BlockStateProps.FFLUID_LEVEL;

    // public int getFLevel() {

    // return 0;
    // }

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void tick(World worldIn, BlockPos pos, FluidState fs, CallbackInfo ci) {
        if (!worldIn.isClientSide) {
            FluidTasksManager.addFluidTask((ServerWorld) worldIn, pos, fs.createLegacyBlock());
        }
        ci.cancel();

    }

    @Inject(method = "getFlow", at = @At(value = "HEAD"), cancellable = true)
    public void getFlow(IBlockReader w, BlockPos pos, FluidState fs, CallbackInfoReturnable<Vector3d> ci) {
        // .setReturnValue cancels the method injected into and overwrites the return value. No explicit return needed
        ci.setReturnValue(FFluidStatic.getVel(w, pos, fs));
    }

    @Inject(method = "getOwnHeight", at = @At(value = "HEAD"), cancellable = true)
    public void getOwnHeight(FluidState fs, CallbackInfoReturnable<Float> ci) {
        // .setReturnValue cancels the method injected into and overwrites the return value. No explicit return needed
        ci.setReturnValue(FFluidStatic.getHeight(fs.getAmount()));
    }

    public void beforeReplacingBlockCustom(IWorld worldIn, BlockPos pos, BlockState state) {
        beforeDestroyingBlock(worldIn, pos, state);

    }

    // ================= SHADOW ================ //

    @Shadow
    protected void beforeDestroyingBlock(IWorld worldIn, BlockPos pos, BlockState state) {
    }
}