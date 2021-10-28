package net.skds.wpo.data;

import net.minecraft.nbt.CompoundNBT;
import net.skds.core.api.IChunkData;
import net.skds.core.util.SKDSUtils.Side;
import net.skds.core.util.data.capability.ChunkCapabilityData;

public class WPOChunkData implements IChunkData {

	private final Side side;
	//private final ChunkCapabilityData cap;
	
	public WPOChunkData(ChunkCapabilityData cap, Side side) {
		this.side = side;
		//this.cap = cap;
	}

	@Override
	public void tick() {
	}

	@Override
	public void deserialize(CompoundNBT nbt) {
	}

	@Override
	public Side getSide() {
		return side;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public void serialize(CompoundNBT nbt) {
		
	}

}