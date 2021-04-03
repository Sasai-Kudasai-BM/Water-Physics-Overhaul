package net.skds.wpo.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

	public static AdvancedBucket getBucketForReg(Fluid fluid) {
		Properties prop = new Properties().maxStackSize(fluid == Fluids.EMPTY ? 16 : 1)
				.defaultMaxDamage(WPOConfig.MAX_FLUID_LEVEL).setNoRepair().setISTER(() -> ISTER.call());
		return new AdvancedBucket(fluid, prop);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		ExtendedFHIS fh = new ExtendedFHIS(stack, 1000);
		FluidStack fst = fh.getFluid();
		Fluid f = fst.getFluid();
		//Block b = f.getDefaultState().getBlockState().getBlock();
		TextFormatting form = TextFormatting.DARK_PURPLE;
		//ITextComponent texComp = new TranslationTextComponent(b.getTranslationKey()).mergeStyle(form);
		ITextComponent texComp = new TranslationTextComponent(f.getAttributes().getTranslationKey()).mergeStyle(form);
		tooltip.add(texComp);
		texComp = new StringTextComponent(fst.getAmount() + " mb");
		tooltip.add(texComp);		
	}

	@Override
	public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack,
			@Nullable net.minecraft.nbt.CompoundNBT nbt) {
		fhis = new ExtendedFHIS(stack, 1000);
		return fhis;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return fhis.getCapability(cap);
	}

	public static void updateDamage(ItemStack stack) {
		ExtendedFHIS fst = new ExtendedFHIS(stack, 1000);
		int sl = fst.getFluid().getAmount() / FFluidStatic.FCONST;
		stack.setDamage(WPOConfig.MAX_FLUID_LEVEL - sl);
	}

}