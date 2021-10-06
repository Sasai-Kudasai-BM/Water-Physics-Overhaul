package net.skds.wpo.mixins.fluids;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.skds.wpo.client.render.EFluidBlockRenderer;

@Mixin(value = { FluidBlockRenderer.class })
public class FluidBlockRendererMixin {

	private static EFluidBlockRenderer renderer = new EFluidBlockRenderer();

	@Inject(method = "render", at = @At(value = "INVOKE", ordinal = 5), cancellable = true)
	void gc(IBlockDisplayReader lightReaderIn, BlockPos posIn, IVertexBuilder vertexBuilderIn, FluidState fluidStateIn, CallbackInfoReturnable<Boolean> ci) {	
		boolean bl = renderer.render(lightReaderIn, posIn, vertexBuilderIn, fluidStateIn);
		ci.setReturnValue(bl);
	}

}