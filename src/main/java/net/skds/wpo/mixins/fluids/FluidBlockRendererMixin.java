package net.skds.wpo.mixins.fluids;

import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.skds.wpo.fluidphysics.FFluidStatic;

@Mixin(value = { LiquidBlockRenderer.class })
public class FluidBlockRendererMixin {
	/**
	 * Heavy LiquidBlockRenderer tweak
	 *
	 * main method injection/redirect to compute fluid corner heights:
	 * 		tesselate -> calculateAverageHeight
	 *
	 * all these methods are overwritten with defaults (false, 0) to control the execution flow in tesselate.
	 * 		isFaceOccludedByNeighbor, isNeighborSameFluid, shouldRenderFace, getLightColor, vertex, getHeight
	 * the result is that:
	 * 		flag1 = true
	 * 		flag2 = flag3 = flag4 = flag5 = flag6 = false
	 * 		flag8 = false
	 * and we guarantee that the execution reaches the injection point (e.g. f11 = 0)
	 */

	@Shadow
	private float getHeight(BlockAndTintGetter bg, Fluid f, BlockPos pos, BlockState state, FluidState fluidState) {
		return 0.0F;  // disable internal check if full fluid block (f11)
	}

	@Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;calculateAverageHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;FFFLnet/minecraft/core/BlockPos;)F", ordinal = 0))
	public float gc(LiquidBlockRenderer renderer, BlockAndTintGetter w, Fluid f, float thisHeight,
					float northOrSouthNeighborHeight, float eastOrWestNeighborHeight, BlockPos cornerNeighbor) {
		// ordinal 0 => NORTH EAST corner
		BlockPos side1 = cornerNeighbor.relative(Direction.SOUTH);
		BlockPos side2 = cornerNeighbor.relative(Direction.WEST);
		BlockPos center = cornerNeighbor.relative(Direction.SOUTH).relative(Direction.WEST);
		return FFluidStatic.getCornerHeight(w, f, center, side1, side2, cornerNeighbor);
	}

	@Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;calculateAverageHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;FFFLnet/minecraft/core/BlockPos;)F", ordinal = 1))
	public float gc1(LiquidBlockRenderer renderer, BlockAndTintGetter w, Fluid f, float thisHeight,
					 float northOrSouthNeighborHeight, float eastOrWestNeighborHeight, BlockPos cornerNeighbor) {
		// ordinal 1 => NORTH WEST corner
		BlockPos side1 = cornerNeighbor.relative(Direction.SOUTH);
		BlockPos side2 = cornerNeighbor.relative(Direction.EAST);
		BlockPos center = cornerNeighbor.relative(Direction.SOUTH).relative(Direction.EAST);
		return FFluidStatic.getCornerHeight(w, f, center, side1, side2, cornerNeighbor);
	}

	@Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;calculateAverageHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;FFFLnet/minecraft/core/BlockPos;)F", ordinal = 2))
	public float gc2(LiquidBlockRenderer renderer, BlockAndTintGetter w, Fluid f, float thisHeight,
					 float northOrSouthNeighborHeight, float eastOrWestNeighborHeight, BlockPos cornerNeighbor) {
		// ordinal 2 => SOUTH EAST corner
		BlockPos side1 = cornerNeighbor.relative(Direction.NORTH);
		BlockPos side2 = cornerNeighbor.relative(Direction.WEST);
		BlockPos center = cornerNeighbor.relative(Direction.NORTH).relative(Direction.WEST);
		return FFluidStatic.getCornerHeight(w, f, center, side1, side2, cornerNeighbor);
	}

	@Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;calculateAverageHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;FFFLnet/minecraft/core/BlockPos;)F", ordinal = 3))
	public float gc3(LiquidBlockRenderer renderer, BlockAndTintGetter w, Fluid f, float thisHeight,
					 float northOrSouthNeighborHeight, float eastOrWestNeighborHeight, BlockPos cornerNeighbor) {
		// ordinal 3 => SOUTH WEST corner
		BlockPos side1 = cornerNeighbor.relative(Direction.NORTH);
		BlockPos side2 = cornerNeighbor.relative(Direction.EAST);
		BlockPos center = cornerNeighbor.relative(Direction.NORTH).relative(Direction.EAST);
		return FFluidStatic.getCornerHeight(w, f, center, side1, side2, cornerNeighbor);
	}

	@Shadow
	private void vertex(VertexConsumer vertexBuilderIn, double x, double y, double z, float red, float green,
							   float blue, float alpha, float u, float v, int packedLight) {
		// I have the feeling this is doing nothing...?
	}

	@Shadow
	private int getLightColor(BlockAndTintGetter lightReaderIn, BlockPos posIn) {
		return 0;
	}

	@Shadow
	private static boolean isFaceOccludedByNeighbor(BlockGetter lightReaderIn, BlockPos posIn, Direction down, float f, BlockState bs) {
		return false;
	}

	@Shadow
	private static boolean shouldRenderFace(BlockAndTintGetter lightReaderIn, BlockPos posIn, FluidState fluidStateIn,
			BlockState blockstate, Direction down, FluidState fluidStateOut) {
		return false;
	}

	@Shadow
	private static boolean isNeighborSameFluid(FluidState fs1, FluidState fs2) {
		return false;
	}
}