package net.skds.wpo.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.block.entity.FluidGateBlockEntity;
import net.skds.wpo.block.entity.PipePumpBlockEntity;
import net.skds.wpo.block.entity.PipeBlockEntity;
import net.skds.wpo.block.entity.PumpBlockEntity;

import static net.skds.wpo.WPO.MOD_ID;

public class Entities {
	
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);
    
	public static final RegistryObject<BlockEntityType<PipePumpBlockEntity>> PIPE_PUMP = BLOCK_ENTITIES.register("pipe_pump", () -> BlockEntityType.Builder.of(PipePumpBlockEntity::new, FBlocks.PIPE_PUMP.get()).build(null));
	public static final RegistryObject<BlockEntityType<PumpBlockEntity>> PUMP = BLOCK_ENTITIES.register("pump", () -> BlockEntityType.Builder.of(PumpBlockEntity::new, FBlocks.PUMP.get()).build(null));
	public static final RegistryObject<BlockEntityType<FluidGateBlockEntity>> GATE = BLOCK_ENTITIES.register("gate", () -> BlockEntityType.Builder.of(FluidGateBlockEntity::new, FBlocks.GATE.get()).build(null));
	public static final RegistryObject<BlockEntityType<PipeBlockEntity>> PIPE = BLOCK_ENTITIES.register("pipe", () -> BlockEntityType.Builder.of(PipeBlockEntity::new, FBlocks.PIPE.get()).build(null));
	
	public static void register() {
		IEventBus eb = FMLJavaModLoadingContext.get().getModEventBus();
		ENTITIES.register(eb);
		BLOCK_ENTITIES.register(eb);
	}
}