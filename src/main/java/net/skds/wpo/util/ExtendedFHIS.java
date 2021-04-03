package net.skds.wpo.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.skds.wpo.item.AdvancedBucket;

public class ExtendedFHIS extends FluidHandlerItemStack {

	public ExtendedFHIS(ItemStack container, int capacity) {
		super(container, capacity);
	}

    protected void setFluid(FluidStack fluid) {
		super.setFluid(fluid);
		AdvancedBucket.updateDamage(container);
	}	
}