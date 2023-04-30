package net.skds.wpo.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.skds.wpo.block.entity.PipeBlockEntity;

public class PipeUpdatePacket {

	private CompoundTag nbt;

	public PipeUpdatePacket(CompoundTag nbt) {
		this.nbt = nbt;
	}

	public PipeUpdatePacket(FriendlyByteBuf buffer) {
		this.nbt = buffer.readNbt();
	}

	void encoder(FriendlyByteBuf buffer) {
		buffer.writeNbt(nbt);
	}

	public static PipeUpdatePacket decoder(FriendlyByteBuf buffer) {
		return new PipeUpdatePacket(buffer);
	}

	void handle(Supplier<NetworkEvent.Context> context) {		
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel w = (ClientLevel) minecraft.player.level;
		//w.addParticle(ParticleTypes.FLAME, nbt.getX() + 0.5, nbt.getY() + 0.5, nbt.getZ() + 0.5, 0, 0.06, 0);
		int x = nbt.getInt("x");
		int y = nbt.getInt("y");
		int z = nbt.getInt("z");
		BlockPos pos = new BlockPos(x, y, z);
		//w.addParticle(ParticleTypes.FLAME, x + 0.5, y + 0.5, z + 0.5, 0, 0.06, 0);

		BlockEntity te = w.getBlockEntity(pos);
		if (te != null && te instanceof PipeBlockEntity) {
			PipeBlockEntity pipe = (PipeBlockEntity) te;
			pipe.load(nbt);
		}


		context.get().setPacketHandled(true);
	}
}