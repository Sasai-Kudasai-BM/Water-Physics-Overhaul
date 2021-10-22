package net.skds.wpo.util;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.skds.core.util.data.ChunkSectionAdditionalData;
import net.skds.wpo.data.WPOChunkData;

public class FUtils {

	public static FluidState setFluidState(FluidState fs, World w, BlockPos pos) {
		Chunk c = w.getChunkProvider().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
		if (c == null || c.isEmpty()) {
			return Fluids.EMPTY.getDefaultState();
		}
		return setFluidState(fs, c, pos.getX(), pos.getY(), pos.getZ());
	}

	public static FluidState setFluidState(FluidState fs, Chunk c, BlockPos pos) {
		return setFluidState(fs, c, pos.getX(), pos.getY(), pos.getZ());
	}

	public static FluidState setFluidState(FluidState fs, Chunk c, int x, int y, int z) {
		WPOChunkData data = ChunkSectionAdditionalData.getTyped(c, y >> 4, WPOChunkData.class);
		if (data == null) {
			return Fluids.EMPTY.getDefaultState();
		}
		
		return null;
	}
}
