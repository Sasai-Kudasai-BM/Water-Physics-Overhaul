package net.skds.wpo.registry;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Properties;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.WPO;
import net.skds.wpo.client.models.ISTER;
import net.skds.wpo.item.AdvancedBucket;

public class Items {
	
    public static final ItemGroup CTAB = (new ItemGroup(ItemGroup.getGroupCountSafe(), "WPO") {
    
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ADVANCED_BUCKET.get());
        }
    }).setTabPath("wpo");


    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WPO.MOD_ID);
    
	public static final RegistryObject<Item> ADVANCED_BUCKET = ITEMS.register("advanced_bucket", () -> AdvancedBucket.getBucketForReg(Fluids.EMPTY));

	public static final RegistryObject<BlockItem> GATE = registerBlockItem(FBlocks.GATE, new Properties().group(CTAB));
	public static final RegistryObject<BlockItem> PIPE_PUMP = registerBlockItem(FBlocks.PIPE_PUMP, new Properties().group(CTAB).setISTER(() -> ISTER.call()));
	public static final RegistryObject<BlockItem> PUMP = registerBlockItem(FBlocks.PUMP, new Properties().group(CTAB).setISTER(() -> ISTER.call()));
	public static final RegistryObject<BlockItem> PIPE = registerBlockItem(FBlocks.PIPE, new Properties().group(CTAB).setISTER(() -> ISTER.call()));
	
    private static RegistryObject<BlockItem> registerBlockItem(RegistryObject<Block> ro, Properties prop) {
        return ITEMS.register(ro.getId().getPath(), () -> new BlockItem(ro.get(), prop));
    }

	public static void register() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}