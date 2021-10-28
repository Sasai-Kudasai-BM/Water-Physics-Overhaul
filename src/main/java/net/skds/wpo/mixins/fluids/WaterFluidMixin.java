package net.skds.wpo.mixins.fluids;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(value = { WaterFluid.class })
public class WaterFluidMixin {

	@OnlyIn(Dist.CLIENT)
	@Overwrite
	public void animateTick(World worldIn, BlockPos pos, FluidState state, Random random) {
		if (!state.isSource() && !state.get(BlockStateProperties.FALLING)) {

			if (random.nextInt(16) == 0 && state.getFlow(worldIn, pos).lengthSquared() > 0.5D) {
				worldIn.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
						SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, random.nextFloat() * 0.25F + 0.75F,
						random.nextFloat() + 0.5F, false);
			}

		} else if (random.nextInt(10) == 0 && state.isSource()) {
			worldIn.addParticle(ParticleTypes.UNDERWATER, (double) pos.getX() + random.nextDouble(),
					(double) pos.getY() + random.nextDouble(), (double) pos.getZ() + random.nextDouble(), 0.0D, 0.0D,
					0.0D);
		}
	}
}