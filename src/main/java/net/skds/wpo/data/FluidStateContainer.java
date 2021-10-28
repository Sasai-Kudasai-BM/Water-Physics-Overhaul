package net.skds.wpo.data;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class FluidStateContainer {

	public static final FluidState EMPTY = new FluidState(null, null, null);
	
	private static ObjectIntIdentityMap<FluidState> identityMap = new ObjectIntIdentityMap<>();

	public final FluidState state;

	private FluidStateContainer(FluidState state) {
		this.state = state;
	}

	public static ObjectIntIdentityMap<FluidState> getIdentityMap() {
		return identityMap;
	}

	public static void setIdentityMap() {
		ObjectIntIdentityMap<FluidState> map = new ObjectIntIdentityMap<>();
		map.add(EMPTY);
		ForgeRegistries.FLUIDS.forEach(f -> {
			f.getStateContainer().getValidStates().forEach(fs -> {
				map.add(fs);
			});
		});
		identityMap = map;
	}

}
