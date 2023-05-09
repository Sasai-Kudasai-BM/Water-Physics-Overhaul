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
	public boolean aaa(Level level, BlockPos pos, BlockState newBS, int i) {
		BlockState oldBS = level.getBlockState(pos);
		if (!oldBS.getFluidState().isEmpty() && !level.isClientSide) {
			TurboDisplacer.markForDisplace((ServerLevel) level, pos, oldBS, newBS);
			//System.out.println(level + "   " + pos + "   " + oldBS + "   " + newBS);
		}
		return level.setBlock(pos, newBS, i);
	}
}