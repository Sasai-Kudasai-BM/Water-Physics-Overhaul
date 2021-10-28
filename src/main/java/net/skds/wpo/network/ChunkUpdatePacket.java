package net.skds.wpo.network;

import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;
import net.skds.core.util.data.ChunkSectionAdditionalData;
import net.skds.core.util.data.capability.ChunkCapabilityData;
import net.skds.wpo.data.WPOChunkSectionData;

public class ChunkUpdatePacket {

	WPOChunkSectionData[] data;
	ByteBuf buf;
	int x;
	int z;
	int c;

	public ChunkUpdatePacket(ChunkCapabilityData cp) {
		ChunkPos pos = cp.chunk.getPos();
		x = pos.x;
		z = pos.z;
		c = cp.getCSADSize();
		data = new WPOChunkSectionData[c];
		for (int i = 0; i < c; i++) {
			ChunkSectionAdditionalData ad = cp.getCSAD(i, false);
			if (ad != null) {
				data[i] = ad.getData(WPOChunkSectionData.class);
			}
		}
	}

	public ChunkUpdatePacket(PacketBuffer buffer) {
		c = buffer.readByte();
		x = buffer.readInt();
		z = buffer.readInt();
		buf = buffer.copy();
	}

	public void encoder(PacketBuffer buffer) {
		buffer.writeByte(c);
		buffer.writeInt(x);
		buffer.writeInt(z);
		for (WPOChunkSectionData d : data) {
			if (d == null) {
				buffer.writeByte(5);
			} else {
				d.packChanges(buffer);
			}
		}
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		Minecraft mc = Minecraft.getInstance();
		World w = mc.world;
		Chunk chunk = w.getChunkProvider().getChunkNow(x, z);
		if (chunk != null && !chunk.isEmpty()) {
			PacketBuffer buffer = new PacketBuffer(buf);
			for (int i = 0; i < c; i++) {
				final int y = i;
				ChunkCapabilityData.apply(chunk, cap -> {
					byte b = buffer.readByte();
					if (b != 5) {
						cap.getCSAD(y, true).getData(WPOChunkSectionData.class).readChanges(buffer, b);
					}
				});
				//chunk.markDirty();
				//chunk.setModified(true);
			}
		}
		context.get().setPacketHandled(true);
	}
}