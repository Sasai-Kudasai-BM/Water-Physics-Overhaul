package net.skds.wpo.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.skds.wpo.WPO;
import net.skds.wpo.block.entity.PipeBlockEntity;

@OnlyIn(Dist.CLIENT)
public class PipeRenderer implements BlockEntityRenderer<PipeBlockEntity> {

	private final ModelPart unconnected;
	private final ModelPart singleConnected;
	private final ModelPart doubleConnected;

	private static final ResourceLocation TEXTURE = new ResourceLocation(WPO.MOD_ID, "textures/block/pipe.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE);
	private final BlockEntityRenderDispatcher renderDispatcher;

	public PipeRenderer(BlockEntityRendererProvider.Context context) {
		// rendererDispatcherIn.textureManager.bindTexture(TEXTURE);
		this.renderDispatcher = context.getBlockEntityRenderDispatcher();

		ModelPart modelpart = context.bakeLayer(WPOModelLayers.PIPE);
		this.unconnected = modelpart.getChild("pipe_0_con");
		this.singleConnected = modelpart.getChild("pipe_1_con");
		this.doubleConnected = modelpart.getChild("pipe_2_con");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("pipe_0_con", CubeListBuilder.create()
						.texOffs(0, 14).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F)
				, PartPose.ZERO);
		partdefinition.addOrReplaceChild("pipe_1_con", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
						.texOffs(28, 0).addBox(-5.0F, -5.0F, -8.0F, 10.0F, 10.0F, 2.0F)
				, PartPose.ZERO);
		partdefinition.addOrReplaceChild("pipe_2_con", CubeListBuilder.create()
						.texOffs(1, 45).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 11.0F)
						.texOffs(28, 0).addBox(-5.0F, -5.0F, -8.0F, 10.0F, 10.0F, 2.0F)
				, PartPose.ZERO);
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void render(PipeBlockEntity blockEntityIn, float partialTicks, PoseStack matrixStackIn,
					   MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// RenderMaterial m = new RenderMaterial(ATLAS, TEXTURE);
		// IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getCutout());

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RENDER_TYPE);
		boolean[] bl = blockEntityIn.boolConnections;

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
					singleConnected.render(matrixStackIn, vertexConsumer, combinedLightIn, combinedOverlayIn);
				} else {
					doubleConnected.render(matrixStackIn, vertexConsumer, combinedLightIn, combinedOverlayIn);
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
			unconnected.render(matrixStackIn, vertexConsumer, combinedLightIn, combinedOverlayIn);
		}

		boolean debug = false;
		if (debug) {
			// =============================

			matrixStackIn.pushPose();
			Font fontrenderer = this.renderDispatcher.font;
			matrixStackIn.mulPose(this.renderDispatcher.camera.rotation());
			matrixStackIn.translate(0.65, 0, -1.25);
			matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
			String result = String.format("%.3f", blockEntityIn.pressure);
			fontrenderer.draw(matrixStackIn, result, 0, 0, 16776960);
			matrixStackIn.popPose();
			// =============================
			float s = (float) blockEntityIn.getFluidInTank(0).getAmount() / 500;
			if (s > 0) {
				matrixStackIn.pushPose();
				matrixStackIn.scale(1, s, 1);
				matrixStackIn.translate(-0.5, -0.5, -0.5);

				BlockPos pos = blockEntityIn.getBlockPos();
				AABB axisalignedbb = new AABB(pos).move(-pos.getX(), -pos.getY(), -pos.getZ());
				LevelRenderer.renderLineBox(matrixStackIn, bufferIn.getBuffer(RenderType.lines()), axisalignedbb,
						0.0F, 0.0F, 1.0F, 1.0F);

				matrixStackIn.popPose();
			}
			// =============================
		}
	}
}