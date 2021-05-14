package net.skds.wpo.mixins;

import com.mojang.serialization.MapCodec;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.palette.PalettedContainer;
import net.minecraft.world.chunk.ChunkSection;

@Mixin(value = { ChunkSection.class })
public class AMixin {

	@Shadow
	@Final
	private PalettedContainer<BlockState> data;

	@Shadow
	public BlockState getBlockState(int x, int y, int z) {
		return null;
	}

	@Inject(method = "getFluidState", at = @At(value = "HEAD", ordinal = 0), cancellable = true)
	public void aaa(int x, int y, int z, CallbackInfoReturnable<FluidState> ci) {
		BlockState state = getBlockState(x, y, z);
		if (state.getBlock() instanceof SaplingBlock) {
			FluidState fs = Fluids.WATER.getStateContainer().getValidStates().get(0);
			ci.setReturnValue(fs);
		}
	}

}