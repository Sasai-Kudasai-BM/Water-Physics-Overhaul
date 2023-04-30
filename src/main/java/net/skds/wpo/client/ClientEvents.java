package net.skds.wpo.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.skds.wpo.WPO;
import net.skds.wpo.client.models.WPOModelLayers;
import net.skds.wpo.client.models.PipePumpRenderer;
import net.skds.wpo.client.models.PipeRenderer;
import net.skds.wpo.client.models.PumpRenderer;
import net.skds.wpo.registry.Entities;
import net.skds.wpo.registry.FBlocks;

@Mod.EventBusSubscriber(modid = WPO.MOD_ID, value = Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

	public static void setup(final FMLClientSetupEvent event) {

		ItemBlockRenderTypes.setRenderLayer(FBlocks.PIPE.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(FBlocks.PIPE_PUMP.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(FBlocks.PUMP.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(FBlocks.GATE.get(), RenderType.translucent());
	}


	@SubscribeEvent
	public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(WPOModelLayers.PIPE, PipeRenderer::createLayer);
		event.registerLayerDefinition(WPOModelLayers.PIPE_PUMP, PipePumpRenderer::createLayer);
		event.registerLayerDefinition(WPOModelLayers.PUMP, PumpRenderer::createLayer);
	}

	@SubscribeEvent
	public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(Entities.PIPE.get(), PipeRenderer::new);
		event.registerBlockEntityRenderer(Entities.PIPE_PUMP.get(), PipePumpRenderer::new);
		event.registerBlockEntityRenderer(Entities.PUMP.get(), PumpRenderer::new);
	}


}