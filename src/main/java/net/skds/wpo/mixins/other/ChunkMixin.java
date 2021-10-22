package net.skds.wpo.mixins.other;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.skds.core.util.data.ChunkSectionAdditionalData;
import net.skds.wpo.data.FluidStateContainer;
import net.skds.wpo.data.WPOChunkData;

@Mixin(Chunk.class)
public class ChunkMixin {

	@Shadow
	@Final
	ChunkSection[] sections;

	@Overwrite
	public FluidState getFluidState(int x, int y, int z) {
		if (y >= 0 && y >> 4 < this.sections.length) {
			ChunkSection chunksection = this.sections[y >> 4];
			if (!ChunkSection.isEmpty(chunksection)) {

				WPOChunkData data = ChunkSectionAdditionalData.getTyped((Chunk) (Object) this, y >> 4, WPOChunkData.class);
				if (data != null) {
					FluidStateContainer container = data.getFS(x, y, z);
					if (container != FluidStateContainer.EMPTY) {
						return container.state;
					}
				}
				return chunksection.getFluidState(x & 15, y & 15, z & 15);
			}
		}

		return Fluids.EMPTY.getDefaultState();
	}
}
