package net.skds.wpo.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.WPO;
import net.skds.wpo.item.AdvancedBucket;

public class Items {
	
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WPO.MOD_ID);
    
	public static final RegistryObject<Item> ADVANCED_BUCKET = ITEMS.register("advanced_bucket", () -> AdvancedBucket.getBucketForReg(Fluids.EMPTY));

	public static void register() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}