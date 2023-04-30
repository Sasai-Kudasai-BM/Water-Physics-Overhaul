package net.skds.wpo.client.models;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.skds.wpo.block.PipeBlock;
import net.skds.wpo.block.PumpBlock;
import net.skds.wpo.item.AdvancedBucket;
import net.skds.wpo.registry.FBlocks;
import net.skds.wpo.block.entity.PipePumpBlockEntity;
import net.skds.wpo.block.entity.PipeBlockEntity;
import net.skds.wpo.block.entity.PumpBlockEntity;

@OnlyIn(Dist.CLIENT)
public class ISTER extends BlockEntityWithoutLevelRenderer {
	private final PipeBlockEntity pipeBlockEntity = new PipeBlockEntity(BlockPos.ZERO, FBlocks.PIPE.get().defaultBlockState());
	private final PumpBlockEntity pumpBlockEntity = new PumpBlockEntity(BlockPos.ZERO, FBlocks.PUMP.get().defaultBlockState());
	private final PipePumpBlockEntity pipePumpBlockEntity = new PipePumpBlockEntity(BlockPos.ZERO, FBlocks.PIPE_PUMP.get().defaultBlockState());

	private final ItemRenderer itemRenderer;
	private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

	private static ISTER instance = null;

	private ISTER() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

		this.itemRenderer = Minecraft.getInstance().getItemRenderer();
		blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
		// why do we need these custom block states?
		this.pipeBlockEntity.boolConnections = new boolean[] { false, false, true, true, false, false };
		this.pipePumpBlockEntity.facing = Direction.NORTH;
		this.pipePumpBlockEntity.anim = -1;
	}

	public static ISTER getInstance(){
		if (instance == null) {
			instance = new ISTER();
		}
		return instance;
	}

	@Override
	public void renderByItem(ItemStack stack, TransformType p_239207_2_, PoseStack matrixStack,
							 MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		BlockEntity blockEntity;

		//System.out.println(stack);

		Item item = stack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block instanceof PipeBlock) {
				blockEntity = pipeBlockEntity;
			} else if (block instanceof PumpBlock) {
				blockEntity = pumpBlockEntity;
			} else {
				blockEntity = pipePumpBlockEntity;
			}
			this.blockEntityRenderDispatcher.renderItem(blockEntity, matrixStack, buffer, combinedLight, combinedOverlay);
		} else {
			if (item instanceof AdvancedBucket) {
				matrixStack.translate(0.5, 0.5, 0.5);
				Optional<IFluidHandlerItem> op = stack
						.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).resolve();
				ItemStack stack2;
				if (op.isPresent()) {
					IFluidHandlerItem handler = op.get();
					Fluid f = handler.getFluidInTank(0).getFluid();
					if (f != Fluids.EMPTY) {
						stack2 = new ItemStack(f.getBucket(), 1);
					} else {
						stack2 = new ItemStack(Items.BUCKET, 1);
					}
				} else {
					stack2 = new ItemStack(Items.BUCKET, 1);
				}
				//System.out.println(stack);
				// TODO replaced null with 0 as last parameter. If error, check if right
				BakedModel ibakedmodel = this.itemRenderer.getModel(stack2, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);
				itemRenderer.render(stack2, TransformType.NONE, false, matrixStack, buffer, combinedLight,
						OverlayTexture.NO_OVERLAY, ibakedmodel);
			}
		}
	}
}