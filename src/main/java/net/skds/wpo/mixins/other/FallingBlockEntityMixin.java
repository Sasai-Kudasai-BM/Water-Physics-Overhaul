package net.skds.wpo.mixins.other;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.skds.wpo.fluidphysics.TurboDisplacer;

@Mixin(value = { FallingBlockEntity.class })
public class FallingBlockEntityMixin {

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	public boolean aaa(World w, BlockPos pos, BlockState ns, int i) {
		BlockState os = w.getBlockState(pos);
		if (!os.getFluidState().isEmpty() && !w.isClientSide) {
			TurboDisplacer.markForDisplace((ServerWorld) w, pos, os, ns);
			//System.out.println(w + "   " + pos + "   " + os + "   " + ns);
		}
		return w.setBlock(pos, ns, i);
	}
}