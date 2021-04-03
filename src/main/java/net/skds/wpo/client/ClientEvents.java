package net.skds.wpo.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.skds.wpo.client.models.PipePumpRenderer;
import net.skds.wpo.client.models.PipeRenderer;
import net.skds.wpo.client.models.PumpRenderer;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.registry.FBlocks;
import net.skds.wpo.tileentity.PipePumpTileEntity;
import net.skds.wpo.tileentity.PipeTileEntity;
import net.skds.wpo.tileentity.PumpTileEntity;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

	public static void setup(final FMLClientSetupEvent event) {

		RenderTypeLookup.setRenderLayer(FBlocks.PIPE.get(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(FBlocks.PIPE_PUMP.get(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(FBlocks.PUMP.get(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(FBlocks.GATE.get(), RenderType.getTranslucent());

		ClientRegistry.bindTileEntityRenderer((TileEntityType<PipePumpTileEntity>) Entities.PIPE_PUMP.get(), PipePumpRenderer::new);
		ClientRegistry.bindTileEntityRenderer((TileEntityType<PumpTileEntity>) Entities.PUMP.get(), PumpRenderer::new);
		ClientRegistry.bindTileEntityRenderer((TileEntityType<PipeTileEntity>) Entities.PIPE.get(), PipeRenderer::new);
	}
}