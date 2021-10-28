package net.skds.wpo.mixins.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.skds.wpo.fluidphysics.FluidTasksManager;

@Mixin(World.class)
public class WorldMixin {
	
	@Inject(method = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	void setBlockState(BlockPos pos, BlockState newState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, Chunk chunk, Block block, BlockSnapshot blockSnapshot, BlockState oldState, int oldLight, int oldOpacity) {
		World world = (World) (Object) this;
		if (!world.isRemote && chunk != null && !chunk.isEmpty()) {
			FluidTasksManager.onBlockAdded((ServerWorld) world, pos, newState, oldState, chunk, flags);
		}
	}
}
