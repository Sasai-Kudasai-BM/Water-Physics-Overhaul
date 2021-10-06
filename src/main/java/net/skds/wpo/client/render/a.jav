// Decompiled with: CFR 0.150
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.skds.wpo.fluidphysics.FFluidStatic;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class FluidBlockRenderer {
    private final TextureAtlasSprite[] field_178272_a;
    private final TextureAtlasSprite[] field_178271_b;
    private TextureAtlasSprite field_187501_d;
    ConcurrentHashMap customAH = new ConcurrentHashMap();
    float[] customAHSafe = new float[4];

    public FluidBlockRenderer() {
        this.field_178272_a = new TextureAtlasSprite[2];
        this.field_178271_b = new TextureAtlasSprite[2];
    }

    protected void func_178268_a() {
        this.field_178272_a[0] = Minecraft.func_71410_x().func_209506_al().func_174954_c().func_178125_b(Blocks.field_150353_l.func_176223_P()).func_177554_e();
        this.field_178272_a[1] = ModelBakery.field_207766_d.func_229314_c_();
        this.field_178271_b[0] = Minecraft.func_71410_x().func_209506_al().func_174954_c().func_178125_b(Blocks.field_150355_j.func_176223_P()).func_177554_e();
        this.field_178271_b[1] = ModelBakery.field_207768_f.func_229314_c_();
        this.field_187501_d = ModelBakery.field_207769_g.func_229314_c_();
    }

    private static boolean func_209557_a(IBlockReader worldIn, BlockPos pos, Direction side, FluidState state) {
        BlockPos blockpos = pos.func_177972_a(side);
        FluidState fluidstate = worldIn.func_204610_c(blockpos);
        return fluidstate.func_206886_c().func_207187_a(state.func_206886_c());
    }

    private static boolean func_239284_a_(IBlockReader reader, Direction face, float heightIn, BlockPos pos, BlockState blockState) {
        if (blockState.func_200132_m()) {
            VoxelShape voxelshape = VoxelShapes.func_197873_a(0.0, 0.0, 0.0, 1.0, heightIn, 1.0);
            VoxelShape voxelshape1 = blockState.func_235754_c_(reader, pos);
            return VoxelShapes.func_197875_a(voxelshape, voxelshape1, face);
        }
        return false;
    }

    private static boolean func_239283_a_(IBlockReader reader, BlockPos pos, Direction face, float heightIn) {
        BlockPos blockpos = pos.func_177972_a(face);
        BlockState blockstate = reader.func_180495_p(blockpos);
        return FluidBlockRenderer.func_239284_a_(reader, face, heightIn, blockpos, blockstate);
    }

    private static boolean func_239282_a_(IBlockReader reader, BlockPos pos, BlockState blockState, Direction face) {
        return FluidBlockRenderer.func_239284_a_(reader, face.func_176734_d(), 1.0f, pos, blockState);
    }

    public static boolean func_239281_a_(IBlockDisplayReader reader, BlockPos pos, FluidState fluidStateIn, BlockState blockStateIn, Direction sideIn) {
        return !FluidBlockRenderer.func_239282_a_(reader, pos, blockStateIn, sideIn) && !FluidBlockRenderer.func_209557_a(reader, pos, sideIn, fluidStateIn);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean func_228796_a_(IBlockDisplayReader lightReaderIn, BlockPos posIn, IVertexBuilder vertexBuilderIn, FluidState fluidStateIn) {
        BlockState blockStateIn = fluidStateIn.func_206883_i();
        try {
            float f12;
            Object fluidAttributes;
            boolean flag;
            if (Config.isShaders()) {
                SVertexBuilder.pushEntity(blockStateIn, vertexBuilderIn);
            }
            TextureAtlasSprite[] atextureatlassprite = (flag = fluidStateIn.func_206884_a(FluidTags.field_206960_b)) ? this.field_178272_a : this.field_178271_b;
            BlockState blockstate = lightReaderIn.func_180495_p(posIn);
            if (Reflector.ForgeHooksClient_getFluidSprites.exists()) {
                TextureAtlasSprite[] forgeFluidSprites = (TextureAtlasSprite[])Reflector.call(Reflector.ForgeHooksClient_getFluidSprites, new Object[]{lightReaderIn, posIn, fluidStateIn});
                if (forgeFluidSprites != null) {
                    atextureatlassprite = forgeFluidSprites;
                }
            }
            RenderEnv renderEnv = vertexBuilderIn.getRenderEnv(blockStateIn, posIn);
            int i = -1;
            float alpha = 1.0f;
            if (Reflector.IForgeFluid_getAttributes.exists() && (fluidAttributes = Reflector.call(fluidStateIn.func_206886_c(), Reflector.IForgeFluid_getAttributes, new Object[0])) != null && Reflector.FluidAttributes_getColor.exists()) {
                i = Reflector.callInt(fluidAttributes, Reflector.FluidAttributes_getColor, new Object[]{lightReaderIn, posIn});
                alpha = (float)(i >> 24 & 0xFF) / 255.0f;
            }
            boolean flag1 = !FluidBlockRenderer.func_209557_a(lightReaderIn, posIn, Direction.UP, fluidStateIn);
            boolean flag2 = FluidBlockRenderer.func_239281_a_(lightReaderIn, posIn, fluidStateIn, blockstate, Direction.DOWN) && !FluidBlockRenderer.func_239283_a_(lightReaderIn, posIn, Direction.DOWN, 0.8888889f);
            boolean flag3 = FluidBlockRenderer.func_239281_a_(lightReaderIn, posIn, fluidStateIn, blockstate, Direction.NORTH);
            boolean flag4 = FluidBlockRenderer.func_239281_a_(lightReaderIn, posIn, fluidStateIn, blockstate, Direction.SOUTH);
            boolean flag5 = FluidBlockRenderer.func_239281_a_(lightReaderIn, posIn, fluidStateIn, blockstate, Direction.WEST);
            boolean flag6 = FluidBlockRenderer.func_239281_a_(lightReaderIn, posIn, fluidStateIn, blockstate, Direction.EAST);
            if (!(flag1 || flag2 || flag6 || flag5 || flag3 || flag4)) {
                boolean bl = false;
                return bl;
            }
            if (i < 0) {
                i = CustomColors.getFluidColor(lightReaderIn, blockStateIn, posIn, renderEnv);
            }
            float f = (float)(i >> 16 & 0xFF) / 255.0f;
            float f1 = (float)(i >> 8 & 0xFF) / 255.0f;
            float f2 = (float)(i & 0xFF) / 255.0f;
            boolean flag7 = false;
            float f3 = lightReaderIn.func_230487_a_(Direction.DOWN, true);
            float f4 = lightReaderIn.func_230487_a_(Direction.UP, true);
            float f5 = lightReaderIn.func_230487_a_(Direction.NORTH, true);
            float f6 = lightReaderIn.func_230487_a_(Direction.WEST, true);
            Fluid fluid = fluidStateIn.func_206886_c();
            BlockPos blockPos = posIn;
            IBlockDisplayReader iBlockDisplayReader = lightReaderIn;
            FluidBlockRenderer fluidBlockRenderer = this;
            float f7 = this.redirect$zba000$gc(fluidBlockRenderer, iBlockDisplayReader, blockPos, fluid);
            fluid = fluidStateIn.func_206886_c();
            blockPos = posIn.func_177968_d();
            iBlockDisplayReader = lightReaderIn;
            fluidBlockRenderer = this;
            float f8 = this.redirect$zba000$gc1(fluidBlockRenderer, iBlockDisplayReader, blockPos, fluid);
            fluid = fluidStateIn.func_206886_c();
            blockPos = posIn.func_177974_f().func_177968_d();
            iBlockDisplayReader = lightReaderIn;
            fluidBlockRenderer = this;
            float f9 = this.redirect$zba000$gc2(fluidBlockRenderer, iBlockDisplayReader, blockPos, fluid);
            fluid = fluidStateIn.func_206886_c();
            blockPos = posIn.func_177974_f();
            iBlockDisplayReader = lightReaderIn;
            fluidBlockRenderer = this;
            float f10 = this.redirect$zba000$gc3(fluidBlockRenderer, iBlockDisplayReader, blockPos, fluid);
            double d0 = posIn.func_177958_n() & 0xF;
            double d1 = posIn.func_177956_o() & 0xF;
            double d2 = posIn.func_177952_p() & 0xF;
            if (Config.isRenderRegions()) {
                int chunkX = posIn.func_177958_n() >> 4 << 4;
                int chunkY = posIn.func_177956_o() >> 4 << 4;
                int chunkZ = posIn.func_177952_p() >> 4 << 4;
                int bits = 8;
                int regionX = chunkX >> bits << bits;
                int regionZ = chunkZ >> bits << bits;
                int dx = chunkX - regionX;
                int dy = chunkY;
                int dz = chunkZ - regionZ;
                d0 += (double)dx;
                d1 += (double)dy;
                d2 += (double)dz;
            }
            float f11 = 0.001f;
            float f13 = f12 = flag2 ? 0.001f : 0.0f;
            if (flag1 && !FluidBlockRenderer.func_239283_a_(lightReaderIn, posIn, Direction.UP, Math.min(Math.min(f7, f8), Math.min(f9, f10)))) {
                float f20;
                float f16;
                float f19;
                float f15;
                float f18;
                float f14;
                float f17;
                float f132;
                flag7 = true;
                f7 -= 0.001f;
                f8 -= 0.001f;
                f9 -= 0.001f;
                f10 -= 0.001f;
                Vector3d vector3d = fluidStateIn.func_215673_c(lightReaderIn, posIn);
                if (vector3d.field_72450_a == 0.0 && vector3d.field_72449_c == 0.0) {
                    TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
                    vertexBuilderIn.setSprite(textureatlassprite1);
                    f132 = textureatlassprite1.func_94214_a(0.0);
                    f17 = textureatlassprite1.func_94207_b(0.0);
                    f14 = f132;
                    f18 = textureatlassprite1.func_94207_b(16.0);
                    f15 = textureatlassprite1.func_94214_a(16.0);
                    f19 = f18;
                    f16 = f15;
                    f20 = f17;
                } else {
                    TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
                    vertexBuilderIn.setSprite(textureatlassprite);
                    float f21 = (float)MathHelper.func_181159_b(vector3d.field_72449_c, vector3d.field_72450_a) - 1.5707964f;
                    float f22 = MathHelper.func_76126_a(f21) * 0.25f;
                    float f23 = MathHelper.func_76134_b(f21) * 0.25f;
                    float f24 = 8.0f;
                    f132 = textureatlassprite.func_94214_a(8.0f + (-f23 - f22) * 16.0f);
                    f17 = textureatlassprite.func_94207_b(8.0f + (-f23 + f22) * 16.0f);
                    f14 = textureatlassprite.func_94214_a(8.0f + (-f23 + f22) * 16.0f);
                    f18 = textureatlassprite.func_94207_b(8.0f + (f23 + f22) * 16.0f);
                    f15 = textureatlassprite.func_94214_a(8.0f + (f23 + f22) * 16.0f);
                    f19 = textureatlassprite.func_94207_b(8.0f + (f23 - f22) * 16.0f);
                    f16 = textureatlassprite.func_94214_a(8.0f + (f23 - f22) * 16.0f);
                    f20 = textureatlassprite.func_94207_b(8.0f + (-f23 - f22) * 16.0f);
                }
                float f43 = (f132 + f14 + f15 + f16) / 4.0f;
                float f44 = (f17 + f18 + f19 + f20) / 4.0f;
                float f45 = (float)atextureatlassprite[0].func_94211_a() / (atextureatlassprite[0].func_94212_f() - atextureatlassprite[0].func_94209_e());
                float f46 = (float)atextureatlassprite[0].func_94216_b() / (atextureatlassprite[0].func_94210_h() - atextureatlassprite[0].func_94206_g());
                float f47 = 4.0f / Math.max(f46, f45);
                f132 = MathHelper.func_219799_g(f47, f132, f43);
                f14 = MathHelper.func_219799_g(f47, f14, f43);
                f15 = MathHelper.func_219799_g(f47, f15, f43);
                f16 = MathHelper.func_219799_g(f47, f16, f43);
                f17 = MathHelper.func_219799_g(f47, f17, f44);
                f18 = MathHelper.func_219799_g(f47, f18, f44);
                f19 = MathHelper.func_219799_g(f47, f19, f44);
                f20 = MathHelper.func_219799_g(f47, f20, f44);
                int j = this.func_228795_a_(lightReaderIn, posIn);
                float f25 = f4 * f;
                float f26 = f4 * f1;
                float f27 = f4 * f2;
                this.vertexVanilla(vertexBuilderIn, d0 + 0.0, d1 + (double)f7, d2 + 0.0, f25, f26, f27, alpha, f132, f17, j);
                this.vertexVanilla(vertexBuilderIn, d0 + 0.0, d1 + (double)f8, d2 + 1.0, f25, f26, f27, alpha, f14, f18, j);
                this.vertexVanilla(vertexBuilderIn, d0 + 1.0, d1 + (double)f9, d2 + 1.0, f25, f26, f27, alpha, f15, f19, j);
                this.vertexVanilla(vertexBuilderIn, d0 + 1.0, d1 + (double)f10, d2 + 0.0, f25, f26, f27, alpha, f16, f20, j);
                if (fluidStateIn.func_205586_a(lightReaderIn, posIn.func_177984_a())) {
                    this.vertexVanilla(vertexBuilderIn, d0 + 0.0, d1 + (double)f7, d2 + 0.0, f25, f26, f27, alpha, f132, f17, j);
                    this.vertexVanilla(vertexBuilderIn, d0 + 1.0, d1 + (double)f10, d2 + 0.0, f25, f26, f27, alpha, f16, f20, j);
                    this.vertexVanilla(vertexBuilderIn, d0 + 1.0, d1 + (double)f9, d2 + 1.0, f25, f26, f27, alpha, f15, f19, j);
                    this.vertexVanilla(vertexBuilderIn, d0 + 0.0, d1 + (double)f8, d2 + 1.0, f25, f26, f27, alpha, f14, f18, j);
                }
            }
            if (flag2) {
                vertexBuilderIn.setSprite(atextureatlassprite[0]);
                float f34 = atextureatlassprite[0].func_94209_e();
                float f35 = atextureatlassprite[0].func_94212_f();
                float f37 = atextureatlassprite[0].func_94206_g();
                float f39 = atextureatlassprite[0].func_94210_h();
                int i1 = this.func_228795_a_(lightReaderIn, posIn.func_177977_b());
                float fbr = lightReaderIn.func_230487_a_(Direction.DOWN, true);
                float f40 = fbr * f;
                float f41 = fbr * f1;
                float f42 = fbr * f2;
                this.vertexVanilla(vertexBuilderIn, d0, d1 + (double)f12, d2 + 1.0, f40, f41, f42, alpha, f34, f39, i1);
                this.vertexVanilla(vertexBuilderIn, d0, d1 + (double)f12, d2, f40, f41, f42, alpha, f34, f37, i1);
                this.vertexVanilla(vertexBuilderIn, d0 + 1.0, d1 + (double)f12, d2, f40, f41, f42, alpha, f35, f37, i1);
                this.vertexVanilla(vertexBuilderIn, d0 + 1.0, d1 + (double)f12, d2 + 1.0, f40, f41, f42, alpha, f35, f39, i1);
                flag7 = true;
            }
            for (int l = 0; l < 4; ++l) {
                boolean notLava;
                boolean flag8;
                Direction direction;
                double d6;
                double d4;
                double d5;
                double d3;
                float f38;
                float f36;
                if (l == 0) {
                    f36 = f7;
                    f38 = f10;
                    d3 = d0;
                    d5 = d0 + 1.0;
                    d4 = d2 + (double)0.001f;
                    d6 = d2 + (double)0.001f;
                    direction = Direction.NORTH;
                    flag8 = flag3;
                } else if (l == 1) {
                    f36 = f9;
                    f38 = f8;
                    d3 = d0 + 1.0;
                    d5 = d0;
                    d4 = d2 + 1.0 - (double)0.001f;
                    d6 = d2 + 1.0 - (double)0.001f;
                    direction = Direction.SOUTH;
                    flag8 = flag4;
                } else if (l == 2) {
                    f36 = f8;
                    f38 = f7;
                    d3 = d0 + (double)0.001f;
                    d5 = d0 + (double)0.001f;
                    d4 = d2 + 1.0;
                    d6 = d2;
                    direction = Direction.WEST;
                    flag8 = flag5;
                } else {
                    f36 = f10;
                    f38 = f9;
                    d3 = d0 + 1.0 - (double)0.001f;
                    d5 = d0 + 1.0 - (double)0.001f;
                    d4 = d2;
                    d6 = d2 + 1.0;
                    direction = Direction.EAST;
                    flag8 = flag6;
                }
                if (!flag8 || FluidBlockRenderer.func_239283_a_(lightReaderIn, posIn, direction, Math.max(f36, f38))) continue;
                flag7 = true;
                BlockPos blockpos = posIn.func_177972_a(direction);
                TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
                float yMin1 = 0.0f;
                float yMin2 = 0.0f;
                boolean bl = notLava = !flag;
                if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists()) {
                    boolean bl2 = notLava = atextureatlassprite[2] != null;
                }
                if (notLava) {
                    BlockState blockState = lightReaderIn.func_180495_p(blockpos);
                    Block block = blockState.func_177230_c();
                    boolean forgeFluidOverlay = false;
                    if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists()) {
                        forgeFluidOverlay = Reflector.callBoolean(blockState, Reflector.IForgeBlockState_shouldDisplayFluidOverlay, new Object[]{lightReaderIn, blockpos, fluidStateIn});
                    }
                    if (forgeFluidOverlay || block instanceof BreakableBlock || block instanceof LeavesBlock || block == Blocks.field_150461_bJ) {
                        textureatlassprite2 = this.field_187501_d;
                    }
                    if (block == Blocks.field_150458_ak || block == Blocks.field_185774_da) {
                        yMin1 = 0.9375f;
                        yMin2 = 0.9375f;
                    }
                    if (block instanceof SlabBlock) {
                        SlabBlock blockSlab = (SlabBlock)((Object)block);
                        if (blockState.func_177229_b(SlabBlock.field_196505_a) == SlabType.BOTTOM) {
                            yMin1 = 0.5f;
                            yMin2 = 0.5f;
                        }
                    }
                }
                vertexBuilderIn.setSprite(textureatlassprite2);
                if (f36 <= yMin1 && f38 <= yMin2) continue;
                yMin1 = Math.min(yMin1, f36);
                yMin2 = Math.min(yMin2, f38);
                if (yMin1 > f11) {
                    yMin1 -= f11;
                }
                if (yMin2 > f11) {
                    yMin2 -= f11;
                }
                float vMin1 = textureatlassprite2.func_94207_b((1.0f - yMin1) * 16.0f * 0.5f);
                float vMin2 = textureatlassprite2.func_94207_b((1.0f - yMin2) * 16.0f * 0.5f);
                float f48 = textureatlassprite2.func_94214_a(0.0);
                float f49 = textureatlassprite2.func_94214_a(8.0);
                float f50 = textureatlassprite2.func_94207_b((1.0f - f36) * 16.0f * 0.5f);
                float f28 = textureatlassprite2.func_94207_b((1.0f - f38) * 16.0f * 0.5f);
                float f29 = textureatlassprite2.func_94207_b(8.0);
                int k = this.func_228795_a_(lightReaderIn, blockpos);
                float f30 = l < 2 ? lightReaderIn.func_230487_a_(Direction.NORTH, true) : lightReaderIn.func_230487_a_(Direction.WEST, true);
                float f31 = 1.0f * f30 * f;
                float f32 = 1.0f * f30 * f1;
                float f33 = 1.0f * f30 * f2;
                this.vertexVanilla(vertexBuilderIn, d3, d1 + (double)f36, d4, f31, f32, f33, alpha, f48, f50, k);
                this.vertexVanilla(vertexBuilderIn, d5, d1 + (double)f38, d6, f31, f32, f33, alpha, f49, f28, k);
                this.vertexVanilla(vertexBuilderIn, d5, d1 + (double)f12, d6, f31, f32, f33, alpha, f49, vMin2, k);
                this.vertexVanilla(vertexBuilderIn, d3, d1 + (double)f12, d4, f31, f32, f33, alpha, f48, vMin1, k);
                if (textureatlassprite2 == this.field_187501_d) continue;
                this.vertexVanilla(vertexBuilderIn, d3, d1 + (double)f12, d4, f31, f32, f33, alpha, f48, vMin1, k);
                this.vertexVanilla(vertexBuilderIn, d5, d1 + (double)f12, d6, f31, f32, f33, alpha, f49, vMin2, k);
                this.vertexVanilla(vertexBuilderIn, d5, d1 + (double)f38, d6, f31, f32, f33, alpha, f49, f28, k);
                this.vertexVanilla(vertexBuilderIn, d3, d1 + (double)f36, d4, f31, f32, f33, alpha, f48, f50, k);
            }
            vertexBuilderIn.setSprite(null);
            boolean bl = flag7;
            return bl;
        }
        finally {
            if (Config.isShaders()) {
                SVertexBuilder.popEntity(vertexBuilderIn);
            }
        }
    }

    private void func_228797_a_(IVertexBuilder vertexBuilderIn, double x, double y, double z, float red, float green, float blue, float u, float v, int packedLight) {
        vertexBuilderIn.func_225582_a_(x, y, z).func_227885_a_(red, green, blue, 1.0f).func_225583_a_(u, v).func_227886_a_(packedLight).func_225584_a_(0.0f, 1.0f, 0.0f).func_181675_d();
    }

    private void vertexVanilla(IVertexBuilder buffer, double x, double y, double z, float red, float green, float blue, float alpha, float u, float v, int combinedLight) {
        buffer.func_225582_a_(x, y, z).func_227885_a_(red, green, blue, alpha).func_225583_a_(u, v).func_227886_a_(combinedLight).func_225584_a_(0.0f, 1.0f, 0.0f).func_181675_d();
    }

    private int func_228795_a_(IBlockDisplayReader lightReaderIn, BlockPos posIn) {
        int i = WorldRenderer.func_228421_a_(lightReaderIn, posIn);
        int j = WorldRenderer.func_228421_a_(lightReaderIn, posIn.func_177984_a());
        int k = i & 0xFF;
        int l = j & 0xFF;
        int i1 = i >> 16 & 0xFF;
        int j1 = j >> 16 & 0xFF;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }

    private float func_217640_a(IBlockReader reader, BlockPos pos, Fluid fluidIn) {
        int i = 0;
        float f = 0.0f;
        for (int j = 0; j < 4; ++j) {
            BlockPos blockpos = pos.func_177982_a(-(j & 1), 0, -(j >> 1 & 1));
            if (reader.func_204610_c(blockpos.func_177984_a()).func_206886_c().func_207187_a(fluidIn)) {
                return 1.0f;
            }
            FluidState fluidstate = reader.func_204610_c(blockpos);
            if (fluidstate.func_206886_c().func_207187_a(fluidIn)) {
                float f1 = fluidstate.func_215679_a(reader, blockpos);
                if (f1 >= 0.8f) {
                    f += f1 * 10.0f;
                    i += 10;
                    continue;
                }
                f += f1;
                ++i;
                continue;
            }
            if (reader.func_180495_p(blockpos).func_185904_a().func_76220_a()) continue;
            ++i;
        }
        return f / (float)i;
    }

    @MixinMerged(mixin="net.skds.wpo.mixins.fluids.FluidBlockRendererMixin", priority=1000, sessionId="0f2c1fe9-16a3-4168-9d71-ea23fa9490ca")
    public float redirect$zba000$gc(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {
        float[] flex = FFluidStatic.getConH(w, p, f);
        this.customAH.put(p.func_218275_a(), flex);
        this.customAHSafe = flex;
        return flex[0];
    }

    @MixinMerged(mixin="net.skds.wpo.mixins.fluids.FluidBlockRendererMixin", priority=1000, sessionId="0f2c1fe9-16a3-4168-9d71-ea23fa9490ca")
    public float redirect$zba000$gc1(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {
        float[] ffmas = (float[])this.customAH.get(p.func_177978_c().func_218275_a());
        if (ffmas == null) {
            ffmas = this.customAHSafe;
        }
        float fll = ffmas[1];
        return fll;
    }

    @MixinMerged(mixin="net.skds.wpo.mixins.fluids.FluidBlockRendererMixin", priority=1000, sessionId="0f2c1fe9-16a3-4168-9d71-ea23fa9490ca")
    public float redirect$zba000$gc2(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {
        float[] ffmas = (float[])this.customAH.get(p.func_177978_c().func_177976_e().func_218275_a());
        if (ffmas == null) {
            ffmas = this.customAHSafe;
        }
        float fll = ffmas[2];
        return fll;
    }

    @MixinMerged(mixin="net.skds.wpo.mixins.fluids.FluidBlockRendererMixin", priority=1000, sessionId="0f2c1fe9-16a3-4168-9d71-ea23fa9490ca")
    public float redirect$zba000$gc3(FluidBlockRenderer fr, IBlockReader w, BlockPos p, Fluid f) {
        float[] ffmas = (float[])this.customAH.remove(p.func_177976_e().func_218275_a());
        if (ffmas == null) {
            ffmas = this.customAHSafe;
        }
        float fll = ffmas[3];
        return fll;
    }
}
