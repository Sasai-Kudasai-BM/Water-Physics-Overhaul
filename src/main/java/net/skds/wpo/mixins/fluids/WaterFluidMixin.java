package net.skds.wpo.mixins.fluids;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(value = { WaterFluid.class })
public class WaterFluidMixin {

	@OnlyIn(Dist.CLIENT)
	@Overwrite
	public void animateTick(Level worldIn, BlockPos pos, FluidState state, Random random) {
		if (!state.isSource() && !state.getValue(BlockStateProperties.FALLING)) {

			if (random.nextInt(16) == 0 && state.getFlow(worldIn, pos).lengthSqr() > 0.5D) {
				worldIn.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
						SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F,
						random.nextFloat() + 0.5F, false);
			}

		} else if (random.nextInt(10) == 0 && state.isSource()) {
			worldIn.addParticle(ParticleTypes.UNDERWATER, (double) pos.getX() + random.nextDouble(),
					(double) pos.getY() + random.nextDouble(), (double) pos.getZ() + random.nextDouble(), 0.0D, 0.0D,
					0.0D);
		}
	}
}