package net.skds.wpo.client.render;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.skds.wpo.fluidphysics.FFluidStatic;

@OnlyIn(Dist.CLIENT)
public class EFluidBlockRenderer extends FluidBlockRenderer {

	@Override
	public boolean render(IBlockDisplayReader world, BlockPos pos, IVertexBuilder vb, FluidState fluidState) {

		//TextureManager manager = Minecraft.getInstance().getTextureManager();

		TextureAtlasSprite[] atextureatlassprite = ForgeHooksClient.getFluidSprites(world, pos, fluidState);

		BlockState blockstate = world.getBlockState(pos);

		BlockState blockstateUp = world.getBlockState(pos.up());
		BlockState blockstateDown = world.getBlockState(pos.down());

		FluidState fluidstateUp = world.getFluidState(pos.up());
		FluidState fluidstateDown = world.getFluidState(pos.down());

		// color
		int i = fluidState.getFluid().getAttributes().getColor(world, pos);
		float alpha = (float) (i >> 24 & 255) / 255.0F;
		float f = (float) (i >> 16 & 255) / 255.0F;
		float f1 = (float) (i >> 8 & 255) / 255.0F;
		float f2 = (float) (i & 255) / 255.0F;

		// should render?
		BlockState[] radialStates = new BlockState[4];
		FluidState[] radialFluidStates = new FluidState[4];
		boolean[] renderSides = new boolean[4];

		boolean hasOpenSide = false;
		for (int n = 0; n < 4; n++) {
			Direction dir = Direction.byHorizontalIndex(n);
			BlockPos pos2 = pos.offset(dir);
			radialFluidStates[n] = world.getFluidState(pos2);
			radialStates[n] = world.getBlockState(pos2);

			if (willSideRender(world, dir, pos, blockstate, radialStates[n], fluidState, radialFluidStates[n])) {
				renderSides[n] = true;
				hasOpenSide = true;
			}
		}

		//boolean renderUp = isAdjacentFluidSameAs(world, pos, Direction.UP, fluidState);
		//boolean renderDown = isAdjacentFluidSameAs(world, pos, Direction.DOWN, fluidState);

		boolean renderUp = willSideRender(world, Direction.UP, pos, blockstate, blockstateUp, fluidState, fluidstateUp);
		boolean renderDown = willSideRender(world, Direction.DOWN, pos, blockstate, blockstateDown, fluidState,
				fluidstateDown);

		// render
		if (renderUp || renderDown || hasOpenSide) {

			// light mp

			float f3 = world.func_230487_a_(Direction.DOWN, true);
			float f4 = world.func_230487_a_(Direction.UP, true);
			float f5 = world.func_230487_a_(Direction.NORTH, true);
			float f6 = world.func_230487_a_(Direction.WEST, true);

			// height

			float[] fl = getConH(world, pos, fluidState.getFluid());
			//float[] fl = {0.5f,0.5f,0.5f,0.5f};
			float f7 = fl[0];
			float f8 = fl[1];
			float f9 = fl[2];
			float f10 = fl[3];

			// pos

			double d0 = (pos.getX() & 15);
			double d1 = (pos.getY() & 15);
			double d2 = (pos.getZ() & 15);

			// render sides

			float f12 = 0.001F;
			for (int l = 0; l < 4; ++l) {
				if (!renderSides[l]) {
					continue;
				}
				float f36;
				float f38;
				double d3;
				double d4;
				double d5;
				double d6;
				Direction direction = Direction.byHorizontalIndex(l);
				if (l == 0) {
					f36 = f9;
					f38 = f8;
					d3 = d0 + 1.0D;
					d5 = d0;
					d4 = d2 + 1.0D - 0.001F;
					d6 = d2 + 1.0D - 0.001F;
				} else if (l == 1) {
					f36 = f8;
					f38 = f7;
					d3 = d0 + 0.001F;
					d5 = d0 + 0.001F;
					d4 = d2 + 1.0D;
					d6 = d2;
				} else if (l == 2) {
					f36 = f7;
					f38 = f10;
					d3 = d0;
					d5 = d0 + 1.0D;
					d4 = d2 + 0.001F;
					d6 = d2 + 0.001F;
				} else {
					f36 = f10;
					f38 = f9;
					d3 = d0 + 1.0D - 0.001F;
					d5 = d0 + 1.0D - 0.001F;
					d4 = d2;
					d6 = d2 + 1.0D;
				}

				BlockPos blockpos = pos.offset(direction);
				TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
				if (atextureatlassprite[2] != null) {
					if (radialStates[l].shouldDisplayFluidOverlay(world, blockpos, fluidState)) {
						textureatlassprite2 = atextureatlassprite[2];
					}
				}

				IVertexBuilder vertexBuilder = textureatlassprite2.wrapBuffer(vb);

				float f48 = textureatlassprite2.getInterpolatedU(0.0D);
				float f49 = textureatlassprite2.getInterpolatedU(8.0D);
				float f50 = textureatlassprite2.getInterpolatedV((double) ((1.0F - f36) * 16.0F * 0.5F));
				float f28 = textureatlassprite2.getInterpolatedV((double) ((1.0F - f38) * 16.0F * 0.5F));
				float f29 = textureatlassprite2.getInterpolatedV(8.0D);
				int k = this.getCombinedAverageLight(world, blockpos);
				float f30 = l < 2 ? f5 : f6;
				float f31 = f4 * f30 * f;
				float f32 = f4 * f30 * f1;
				float f33 = f4 * f30 * f2;
				this.vertexVanilla(vertexBuilder, d3, d1 + f36, d4, f31, f32, f33, alpha, f48, f50, k);
				this.vertexVanilla(vertexBuilder, d5, d1 + f38, d6, f31, f32, f33, alpha, f49, f28, k);
				this.vertexVanilla(vertexBuilder, d5, d1 + f12, d6, f31, f32, f33, alpha, f49, f29, k);
				this.vertexVanilla(vertexBuilder, d3, d1 + f12, d4, f31, f32, f33, alpha, f48, f29, k);
				if (textureatlassprite2 != atextureatlassprite[2]) {
					this.vertexVanilla(vertexBuilder, d3, d1 + f12, d4, f31, f32, f33, alpha, f48, f29, k);
					this.vertexVanilla(vertexBuilder, d5, d1 + f12, d6, f31, f32, f33, alpha, f49, f29, k);
					this.vertexVanilla(vertexBuilder, d5, d1 + f38, d6, f31, f32, f33, alpha, f49, f28, k);
					this.vertexVanilla(vertexBuilder, d3, d1 + f36, d4, f31, f32, f33, alpha, f48, f50, k);

				}
			}

			// render top

			if (renderUp) {
				f7 -= 0.001F;
				f8 -= 0.001F;
				f9 -= 0.001F;
				f10 -= 0.001F;
				Vector3d vector3d = fluidState.getFlow(world, pos);
				float f13;
				float f14;
				float f15;
				float f16;
				float f17;
				float f18;
				float f19;
				float f20;
				
				IVertexBuilder vertexBuilder;
				if (vector3d.x == 0.0D && vector3d.z == 0.0D) {
					TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];

					vertexBuilder = textureatlassprite1.wrapBuffer(vb);

					f13 = textureatlassprite1.getInterpolatedU(0.0D);
					f17 = textureatlassprite1.getInterpolatedV(0.0D);
					f14 = f13;
					f18 = textureatlassprite1.getInterpolatedV(16.0D);
					f15 = textureatlassprite1.getInterpolatedU(16.0D);
					f19 = f18;
					f16 = f15;
					f20 = f17;
				} else {
					TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
					
					vertexBuilder = textureatlassprite.wrapBuffer(vb);
					float f21 = (float) MathHelper.atan2(vector3d.z, vector3d.x) - ((float) Math.PI / 2F);
					float f22 = MathHelper.sin(f21) * 0.25F;
					float f23 = MathHelper.cos(f21) * 0.25F;

					f13 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f23 - f22) * 16.0F));
					f17 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f23 + f22) * 16.0F));
					f14 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f23 + f22) * 16.0F));
					f18 = textureatlassprite.getInterpolatedV((double) (8.0F + (f23 + f22) * 16.0F));
					f15 = textureatlassprite.getInterpolatedU((double) (8.0F + (f23 + f22) * 16.0F));
					f19 = textureatlassprite.getInterpolatedV((double) (8.0F + (f23 - f22) * 16.0F));
					f16 = textureatlassprite.getInterpolatedU((double) (8.0F + (f23 - f22) * 16.0F));
					f20 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f23 - f22) * 16.0F));
				}

				float f43 = (f13 + f14 + f15 + f16) / 4.0F;
				float f44 = (f17 + f18 + f19 + f20) / 4.0F;
				float f45 = (float) atextureatlassprite[0].getWidth()
						/ (atextureatlassprite[0].getMaxU() - atextureatlassprite[0].getMinU());
				float f46 = (float) atextureatlassprite[0].getHeight()
						/ (atextureatlassprite[0].getMaxV() - atextureatlassprite[0].getMinV());
				float f47 = 4.0F / Math.max(f46, f45);
				f13 = MathHelper.lerp(f47, f13, f43);
				f14 = MathHelper.lerp(f47, f14, f43);
				f15 = MathHelper.lerp(f47, f15, f43);
				f16 = MathHelper.lerp(f47, f16, f43);
				f17 = MathHelper.lerp(f47, f17, f44);
				f18 = MathHelper.lerp(f47, f18, f44);
				f19 = MathHelper.lerp(f47, f19, f44);
				f20 = MathHelper.lerp(f47, f20, f44);
				int j = this.getCombinedAverageLight(world, pos);
				float f25 = f4 * f;
				float f26 = f4 * f1;
				float f27 = f4 * f2;
				this.vertexVanilla(vertexBuilder, d0 + 0.0D, d1 + f7, d2 + 0.0D, f25, f26, f27, alpha, f13, f17, j);
				this.vertexVanilla(vertexBuilder, d0 + 0.0D, d1 + f8, d2 + 1.0D, f25, f26, f27, alpha, f14, f18, j);
				this.vertexVanilla(vertexBuilder, d0 + 1.0D, d1 + f9, d2 + 1.0D, f25, f26, f27, alpha, f15, f19, j);
				this.vertexVanilla(vertexBuilder, d0 + 1.0D, d1 + f10, d2 + 0.0D, f25, f26, f27, alpha, f16, f20, j);
				if (fluidState.shouldRenderSides(world, pos.up())) {
					this.vertexVanilla(vertexBuilder, d0 + 0.0D, d1 + f7, d2 + 0.0D, f25, f26, f27, alpha, f13, f17, j);
					this.vertexVanilla(vertexBuilder, d0 + 1.0D, d1 + f10, d2 + 0.0D, f25, f26, f27, alpha, f16, f20,
							j);
					this.vertexVanilla(vertexBuilder, d0 + 1.0D, d1 + f9, d2 + 1.0D, f25, f26, f27, alpha, f15, f19, j);
					this.vertexVanilla(vertexBuilder, d0 + 0.0D, d1 + f8, d2 + 1.0D, f25, f26, f27, alpha, f14, f18, j);
				}
			}

			// render bot

			if (renderDown) {
				IVertexBuilder vertexBuilder = atextureatlassprite[0].wrapBuffer(vb);
				float f34 = atextureatlassprite[0].getMinU();
				float f35 = atextureatlassprite[0].getMaxU();
				float f37 = atextureatlassprite[0].getMinV();
				float f39 = atextureatlassprite[0].getMaxV();
				int i1 = this.getCombinedAverageLight(world, pos.down());
				float f40 = f3 * f;
				float f41 = f3 * f1;
				float f42 = f3 * f2;
				this.vertexVanilla(vertexBuilder, d0, d1 + f12, d2 + 1.0D, f40, f41, f42, alpha, f34, f39, i1);
				this.vertexVanilla(vertexBuilder, d0, d1 + f12, d2, f40, f41, f42, alpha, f34, f37, i1);
				this.vertexVanilla(vertexBuilder, d0 + 1.0D, d1 + f12, d2, f40, f41, f42, alpha, f35, f37, i1);
				this.vertexVanilla(vertexBuilder, d0 + 1.0D, d1 + f12, d2 + 1.0D, f40, f41, f42, alpha, f35, f39, i1);
			}

			return true;
		}
		return false;
	}

	private void vertexVanilla(IVertexBuilder vertexBuilder, double x, double y, double z, float red, float green,
			float blue, float alpha, float u, float v, int packedLight) {
		vertexBuilder.pos(x, y, z).color(red, green, blue, alpha).tex(u, v).lightmap(packedLight)
				.normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	private int getCombinedAverageLight(IBlockDisplayReader world, BlockPos pos) {
		int i = WorldRenderer.getCombinedLight(world, pos);
		int j = WorldRenderer.getCombinedLight(world, pos.up());
		int k = i & 255;
		int l = j & 255;
		int i1 = i >> 16 & 255;
		int j1 = j >> 16 & 255;
		return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
	}

	private static boolean willSideRender(IBlockReader world, Direction dir, BlockPos pos, BlockState state,
			BlockState state2, FluidState fs, FluidState fs2) {

		if (isSameFluid(fs, fs2)) {
			return false;
		}

		if (FFluidStatic.canReach(pos, pos.offset(dir), state, state2, fs.getFluid(), world)) {
			return true;
		}

		if (dir == Direction.UP) {
			return fs.getLevel() < 8;
		}

		return false;
	}

	private static boolean isSameFluid(FluidState fs, FluidState fs2) {
		return fs.getFluid().isEquivalentTo(fs2.getFluid());
	}

	public static float[] getConH(IBlockReader w, BlockPos pos, Fluid fluid) {
		int[] count = new int[] { 1, 1, 1, 1 };
		boolean[] conner = new boolean[4];
		boolean[] setconner = new boolean[4];
		float[] setconnervl = new float[4];
		// boolean downtry = false;
		boolean downsuc = false;

		float offset = 0.0036F;
		float offset2 = 0.99999F;

		BlockPos posd = null;
		BlockState stated = null;

		BlockState state = w.getBlockState(pos);
		float level = state.getFluidState().getHeight();
		float[] sum = new float[] { level, level, level, level };

		BlockPos posu = pos.up();
		BlockState statu = w.getBlockState(posu);
		FluidState ufs = w.getFluidState(posu);

		boolean posus = FFluidStatic.canReach(pos, posu, state, statu, fluid, w);

		if (fluid.isEquivalentTo(ufs.getFluid()) && posus) {
			return new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
		}

		posd = pos.down();
		stated = w.getBlockState(posd);
		downsuc = (stated.getFluidState().getFluid().isEquivalentTo(fluid));

		if (posus) {
			offset2 = 1.0F;
		}

		if (downsuc) {
			offset = 0.0F;
		}

		// int n = -1;
		Direction dir = Direction.EAST;
		for (int n = 0; n < 4; n++) {
			dir = dir.rotateYCCW();
			// ++n;
			int n2 = n > 0 ? n - 1 : 3;
			BlockPos pos2 = pos.offset(dir);
			BlockState state2 = w.getBlockState(pos2);

			boolean reach2 = FFluidStatic.canReach(pos, pos2, state, state2, fluid, w);
			boolean same2 = state2.getFluidState().getFluid().isEquivalentTo(fluid);
			if (same2 && reach2) {

				BlockPos pos2u = pos2.up();
				BlockState state2u = w.getBlockState(pos2u);
				if (state2u.getFluidState().getFluid().isEquivalentTo(fluid)
						&& FFluidStatic.canReach(pos2, pos2u, state2, state2u, fluid, w)) {
					conner[n] = true;
					conner[n2] = true;
					setconner[n] = true;
					setconner[n2] = true;
					setconnervl[n] = offset2;
					setconnervl[n2] = offset2;
				} else {
					float level2 = state2.getFluidState().getHeight();
					sum[n] += level2;
					sum[n2] += level2;
					count[n]++;
					count[n2]++;
				}
				Direction[] dirside = new Direction[2];
				dirside[0] = dir.rotateY();
				dirside[1] = dir.rotateYCCW();

				for (int i = 0; i < 2; i++) {
					if (i == 0 && (conner[n2])) {
						continue;
					}
					if (i == 1 && (conner[n])) {
						continue;
					}
					BlockPos pos2dir = pos2.offset(dirside[i]);
					BlockState state2dir = w.getBlockState(pos2dir);
					if (FFluidStatic.canReach(pos2, pos2dir, state2, state2dir, fluid, w)) {

						if (state2dir.getFluidState().getFluid().isEquivalentTo(fluid)) {

							BlockPos pos2diru = pos2dir.up();
							BlockState state2diru = w.getBlockState(pos2diru);
							if (state2diru.getFluidState().getFluid().isEquivalentTo(fluid)
									&& FFluidStatic.canReach(pos2dir, pos2diru, state2dir, state2diru, fluid, w)) {
								if (i == 0) {
									setconnervl[n2] = offset2;
									setconner[n2] = true;
									conner[n2] = true;
								} else {
									setconnervl[n] = offset2;
									setconner[n] = true;
									conner[n] = true;
								}
							} else {
								float level2dir = state2dir.getFluidState().getHeight();
								if (i == 0) {
									sum[n2] += level2dir;
									count[n2]++;
									conner[n2] = true;
								} else {
									sum[n] += level2dir;
									count[n]++;
									conner[n] = true;
								}
							}

						} else if (state2dir.getFluidState().isEmpty()) {
							BlockPos pos2dird = pos2dir.down();
							BlockState state2dird = w.getBlockState(pos2dird);
							if (state2dird.getFluidState().getFluid().isEquivalentTo(fluid)
									&& FFluidStatic.canReach(pos2dir, pos2dird, state2dir, state2dird, fluid, w)) {
								if (i == 0) {
									if (!setconner[n2])
										setconnervl[n2] = offset;
									setconner[n2] = true;
									conner[n2] = true;
								} else {
									if (!setconner[n2])
										setconnervl[n] = offset;
									setconner[n] = true;
									conner[n] = true;
								}
							}
						}
					}
				}
			} else {

				if (reach2) {
					BlockPos pos2d = pos2.down();
					BlockState state2d = w.getBlockState(pos2d);
					if (state2d.getFluidState().getFluid().isEquivalentTo(fluid)
							&& FFluidStatic.canReach(pos2, pos2d, state2, state2d, fluid, w)) {
						if (!setconner[n]) {
							setconner[n] = true;
							setconnervl[n] = offset;
						}
						if (!setconner[n2]) {
							setconner[n2] = true;
							setconnervl[n2] = offset;
						}
					}
				}
			}
		}

		float[] ch = new float[4];
		for (int i = 0; i < 4; i++) {
			if (setconner[i]) {
				ch[i] = setconnervl[i];
			} else {
				ch[i] = (float) sum[i] / count[i];
			}
		}
		return ch;
	}
}
