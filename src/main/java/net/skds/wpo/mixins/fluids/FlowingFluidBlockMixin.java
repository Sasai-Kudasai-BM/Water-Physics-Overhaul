package net.skds.wpo.mixins.fluids;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.world.IWorldReader;
import net.skds.wpo.fluidphysics.FFluidStatic;

@Mixin(value = { FlowingFluidBlock.class })
public class FlowingFluidBlockMixin extends Block {

	//private static final IntegerProperty F_LEVEL = BlockStateProps.FFLUID_LEVEL;


    public FlowingFluidBlockMixin(Properties properties) {
        super(properties);
    }

    @Redirect(method = { "Lnet/minecraft/block/FlowingFluidBlock;onBlockAdded",
            "Lnet/minecraft/block/FlowingFluidBlock;updatePostPlacement",
            "Lnet/minecraft/block/FlowingFluidBlock;neighborChanged" }, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/fluid/FlowingFluid;getTickRate(Lnet/minecraft/world/IWorldReader;)I"))
    public int a(FlowingFluid fluid, IWorldReader w) {
        return FFluidStatic.getTickRate(fluid, w);
    }

    //public PushReaction getPushReaction(BlockState state) {
    //    if (PhysEXConfig.COMMON.finiteFluids.get()) {
    //        return FFluidStatic.getPushReaction(state);
    //    }
    //    return this.material.getPushReaction();
    //}
}