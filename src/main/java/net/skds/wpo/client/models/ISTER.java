package net.skds.wpo.client.models;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.skds.wpo.item.AdvancedBucket;
import net.skds.wpo.registry.FBlocks;
import net.skds.wpo.tileentity.PipePumpTileEntity;
import net.skds.wpo.tileentity.PipeTileEntity;
import net.skds.wpo.tileentity.PumpTileEntity;

@OnlyIn(Dist.CLIENT)
public class ISTER extends ItemStackTileEntityRenderer {

	private PipeTileEntity pipeTileEntity;
	private PumpTileEntity pumpTileEntity;
	private PipePumpTileEntity pipePumpTileEntity;

	private ItemRenderer itemRenderer;
	private Minecraft mc;

	private boolean firstCall = true;

	private static ISTER instance = null;

	public ISTER() {
		this.mc = Minecraft.getInstance();
	}

	public static Callable<ItemStackTileEntityRenderer> callable = new Callable<ItemStackTileEntityRenderer>(){
	
		@Override
		public ISTER call() throws Exception {
			if (instance == null) {
				instance = new ISTER();
			}
			return instance;
		}
	};

	public static Callable<ItemStackTileEntityRenderer> call() {
		return callable;
	}

	@Override
	public void func_239207_a_(ItemStack stack, TransformType p_239207_2_, MatrixStack matrixStack,
			IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		TileEntity tileentity;

		if (firstCall) {
			firstCall();
		}

		//System.out.println(stack);

		Item item = stack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block == FBlocks.PIPE.get()) {
				tileentity = pipeTileEntity;
			} else if (block == FBlocks.PUMP.get()) {
				tileentity = pumpTileEntity;
			} else {

				tileentity = pipePumpTileEntity;
			}
			TileEntityRendererDispatcher.instance.renderItem(tileentity, matrixStack, buffer, combinedLight,
					combinedOverlay);
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
						stack2 = new ItemStack(f.getFilledBucket(), 1);						
					} else {
						stack2 = new ItemStack(Items.BUCKET, 1);
					}
				} else {
					stack2 = new ItemStack(Items.BUCKET, 1);
				}
				//System.out.println(stack);
				IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(stack2, mc.world, null);
				itemRenderer.renderItem(stack2, TransformType.NONE, false, matrixStack, buffer, combinedLight,
						OverlayTexture.NO_OVERLAY, ibakedmodel);
			}
		}
	}

	private void firstCall() {

		firstCall = false;
		pipePumpTileEntity = new PipePumpTileEntity();
		pipePumpTileEntity.facing = Direction.NORTH;
		pipePumpTileEntity.anim = -1;
		pipeTileEntity = new PipeTileEntity();
		pipeTileEntity.boolConnections = new boolean[] { false, false, true, true, false, false };
		pumpTileEntity = new PumpTileEntity();

		this.itemRenderer = mc.getItemRenderer();
	}

}