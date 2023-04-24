package net.skds.wpo.mixins.fluids;

import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.skds.wpo.fluidphysics.FFluidStatic;

@Mixin(value = { FluidBlockRenderer.class })
public class FluidBlockRendererMixin {

	ConcurrentHashMap<Long, float[]> customAH = new ConcurrentHashMap<>();
	float[] customAHSafe = new float[4];
	// render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z

	@Redirect(method = "tesselate(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getWaterHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 0))
	public float gc(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] flex = FFluidStatic.getConH(w, p, f);
		customAH.put(p.asLong(), flex);
		customAHSafe = flex;
		return flex[0];

	}

	@Redirect(method = "tesselate(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getWaterHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 1))
	public float gc1(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] ffmas = customAH.get(p.north().asLong());
		if (ffmas == null) {
			ffmas = customAHSafe;
		}
		float fll = ffmas[1];
		return fll;

	}

	@Redirect(method = "tesselate(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getWaterHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 2))
	public float gc2(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] ffmas = customAH.get(p.north().west().asLong());
		if (ffmas == null) {
			ffmas = customAHSafe;
		}
		float fll = ffmas[2];
		return fll;

	}

	@Redirect(method = "tesselate(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FluidBlockRenderer;getWaterHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F", ordinal = 3))
	public float gc3(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {

		float[] ffmas = customAH.remove(p.west().asLong());
		if (ffmas == null) {
			ffmas = customAHSafe;
		}
		float fll = ffmas[3];
		return fll;

	}

	// @Redirect(method =
	// "render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z",
	// at //= @At(value = "INVOKE", target =
	// "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft///fluid/Fluid;)F",
	// ordinal = 2))
	// public float gc3(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f)
	// {
	// if (customFF) {
	// return customAH[2];
	// } else {
	// return getFluidHeight(w, p, f);
	// }
	// }
	//
	// @Redirect(method =
	// "render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z",
	// at //= @At(value = "INVOKE", target =
	// "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft///fluid/Fluid;)F",
	// ordinal = 3))
	// public float gc4(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f)
	// {
	// if (customFF) {
	// return customAH[3];
	// } else {
	// return getFluidHeight(w, p, f);
	// }
	// }

	/*
	 * @Redirect(method =
	 * "render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z",
	 * at = @At(value = "INVOKE", target =
	 * "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F",
	 * ordinal = 0)) public float gc(FluidBlockRenderer fr, IBlockReader w, BlockPos
	 * p, Fluid f) { if (PhysEXConfig.COMMON.finiteFluids.get()) { return
	 * FFluidStatic.getConH(w, p, f, new BlockPos(0, 0, 0).north().west()); } else {
	 * return getFluidHeight(w, p, f); } }
	 *
	 * @Redirect(method =
	 * "render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z",
	 * at = @At(value = "INVOKE", target =
	 * "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F",
	 * ordinal = 1)) public float gc1(FluidBlockRenderer fr, IBlockReader w,
	 * BlockPos p, Fluid f) { if (PhysEXConfig.COMMON.finiteFluids.get()) { return
	 * FFluidStatic.getConH(w, p.north(), f, new BlockPos(0, 0, 0).south().west());
	 * } else { return getFluidHeight(w, p, f); } }
	 *
	 * @Redirect(method =
	 * "render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z",
	 * at = @At(value = "INVOKE", target =
	 * "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F",
	 * ordinal = 2)) public float gc2(FluidBlockRenderer fr, IBlockReader w,
	 * BlockPos p, Fluid f) { if (PhysEXConfig.COMMON.finiteFluids.get()) { return
	 * FFluidStatic.getConH(w, p.north().west(), f, new BlockPos(0, 0,
	 * 0).south().east()); } else { return getFluidHeight(w, p, f); } }
	 *
	 * @Redirect(method =
	 * "render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z",
	 * at = @At(value = "INVOKE", target =
	 * "Lnet/minecraft/client/renderer/FluidBlockRenderer;getFluidHeight(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;)F",
	 * ordinal = 3)) public float gc3(FluidBlockRenderer fr, IBlockReader w,
	 * BlockPos p, Fluid f) { if (PhysEXConfig.COMMON.finiteFluids.get()) { return
	 * FFluidStatic.getConH(w, p.west(), f, new BlockPos(0, 0, 0).north().east()); }
	 * else { return getFluidHeight(w, p, f); } }
	 */
	// ==================

	@Shadow
	private void vertexVanilla(IVertexBuilder vertexBuilderIn, double x, double y, double z, float red, float green,
							   float blue, float alpha, float u, float v, int packedLight) {
	}

	@Shadow
	private int getLightColor(IBlockDisplayReader lightReaderIn, BlockPos posIn) {
		return 0;
	}

	@Shadow
	private static boolean isFaceOccludedByNeighbor(IBlockReader lightReaderIn, BlockPos posIn, Direction down, float f) {
		return false;
	}

	@Shadow
	private static boolean shouldRenderFace(IBlockDisplayReader lightReaderIn, BlockPos posIn, FluidState fluidStateIn,
			BlockState blockstate, Direction down) {
		return false;
	}

	@Shadow
	private static boolean isNeighborSameFluid(IBlockReader lightReaderIn, BlockPos posIn, Direction up,
			FluidState fluidStateIn) {
		return false;
	}

//	@Shadow
//	private float getWaterHeight(IBlockReader reader, BlockPos pos, Fluid fluidIn) {
//		return 0;
//	}
	// @Inject(method =
	// "render(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/fluid/FluidState;)Z",
	// at = @At(value = "HEAD", ordinal = 0), cancellable = true)
	// public boolean gc(IBlockDisplayReader lightReaderIn, BlockPos posIn,
	// IVertexBuilder vertexBuilderIn,
	// FluidState fluidStateIn, CallbackInfoReturnable<Boolean> ci) {
	//
	// if (!PhysEXConfig.COMMON.finiteFluids.get()) {
	// return ci.getReturnValue();
	// }
	//
	// //boolean flag = fluidStateIn.isTagged(FluidTags.LAVA);
	// TextureAtlasSprite[] atextureatlassprite =
	// net.minecraftforge.client.ForgeHooksClient
	// .getFluidSprites(lightReaderIn, posIn, fluidStateIn);
	// BlockState blockstate = lightReaderIn.getBlockState(posIn);
	// int i = fluidStateIn.getFluid().getAttributes().getColor(lightReaderIn,
	// posIn);
	// float alpha = (float) (i >> 24 & 255) / 255.0F;
	// float f = (float) (i >> 16 & 255) / 255.0F;
	// float f1 = (float) (i >> 8 & 255) / 255.0F;
	// float f2 = (float) (i & 255) / 255.0F;
	// boolean flag1 = !isAdjacentFluidSameAs(lightReaderIn, posIn, Direction.UP,
	// fluidStateIn);
	// boolean flag2 = func_239281_a_(lightReaderIn, posIn, fluidStateIn,
	// blockstate, Direction.DOWN)
	// && !func_239283_a_(lightReaderIn, posIn, Direction.DOWN, 0.8888889F);
	// boolean flag3 = func_239281_a_(lightReaderIn, posIn, fluidStateIn,
	// blockstate, Direction.NORTH);
	// boolean flag4 = func_239281_a_(lightReaderIn, posIn, fluidStateIn,
	// blockstate, Direction.SOUTH);
	// boolean flag5 = func_239281_a_(lightReaderIn, posIn, fluidStateIn,
	// blockstate, Direction.WEST);
	// boolean flag6 = func_239281_a_(lightReaderIn, posIn, fluidStateIn,
	// blockstate, Direction.EAST);
	// if (!flag1 && !flag2 && !flag6 && !flag5 && !flag3 && !flag4) {
	// ci.setReturnValue(false);
	// return false;
	// } else {
	// boolean flag7 = false;
	// float f3 = lightReaderIn.func_230487_a_(Direction.DOWN, true);
	// float f4 = lightReaderIn.func_230487_a_(Direction.UP, true);
	// float f5 = lightReaderIn.func_230487_a_(Direction.NORTH, true);
	// float f6 = lightReaderIn.func_230487_a_(Direction.WEST, true);
	// float[] ch = FFluidStatic.getConH(lightReaderIn, posIn,
	// fluidStateIn.getFluid());
	// float f7 = ch[0];
	// float f8 = ch[1];
	// float f9 = ch[2];
	// float f10 = ch[3];
	// double d0 = (double) (posIn.getX() & 15);
	// double d1 = (double) (posIn.getY() & 15);
	// double d2 = (double) (posIn.getZ() & 15);
	// //float f11 = 0.001F;
	// float f12 = flag2 ? 0.001F : 0.0F;
	// if (flag1 && !func_239283_a_(lightReaderIn, posIn, Direction.UP,
	// Math.min(Math.min(f7, f8), Math.min(f9, f10)))) {
	// flag7 = true;
	// f7 -= 0.001F;
	// f8 -= 0.001F;
	// f9 -= 0.001F;
	// f10 -= 0.001F;
	// Vector3d vector3d = fluidStateIn.getFlow(lightReaderIn, posIn);
	// float f13;
	// float f14;
	// float f15;
	// float f16;
	// float f17;
	// float f18;
	// float f19;
	// float f20;
	// if (vector3d.x == 0.0D && vector3d.z == 0.0D) {
	// TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
	// f13 = textureatlassprite1.getInterpolatedU(0.0D);
	// f17 = textureatlassprite1.getInterpolatedV(0.0D);
	// f14 = f13;
	// f18 = textureatlassprite1.getInterpolatedV(16.0D);
	// f15 = textureatlassprite1.getInterpolatedU(16.0D);
	// f19 = f18;
	// f16 = f15;
	// f20 = f17;
	// } else {
	// TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
	// float f21 = (float) MathHelper.atan2(vector3d.z, vector3d.x) - ((float)
	// Math.PI / 2F);
	// float f22 = MathHelper.sin(f21) * 0.25F;
	// float f23 = MathHelper.cos(f21) * 0.25F;
	// //float f24 = 8.0F;
	// f13 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f23 - f22) *
	// 16.0F));
	// f17 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f23 + f22) *
	// 16.0F));
	// f14 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f23 + f22) *
	// 16.0F));
	// f18 = textureatlassprite.getInterpolatedV((double) (8.0F + (f23 + f22) *
	// 16.0F));
	// f15 = textureatlassprite.getInterpolatedU((double) (8.0F + (f23 + f22) *
	// 16.0F));
	// f19 = textureatlassprite.getInterpolatedV((double) (8.0F + (f23 - f22) *
	// 16.0F));
	// f16 = textureatlassprite.getInterpolatedU((double) (8.0F + (f23 - f22) *
	// 16.0F));
	// f20 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f23 - f22) *
	// 16.0F));
	// }
	//
	// float f43 = (f13 + f14 + f15 + f16) / 4.0F;
	// float f44 = (f17 + f18 + f19 + f20) / 4.0F;
	// float f45 = (float) atextureatlassprite[0].getWidth()
	// / (atextureatlassprite[0].getMaxU() - atextureatlassprite[0].getMinU());
	// float f46 = (float) atextureatlassprite[0].getHeight()
	// / (atextureatlassprite[0].getMaxV() - atextureatlassprite[0].getMinV());
	// float f47 = 4.0F / Math.max(f46, f45);
	// f13 = MathHelper.lerp(f47, f13, f43);
	// f14 = MathHelper.lerp(f47, f14, f43);
	// f15 = MathHelper.lerp(f47, f15, f43);
	// f16 = MathHelper.lerp(f47, f16, f43);
	// f17 = MathHelper.lerp(f47, f17, f44);
	// f18 = MathHelper.lerp(f47, f18, f44);
	// f19 = MathHelper.lerp(f47, f19, f44);
	// f20 = MathHelper.lerp(f47, f20, f44);
	// int j = this.getCombinedAverageLight(lightReaderIn, posIn);
	// float f25 = f4 * f;
	// float f26 = f4 * f1;
	// float f27 = f4 * f2;
	// this.vertexVanilla(vertexBuilderIn, d0 + 0.0D, d1 + (double) f7, d2 + 0.0D,
	// f25, f26, f27, alpha, f13,
	// f17, j);
	// this.vertexVanilla(vertexBuilderIn, d0 + 0.0D, d1 + (double) f8, d2 + 1.0D,
	// f25, f26, f27, alpha, f14,
	// f18, j);
	// this.vertexVanilla(vertexBuilderIn, d0 + 1.0D, d1 + (double) f9, d2 + 1.0D,
	// f25, f26, f27, alpha, f15,
	// f19, j);
	// this.vertexVanilla(vertexBuilderIn, d0 + 1.0D, d1 + (double) f10, d2 + 0.0D,
	// f25, f26, f27, alpha, f16,
	// f20, j);
	// if (fluidStateIn.shouldRenderSides(lightReaderIn, posIn.up())) {
	// this.vertexVanilla(vertexBuilderIn, d0 + 0.0D, d1 + (double) f7, d2 + 0.0D,
	// f25, f26, f27, alpha,
	// f13, f17, j);
	// this.vertexVanilla(vertexBuilderIn, d0 + 1.0D, d1 + (double) f10, d2 + 0.0D,
	// f25, f26, f27, alpha,
	// f16, f20, j);
	// this.vertexVanilla(vertexBuilderIn, d0 + 1.0D, d1 + (double) f9, d2 + 1.0D,
	// f25, f26, f27, alpha,
	// f15, f19, j);
	// this.vertexVanilla(vertexBuilderIn, d0 + 0.0D, d1 + (double) f8, d2 + 1.0D,
	// f25, f26, f27, alpha,
	// f14, f18, j);
	// }
	// }
	//
	// if (flag2) {
	// float f34 = atextureatlassprite[0].getMinU();
	// float f35 = atextureatlassprite[0].getMaxU();
	// float f37 = atextureatlassprite[0].getMinV();
	// float f39 = atextureatlassprite[0].getMaxV();
	// int i1 = this.getCombinedAverageLight(lightReaderIn, posIn.down());
	// float f40 = f3 * f;
	// float f41 = f3 * f1;
	// float f42 = f3 * f2;
	// this.vertexVanilla(vertexBuilderIn, d0, d1 + (double) f12, d2 + 1.0D, f40,
	// f41, f42, alpha, f34, f39,
	// i1);
	// this.vertexVanilla(vertexBuilderIn, d0, d1 + (double) f12, d2, f40, f41, f42,
	// alpha, f34, f37, i1);
	// this.vertexVanilla(vertexBuilderIn, d0 + 1.0D, d1 + (double) f12, d2, f40,
	// f41, f42, alpha, f35, f37,
	// i1);
	// this.vertexVanilla(vertexBuilderIn, d0 + 1.0D, d1 + (double) f12, d2 + 1.0D,
	// f40, f41, f42, alpha, f35,
	// f39, i1);
	// flag7 = true;
	// }
	//
	// for (int l = 0; l < 4; ++l) {
	// float f36;
	// float f38;
	// double d3;
	// double d4;
	// double d5;
	// double d6;
	// Direction direction;
	// boolean flag8;
	// if (l == 0) {
	// f36 = f7;
	// f38 = f10;
	// d3 = d0;
	// d5 = d0 + 1.0D;
	// d4 = d2 + (double) 0.001F;
	// d6 = d2 + (double) 0.001F;
	// direction = Direction.NORTH;
	// flag8 = flag3;
	// } else if (l == 1) {
	// f36 = f9;
	// f38 = f8;
	// d3 = d0 + 1.0D;
	// d5 = d0;
	// d4 = d2 + 1.0D - (double) 0.001F;
	// d6 = d2 + 1.0D - (double) 0.001F;
	// direction = Direction.SOUTH;
	// flag8 = flag4;
	// } else if (l == 2) {
	// f36 = f8;
	// f38 = f7;
	// d3 = d0 + (double) 0.001F;
	// d5 = d0 + (double) 0.001F;
	// d4 = d2 + 1.0D;
	// d6 = d2;
	// direction = Direction.WEST;
	// flag8 = flag5;
	// } else {
	// f36 = f10;
	// f38 = f9;
	// d3 = d0 + 1.0D - (double) 0.001F;
	// d5 = d0 + 1.0D - (double) 0.001F;
	// d4 = d2;
	// d6 = d2 + 1.0D;
	// direction = Direction.EAST;
	// flag8 = flag6;
	// }
	//
	// if (flag8 && !func_239283_a_(lightReaderIn, posIn, direction, Math.max(f36,
	// f38))) {
	// flag7 = true;
	// BlockPos blockpos = posIn.offset(direction);
	// TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
	// if (atextureatlassprite[2] != null) {
	// if
	// (lightReaderIn.getBlockState(blockpos).shouldDisplayFluidOverlay(lightReaderIn,
	// blockpos,
	// fluidStateIn)) {
	// textureatlassprite2 = atextureatlassprite[2];
	// }
	// }
	//
	// float f48 = textureatlassprite2.getInterpolatedU(0.0D);
	// float f49 = textureatlassprite2.getInterpolatedU(8.0D);
	// float f50 = textureatlassprite2.getInterpolatedV((double) ((1.0F - f36) *
	// 16.0F * 0.5F));
	// float f28 = textureatlassprite2.getInterpolatedV((double) ((1.0F - f38) *
	// 16.0F * 0.5F));
	// float f29 = textureatlassprite2.getInterpolatedV(8.0D);
	// int k = this.getCombinedAverageLight(lightReaderIn, blockpos);
	// float f30 = l < 2 ? f5 : f6;
	// float f31 = f4 * f30 * f;
	// float f32 = f4 * f30 * f1;
	// float f33 = f4 * f30 * f2;
	// this.vertexVanilla(vertexBuilderIn, d3, d1 + (double) f36, d4, f31, f32, f33,
	// alpha, f48, f50, k);
	// this.vertexVanilla(vertexBuilderIn, d5, d1 + (double) f38, d6, f31, f32, f33,
	// alpha, f49, f28, k);
	// this.vertexVanilla(vertexBuilderIn, d5, d1 + (double) f12, d6, f31, f32, f33,
	// alpha, f49, f29, k);
	// this.vertexVanilla(vertexBuilderIn, d3, d1 + (double) f12, d4, f31, f32, f33,
	// alpha, f48, f29, k);
	// if (textureatlassprite2 != atextureatlassprite[2]) {
	// this.vertexVanilla(vertexBuilderIn, d3, d1 + (double) f12, d4, f31, f32, f33,
	// alpha, f48, f29,
	// k);
	// this.vertexVanilla(vertexBuilderIn, d5, d1 + (double) f12, d6, f31, f32, f33,
	// alpha, f49, f29,
	// k);
	// this.vertexVanilla(vertexBuilderIn, d5, d1 + (double) f38, d6, f31, f32, f33,
	// alpha, f49, f28,
	// k);
	// this.vertexVanilla(vertexBuilderIn, d3, d1 + (double) f36, d4, f31, f32, f33,
	// alpha, f48, f50,
	// k);
	// }
	// }
	// }
	//
	// ci.setReturnValue(flag7);
	// return flag7;
	// }
	// }
}