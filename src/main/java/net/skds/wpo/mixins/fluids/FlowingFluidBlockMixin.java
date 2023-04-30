package net.skds.wpo.mixins.fluids;

import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.skds.wpo.fluidphysics.FFluidStatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = { LiquidBlock.class })
public class FlowingFluidBlockMixin extends Block {

	//private static final IntegerProperty F_LEVEL = BlockStateProps.FFLUID_LEVEL;


    public FlowingFluidBlockMixin(Properties properties) {
        super(properties);
    }

    @Redirect(method = { "onPlace", "updateShape", "neighborChanged" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;getTickDelay(Lnet/minecraft/world/level/LevelReader;)I", ordinal = 0))
    public int a(FlowingFluid fluid, LevelReader w) {
        return FFluidStatic.getTickRate(fluid, w);
    }


    //public PushReaction getPushReaction(BlockState state) {
    //    if (PhysEXConfig.COMMON.finiteFluids.get()) {
    //        return FFluidStatic.getPushReaction(state);
    //    }
    //    return this.material.getPushReaction();
    //}
}