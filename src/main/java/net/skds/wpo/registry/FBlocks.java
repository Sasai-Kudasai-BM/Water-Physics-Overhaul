package net.skds.wpo.registry;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.WPO;
import net.skds.wpo.block.FluidGateBlock;
import net.skds.wpo.block.PipePumpBlock;
import net.skds.wpo.block.PumpBlock;
import net.skds.wpo.block.PipeBlock;

public class FBlocks {
	
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WPO.MOD_ID);
	
	public static final RegistryObject<Block> GATE = BLOCKS.register("gate", () -> FluidGateBlock.getForReg());
	public static final RegistryObject<Block> PIPE_PUMP = BLOCKS.register("pipe_pump", () -> PipePumpBlock.getForReg());
	public static final RegistryObject<Block> PUMP = BLOCKS.register("pump", () -> PumpBlock.getForReg());
	public static final RegistryObject<Block> PIPE = BLOCKS.register("pipe", () -> PipeBlock.getForReg());
		
	public static void register() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}