package net.skds.wpo.data;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidStateContainer {

	public static final FluidStateContainer EMPTY = new FluidStateContainer(null);
	public static final FluidStateContainer WATER_DEBUG = new FluidStateContainer(Fluids.WATER.getDefaultState());
	
	private static ObjectIntIdentityMap<FluidStateContainer> identityMap = new ObjectIntIdentityMap<>();

	public final FluidState state;

	public FluidStateContainer(FluidState state) {
		this.state = state;
	}

	public static ObjectIntIdentityMap<FluidStateContainer> getIdentityMap() {
		return identityMap;
	}

	public static void setIdentityMap() {
		ObjectIntIdentityMap<FluidStateContainer> map = new ObjectIntIdentityMap<>();
		map.add(EMPTY);
		ForgeRegistries.FLUIDS.forEach(f -> {
			f.getStateContainer().getValidStates().forEach(fs -> {
				map.add(new FluidStateContainer(fs));
			});
		});
		identityMap = map;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof FluidStateContainer) {
			return state == ((FluidStateContainer) obj).state;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return state.hashCode();
	}

	@Override
	public String toString() {
		return "Container of " + state.toString();
	}
}
