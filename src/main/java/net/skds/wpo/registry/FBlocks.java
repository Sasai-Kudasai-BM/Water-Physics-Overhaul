package net.skds.wpo.registry;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.WPO;

public class FBlocks {
	
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WPO.MOD_ID);
	
	public static void register() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}