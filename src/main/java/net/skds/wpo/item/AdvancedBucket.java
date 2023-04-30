package net.skds.wpo.item;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.skds.wpo.WPOConfig;
import net.skds.wpo.client.models.ISTER;
import net.skds.wpo.fluidphysics.FFluidStatic;
import net.skds.wpo.util.ExtendedFHIS;

public class AdvancedBucket extends BucketItem implements ICapabilityProvider {

	public AdvancedBucket(Fluid fluid, Properties builder) {
		super(() -> fluid, builder);
	}

	private ExtendedFHIS fhis;

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

	public static AdvancedBucket getBucketForReg(Fluid fluid) {
		Properties prop = new Properties().stacksTo(fluid == Fluids.EMPTY ? 16 : 1)
				.defaultDurability(WPOConfig.MAX_FLUID_LEVEL).setNoRepair();
		return new AdvancedBucket(fluid, prop);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip,
								TooltipFlag flagIn) {
		ExtendedFHIS fh = new ExtendedFHIS(stack, 1000);
		FluidStack fst = fh.getFluid();
		Fluid f = fst.getFluid();
		//Block b = f.getDefaultState().getBlockState().getBlock();
		ChatFormatting form = ChatFormatting.DARK_PURPLE;
		//ITextComponent texComp = new TranslationTextComponent(b.getTranslationKey()).mergeStyle(form);
		Component texComp = new TranslatableComponent(f.getAttributes().getTranslationKey()).withStyle(form);
		tooltip.add(texComp);
		texComp = new TextComponent(fst.getAmount() + " mb");
		tooltip.add(texComp);		
	}

	@Override
	public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack,
			@Nullable net.minecraft.nbt.CompoundTag nbt) {
		fhis = new ExtendedFHIS(stack, 1000);
		return fhis;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return fhis.getCapability(cap);
	}

	public static void updateDamage(ItemStack stack) {
		ExtendedFHIS fst = new ExtendedFHIS(stack, 1000);
		int sl = fst.getFluid().getAmount() / FFluidStatic.FCONST;
		stack.setDamageValue(WPOConfig.MAX_FLUID_LEVEL - sl);
	}
}