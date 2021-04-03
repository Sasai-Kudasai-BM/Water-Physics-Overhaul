package net.skds.wpo.registry;

import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.tileentity.FluidGateTileEntity;
import net.skds.wpo.tileentity.PipePumpTileEntity;
import net.skds.wpo.tileentity.PipeTileEntity;
import net.skds.wpo.tileentity.PumpTileEntity;

import static net.skds.wpo.WPO.MOD_ID;

public class Entities {
	
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);
    
	public static final RegistryObject<TileEntityType<PipePumpTileEntity>> PIPE_PUMP = TILE_ENTITIES.register("pipe_pump", () -> TileEntityType.Builder.create(PipePumpTileEntity::new, FBlocks.PIPE_PUMP.get()).build(null));
	public static final RegistryObject<TileEntityType<PumpTileEntity>> PUMP = TILE_ENTITIES.register("pump", () -> TileEntityType.Builder.create(PumpTileEntity::new, FBlocks.PUMP.get()).build(null));
	public static final RegistryObject<TileEntityType<FluidGateTileEntity>> GATE = TILE_ENTITIES.register("gate", () -> TileEntityType.Builder.create(FluidGateTileEntity::new, FBlocks.GATE.get()).build(null));
	public static final RegistryObject<TileEntityType<PipeTileEntity>> PIPE = TILE_ENTITIES.register("pipe", () -> TileEntityType.Builder.create(PipeTileEntity::new, FBlocks.PIPE.get()).build(null));
	
	public static void register() {
		IEventBus eb = FMLJavaModLoadingContext.get().getModEventBus();
		ENTITIES.register(eb);
		TILE_ENTITIES.register(eb);
	}
}