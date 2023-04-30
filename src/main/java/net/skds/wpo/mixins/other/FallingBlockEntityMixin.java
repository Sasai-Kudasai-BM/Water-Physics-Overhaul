package net.skds.wpo.mixins.other;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.skds.wpo.fluidphysics.TurboDisplacer;

@Mixin(value = { FallingBlockEntity.class })
public class FallingBlockEntityMixin {

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	public boolean aaa(Level w, BlockPos pos, BlockState ns, int i) {
		BlockState os = w.getBlockState(pos);
		if (!os.getFluidState().isEmpty() && !w.isClientSide) {
			TurboDisplacer.markForDisplace((ServerLevel) w, pos, os, ns);
			//System.out.println(w + "   " + pos + "   " + os + "   " + ns);
		}
		return w.setBlock(pos, ns, i);
	}
}