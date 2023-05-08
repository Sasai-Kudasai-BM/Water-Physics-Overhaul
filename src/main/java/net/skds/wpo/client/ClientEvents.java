package net.skds.wpo.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.skds.wpo.WPO;

@Mod.EventBusSubscriber(modid = WPO.MOD_ID, value = Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

	public static void setup(final FMLClientSetupEvent event) {
	}
}