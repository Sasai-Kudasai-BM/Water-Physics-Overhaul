package net.skds.wpo.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.skds.wpo.WPO;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(WPO.MOD_ID, "network"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static void send(PlayerEntity target, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)target), message);
	}

	public static SimpleChannel get() {
		return CHANNEL;
	}

	public static void init() {
		int id = 0;
		CHANNEL.registerMessage(id++, DebugPacket.class, DebugPacket::encoder, DebugPacket::decoder, DebugPacket::handle);
		CHANNEL.registerMessage(id++, PipeUpdatePacket.class, PipeUpdatePacket::encoder, PipeUpdatePacket::decoder, PipeUpdatePacket::handle);
	}
}