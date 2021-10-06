package net.skds.wpo.mixins.fluids;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import net.skds.wpo.client.render.EFluidBlockRenderer;

@Mixin(BlockRendererDispatcher.class)
public class BlockRendererDispatcherMixin {

	@Shadow
	private FluidBlockRenderer fluidRenderer;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void init(BlockModelShapes shapes, BlockColors colors, CallbackInfo ci) {
		this.fluidRenderer = new EFluidBlockRenderer();
	}
}
