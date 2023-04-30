package net.skds.wpo.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.skds.wpo.WPO;
import net.skds.wpo.block.entity.PumpBlockEntity;

@OnlyIn(Dist.CLIENT)
public class PumpRenderer implements BlockEntityRenderer<PumpBlockEntity> {

	private final ModelPart zasos;
	private final ModelPart terebilka;

	private static final ResourceLocation TEXTURE = new ResourceLocation(WPO.MOD_ID, "textures/block/pump_te.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE);

	public PumpRenderer(BlockEntityRendererProvider.Context context) {
		// rendererDispatcherIn.textureManager.bindTexture(TEXTURE);

		ModelPart modelpart = context.bakeLayer(WPOModelLayers.PUMP);
		this.zasos = modelpart.getChild("zasos");
		this.terebilka = modelpart.getChild("terebilka");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition root = meshdefinition.getRoot();
		PartDefinition zasosPart = root.addOrReplaceChild("zasos", CubeListBuilder.create()
						.texOffs(1, 1).addBox(-13.0F, -13.0F, 1.0F, 10.0F, 10.0F, 15.0F)
						.texOffs(0, 46).addBox(-1.0F, -11.0F, 2.0F, 1.0F, 6.0F, 12.0F)
						.texOffs(0, 46).addBox(-16.0F, -11.0F, 2.0F, 1.0F, 6.0F, 12.0F)
				, PartPose.offset(8.0F, 8.0F, -8.0F));
		zasosPart.addOrReplaceChild("cube_r1", CubeListBuilder.create()
						.texOffs(0, 46).addBox(-8.0F, -3.0F, -6.0F, 1.0F, 6.0F, 12.0F)
						.texOffs(0, 46).addBox(7.0F, -3.0F, -6.0F, 1.0F, 6.0F, 12.0F)
				, PartPose.offsetAndRotation(-8.0F, -8.0F, 8.0F, 0.0F, 0.0F, 1.5708F));
		root.addOrReplaceChild("terebilka", CubeListBuilder.create()
						.texOffs(0, 26).addBox(-15.0F, -15.0F, 12.0F, 14.0F, 14.0F, 2.0F)
				, PartPose.offset(8.0F, 8.0F, -8.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void render(PumpBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
					   MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// RenderMaterial m = new RenderMaterial(ATLAS, TEXTURE);
		// IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getCutout());
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(RENDER_TYPE);
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5, 0.5, 0.5);

		Direction dir = tileEntityIn.facing;
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

		zasos.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn);

		float partial = partialTicks;
		if (tileEntityIn.powered) {

		} else {
			if (tileEntityIn.anim >= 0) {
				partial = 1 - partial;
			} else {
				partial = 0;
			}
		}

		float anim = tileEntityIn.anim + partial;
		if (tileEntityIn.anim < 0) {
			anim ++;
		}

		float phase = (float) Math.PI * anim / (tileEntityIn.animSpeed + 1);

		//System.out.println(anim);
		//System.out.println(Math.abs(Math.sin(phase)));
		
		float offset =  - (float) Math.abs(Math.cos(phase)) * 0.625F;
		//float offset = -0.4F;

		matrixStackIn.translate(0, 0, offset);

		terebilka.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn);

		matrixStackIn.popPose();
	}
}