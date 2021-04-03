package net.skds.wpo.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class DebugPacket {

	private BlockPos pos;

	public DebugPacket(BlockPos pos) {
		this.pos = pos;
	}

	public DebugPacket(PacketBuffer buffer) {
		this.pos = buffer.readBlockPos();
	}

	void encoder(PacketBuffer buffer) {
		buffer.writeBlockPos(pos);
	}

	public static DebugPacket decoder(PacketBuffer buffer) {
		return new DebugPacket(buffer);
	}

	void handle(Supplier<NetworkEvent.Context> context) {		
		Minecraft minecraft = Minecraft.getInstance();
		ClientWorld w = (ClientWorld) minecraft.player.world;
		w.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0.06, 0);
		context.get().setPacketHandled(true);
	}
}