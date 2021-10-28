package net.skds.wpo.data;

import java.util.concurrent.ConcurrentSkipListSet;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.palette.IPalette;
import net.minecraft.util.palette.IdentityPalette;
import net.minecraft.util.palette.PalettedContainer;
import net.skds.core.api.IChunkSectionData;
import net.skds.core.util.SKDSUtils.Side;
import net.skds.core.util.data.ChunkSectionAdditionalData;
import net.skds.wpo.fluidphysics.FluidTask;
import net.skds.wpo.fluidphysics.WorldWorkSet;
import net.skds.wpo.util.FUtils;

public class WPOChunkSectionData implements IChunkSectionData {

	private static final IPalette<FluidState> REGISTRY_PALETTE = new IdentityPalette<>(
			FluidStateContainer.getIdentityMap(), FluidStateContainer.EMPTY);

	private final PalettedContainer<FluidState> data;
	private final Side side;

	public final ShortArraySet changed = new ShortArraySet();
	public final ChunkSectionAdditionalData sectionData;
	public final WorldWorkSet wws;

	private ConcurrentSkipListSet<FluidTask> TASKS = new ConcurrentSkipListSet<>(WorldWorkSet::compare);

	public WPOChunkSectionData(ChunkSectionAdditionalData sectionData, Side side) {
		this.wws = WorldWorkSet.get(sectionData.world);
		this.sectionData = sectionData;
		this.side = side;
		this.data = new PalettedContainer<>(REGISTRY_PALETTE, FluidStateContainer.getIdentityMap(),
				SerialUtils::readFluidState, SerialUtils::writeFluidState, FluidStateContainer.EMPTY);
	}

	@Override
	public void onUnload() {
		WorldWorkSet.removeTasks(TASKS);
	}

	@Override
	public void deserialize(CompoundNBT nbt) {
		data.readChunkPalette(nbt.getList("Fluids", 10), nbt.getLongArray("FluidStates"));
		ListNBT taskList = nbt.getList("tasks", 10);
		taskList.forEach(n -> TASKS.add(new FluidTask(wws, (CompoundNBT) n)));
	}

	@Override
	public void serialize(CompoundNBT nbt) {
		data.writeChunkPalette(nbt, "Fluids", "FluidStates");
		ListNBT taskList = new ListNBT();
		TASKS.forEach(t -> taskList.add(t.serialize()));
		nbt.put("tasks", taskList);
	}

	public synchronized FluidState setFS(int x, int y, int z, FluidState value) {
		FluidState fs = data.swap(x & 15, y & 15, z & 15, value);
		if (!sectionData.isClient() && fs != value) {
			changed.add((short) getIndex(x, y, z));
		}
		return fs;
	}

	public synchronized FluidState getFS(int x, int y, int z) {
		return data.get(x & 15, y & 15, z & 15);
	}

	private FluidState getFS(int index) {
		return data.get(getX(index), getY(index), getZ(index));
	}

	@SuppressWarnings("unused")
	private FluidState setFS(int index, FluidState value) {
		return data.swap(getX(index), getY(index), getZ(index), value);
	}

	private static int getIndex(int x, int y, int z) {
		return (y & 15) << 8 | (z & 15) << 4 | (x & 15);
	}

	private static int getX(int index) {
		return index & 15;
	}

	private static int getY(int index) {
		return (index >> 8) & 15;
	}

	private static int getZ(int index) {
		return (index >> 4) & 15;
	}

	public void packChanges(PacketBuffer buff) {
		int c = changed.size();
		if (c == 0) {
			buff.writeByte(1);
		} else if (c >= 9999) {
			buff.writeByte(2);
			write(buff);
		} else {
			buff.writeByte(3);
			buff.writeShort(c);
			for (int i : changed) {
				buff.writeShort(i);
				int id = FluidStateContainer.getIdentityMap().getId(getFS(i));
				buff.writeInt(id);
			}
		}
		changed.clear();
	}

	public void readChanges(PacketBuffer buff, byte flag) {
		if (flag == 2) {
			read(buff);
		} else if (flag == 3) {

			short s = buff.readShort();
			for (int i = 0; i < s; i++) {
				int index = buff.readShort();
				int id = buff.readInt();
				FluidState fs = FluidStateContainer.getIdentityMap().getByValue(id);
				BlockPos pos = new BlockPos(getX(index) + sectionData.chunk.getPos().getXStart(),
						getY(index) + (sectionData.secTndex << 4),
						getZ(index) + sectionData.chunk.getPos().getZStart());

				FUtils.setFluidState(fs, sectionData.chunk, this, pos.getX(), pos.getY(), pos.getZ());
				sectionData.world.getLightManager().checkBlock(pos);
			}
		}
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