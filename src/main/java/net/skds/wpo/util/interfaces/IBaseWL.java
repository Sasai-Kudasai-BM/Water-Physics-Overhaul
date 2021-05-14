package net.skds.wpo.util.interfaces;

//import net.minecraft.state.IntegerProperty;

public interface IBaseWL {
	default public boolean isWL() {
		return true;
	}

	default public void fixDS() {
	}
}