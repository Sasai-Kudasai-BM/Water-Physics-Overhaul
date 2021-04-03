package net.skds.wpo.util.api;

import net.minecraft.util.Direction;

public interface IConnectionSides {
	public boolean canBeConnected(Direction dir);
	public boolean canBeConnected(int dir);
}