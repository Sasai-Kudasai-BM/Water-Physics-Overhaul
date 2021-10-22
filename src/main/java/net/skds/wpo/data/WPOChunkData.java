package net.skds.wpo.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.palette.IPalette;
import net.minecraft.util.palette.IdentityPalette;
import net.minecraft.util.palette.PalettedContainer;
import net.skds.core.api.IChunkSectionData;
import net.skds.core.util.SKDSUtils.Side;
import net.skds.core.util.data.ChunkSectionAdditionalData;

public class WPOChunkData implements IChunkSectionData {

	private static final IPalette<FluidStateContainer> REGISTRY_PALETTE = new IdentityPalette<>(FluidStateContainer.getIdentityMap(), FluidStateContainer.WATER_DEBUG);

	private final PalettedContainer<FluidStateContainer> data;
	private final Side side;
	

	public final ChunkSectionAdditionalData sectionData;

	public WPOChunkData(ChunkSectionAdditionalData sectionData, Side side) {
		this.sectionData = sectionData;
		this.side = side;
		this.data = new PalettedContainer<>(REGISTRY_PALETTE, FluidStateContainer.getIdentityMap(), SerialUtils::readFluidStateC, SerialUtils::writeFluidStateC, FluidStateContainer.WATER_DEBUG);
	}

	@Override
	public void deserialize(CompoundNBT nbt) {

		data.readChunkPalette(nbt.getList("Fluids", 10), nbt.getLongArray("FluidStates"));
	}

	@Override
	public void serialize(CompoundNBT nbt) {
		data.writeChunkPalette(nbt, "Fluids", "FluidStates");
	}

	public FluidStateContainer setFS(int x, int y, int z, FluidStateContainer value) {
		return data.swap(x & 15, y & 15, z & 15, value);
	}

	public FluidStateContainer getFS(int x, int y, int z) {
		return data.get(x & 15, y & 15, z & 15);
	}

	@Override
	public void read(PacketBuffer buff) {
		data.read(buff);
	}

	@Override
	public void write(PacketBuffer buff) {
		data.write(buff);
	}

	@Override
	public Side getSide() {
		return side;
	}

	@Override
	public int getSize() {
		return data.getSerializedSize();
	}
}