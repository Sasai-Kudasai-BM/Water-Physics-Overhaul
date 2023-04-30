package net.skds.wpo.mixins.fluids;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
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
    public void tick(Level worldIn, BlockPos pos, FluidState fs, CallbackInfo ci) {
        if (!worldIn.isClientSide) {
            FluidTasksManager.addFluidTask((ServerLevel) worldIn, pos, fs.createLegacyBlock());
        }
        ci.cancel();

    }

    @Inject(method = "getFlow", at = @At(value = "HEAD"), cancellable = true)
    public void getFlow(BlockGetter w, BlockPos pos, FluidState fs, CallbackInfoReturnable<Vec3> ci) {
        // .setReturnValue cancels the method injected into and overwrites the return value. No explicit return needed
        ci.setReturnValue(FFluidStatic.getVel(w, pos, fs));
    }

    @Inject(method = "getOwnHeight", at = @At(value = "HEAD"), cancellable = true)
    public void getOwnHeight(FluidState fs, CallbackInfoReturnable<Float> ci) {
        // .setReturnValue cancels the method injected into and overwrites the return value. No explicit return needed
        ci.setReturnValue(FFluidStatic.getHeight(fs.getAmount()));
    }

    public void beforeReplacingBlockCustom(LevelAccessor worldIn, BlockPos pos, BlockState state) {
        beforeDestroyingBlock(worldIn, pos, state);

    }

    // ================= SHADOW ================ //

    @Shadow
    protected void beforeDestroyingBlock(LevelAccessor worldIn, BlockPos pos, BlockState state) {
    }
}