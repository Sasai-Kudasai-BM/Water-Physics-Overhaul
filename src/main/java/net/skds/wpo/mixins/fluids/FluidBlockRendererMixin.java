package net.skds.wpo.mixins.fluids;

import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.skds.wpo.fluidphysics.FFluidStatic;

@Mixin(value = { FluidBlockRenderer.class })
public class FluidBlockRendererMixin {

	ConcurrentHashMap<Thread, float[]> customAH = new ConcurrentHashMap<>(4);
	float[] customAHSafe = new float[4];

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 0))
	public float gc(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] flex = FFluidStatic.getConH(w, p, f);
		customAH.put(Thread.currentThread(), flex);
		customAHSafe = flex;
		return flex[0];

	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 1))
	public float gc1(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] ffmas = customAH.get(Thread.currentThread());
		if (ffmas == null) {
			ffmas = customAHSafe;
		}
		float fll = ffmas[1];
		return fll;

	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 2))
	public float gc2(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] ffmas = customAH.get(Thread.currentThread());
		if (ffmas == null) {
			ffmas = customAHSafe;
		}
		float fll = ffmas[2];
		return fll;

	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 3))
	public float gc3(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] ffmas = customAH.remove(Thread.currentThread());
		if (ffmas == null) {
			ffmas = customAHSafe;
		}
		float fll = ffmas[3];
		return fll;

	}
}