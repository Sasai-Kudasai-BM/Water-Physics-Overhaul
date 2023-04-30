package net.skds.wpo.registry;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.WPO;
import net.skds.wpo.client.models.ISTER;
import net.skds.wpo.item.AdvancedBucket;

import java.util.function.Consumer;

public class Items {
	
    public static final CreativeModeTab CTAB = (new CreativeModeTab(CreativeModeTab.getGroupCountSafe(), "WPO") {
    
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ADVANCED_BUCKET.get());
        }
    }).setRecipeFolderName("wpo");


    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WPO.MOD_ID);
    
	public static final RegistryObject<Item> ADVANCED_BUCKET = ITEMS.register("advanced_bucket", () -> AdvancedBucket.getBucketForReg(Fluids.EMPTY));

	public static final RegistryObject<BlockItem> GATE = registerBlockItem(FBlocks.GATE, new Properties().tab(CTAB));
//	public static final RegistryObject<BlockItem> PIPE_PUMP = registerBlockItemWithRenderer(FBlocks.PIPE_PUMP, new Properties().tab(CTAB));
	public static final RegistryObject<BlockItem> PIPE_PUMP = registerBlockItem(FBlocks.PIPE_PUMP, new Properties().tab(CTAB));
	public static final RegistryObject<BlockItem> PUMP = registerBlockItem(FBlocks.PUMP, new Properties().tab(CTAB));
//	public static final RegistryObject<BlockItem> PUMP = registerBlockItemWithRenderer(FBlocks.PUMP, new Properties().tab(CTAB));
//	public static final RegistryObject<BlockItem> PIPE = registerBlockItemWithRenderer(FBlocks.PIPE, new Properties().tab(CTAB));
	public static final RegistryObject<BlockItem> PIPE = registerBlockItem(FBlocks.PIPE, new Properties().tab(CTAB));

    private static RegistryObject<BlockItem> registerBlockItem(RegistryObject<Block> ro, Properties prop) {
        return ITEMS.register(ro.getId().getPath(), () -> new BlockItem(ro.get(), prop));
    }

    private static RegistryObject<BlockItem> registerBlockItemWithRenderer(RegistryObject<Block> ro, Properties prop){
        BlockItem blockItem = new BlockItem(ro.get(), prop){

            @Override
            public void initializeClient(Consumer<IItemRenderProperties> consumer) {
                // needed to use custom BlockEntityWithoutLevelRenderer for this item
                consumer.accept(new IItemRenderProperties() {

                    @Override
                    public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                        return ISTER.getInstance();
                    }
                });
            }
        };
        return ITEMS.register(ro.getId().getPath(), () -> blockItem);
}

	public static void register() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}