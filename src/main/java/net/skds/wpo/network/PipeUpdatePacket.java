package net.skds.wpo.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.skds.wpo.tileentity.PipeTileEntity;

public class PipeUpdatePacket {

	private CompoundNBT nbt;

	public PipeUpdatePacket(CompoundNBT nbt) {
		this.nbt = nbt;
	}

	public PipeUpdatePacket(PacketBuffer buffer) {
		this.nbt = buffer.readCompoundTag();
	}

	public void encoder(PacketBuffer buffer) {
		buffer.writeCompoundTag(nbt);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {		
		Minecraft minecraft = Minecraft.getInstance();
		ClientWorld w = (ClientWorld) minecraft.player.world;
		//w.addParticle(ParticleTypes.FLAME, nbt.getX() + 0.5, nbt.getY() + 0.5, nbt.getZ() + 0.5, 0, 0.06, 0);
		int x = nbt.getInt("x");
		int y = nbt.getInt("y");
		int z = nbt.getInt("z");
		BlockPos pos = new BlockPos(x, y, z);
		//w.addParticle(ParticleTypes.FLAME, x + 0.5, y + 0.5, z + 0.5, 0, 0.06, 0);

		TileEntity te = w.getTileEntity(pos);
		if (te != null && te instanceof PipeTileEntity) {
			PipeTileEntity pipe = (PipeTileEntity) te;
			pipe.read(null, nbt);
		}


		context.get().setPacketHandled(true);
	}
}