package net.skds.wpo.client.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.skds.wpo.WPO;
import net.skds.wpo.tileentity.PipeTileEntity;

@OnlyIn(Dist.CLIENT)
public class PipeRenderer extends TileEntityRenderer<PipeTileEntity> {

	private final ModelRenderer junk;
	private final ModelRenderer frong;
	private final ModelRenderer frong2;

	private static final ResourceLocation TEXTURE = new ResourceLocation(WPO.MOD_ID, "textures/block/pipe.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE);

	public PipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// rendererDispatcherIn.textureManager.bindTexture(TEXTURE);

		junk = new ModelRenderer(64, 64, 0, 0);
		junk.setPos(0.0F, 0.0F, 0.0F);
		junk.texOffs(0, 14).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, 0.0F, false);

		frong = new ModelRenderer(64, 64, 0, 0);
		frong.setPos(0.0F, 0.0F, 0.0F);
		frong.texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		frong.texOffs(28, 0).addBox(-5.0F, -5.0F, -8.0F, 10.0F, 10.0F, 2.0F, 0.0F, false);

		frong2 = new ModelRenderer(64, 64, 0, 0);
		frong2.setPos(0.0F, 0.0F, 0.0F);
		frong2.texOffs(1, 45).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 11.0F, 0.0F, false);
		frong2.texOffs(28, 0).addBox(-5.0F, -5.0F, -8.0F, 10.0F, 10.0F, 2.0F, 0.0F, false);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}

	@Override
	public void render(PipeTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// RenderMaterial m = new RenderMaterial(ATLAS, TEXTURE);
		// IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getCutout());

		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RENDER_TYPE);
		boolean[] bl = tileEntityIn.boolConnections;

		matrixStackIn.translate(0.5, 0.5, 0.5);

		int i = 0;
		for (boolean b : bl) {
			if (b) {
				i++;
			}
		}

		int c = 0;
		for (boolean b : bl) {
			if (b) {
				matrixStackIn.pushPose();
				Direction dir = Direction.from3DDataValue(c);
				switch (dir) {
					case UP:
						matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90));
						break;
					case DOWN:
						matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90));
						break;
					case EAST:
						matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
						break;
					case NORTH:
						break;
					case SOUTH:
						matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180));
						break;
					case WEST:
						matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
						break;
					default:
						break;

				}
				if (i == 1) {
					frong2.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn);
				} else {
					frong.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn);
				}
				matrixStackIn.popPose();
				// i++;
			}
			c++;
		}

		boolean l1 = bl[0] && bl[1];
		boolean l2 = bl[2] && bl[3];
		boolean l3 = bl[4] && bl[5];
		boolean renderJunk = !(i == 2 && (l1 || l2 || l3)) && (i > 1 || i == 0);
		if (renderJunk) {
			junk.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn);
		}

		boolean debug = false;
		if (debug) {
			// =============================

			matrixStackIn.pushPose();
			FontRenderer fontrenderer = this.renderer.getFont();
			matrixStackIn.mulPose(this.renderer.camera.rotation());
			matrixStackIn.translate(0.65, 0, -1.25);
			matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
			String result = String.format("%.3f", tileEntityIn.pressure);
			fontrenderer.draw(matrixStackIn, result, 0, 0, 16776960);
			matrixStackIn.popPose();
			// =============================
			float s = (float) tileEntityIn.getFluidInTank(0).getAmount() / 500;
			if (s > 0) {
				matrixStackIn.pushPose();
				matrixStackIn.scale(1, s, 1);
				matrixStackIn.translate(-0.5, -0.5, -0.5);

				BlockPos pos = tileEntityIn.getBlockPos();
				AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos).move(-pos.getX(), -pos.getY(), -pos.getZ());
				WorldRenderer.renderLineBox(matrixStackIn, bufferIn.getBuffer(RenderType.lines()), axisalignedbb,
						0.0F, 0.0F, 1.0F, 1.0F);

				matrixStackIn.popPose();
			}
			// =============================
		}
	}
}