package net.skds.wpo.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class DebugPacket {

	private BlockPos pos;

	public DebugPacket(BlockPos pos) {
		this.pos = pos;
	}

	public DebugPacket(FriendlyByteBuf buffer) {
		this.pos = buffer.readBlockPos();
	}

	void encoder(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	public static DebugPacket decoder(FriendlyByteBuf buffer) {
		return new DebugPacket(buffer);
	}

	void handle(Supplier<NetworkEvent.Context> context) {		
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel w = (ClientLevel) minecraft.player.level;
		w.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0.06, 0);
		context.get().setPacketHandled(true);
	}
}