package net.skds.wpo.mixins.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.world.World;
import net.skds.wpo.fluidphysics.FFluidStatic;

@Mixin(value = { GlassBottleItem.class })
public class GlassBottleItemMixin {

	// , args = "Lnet/minecraft/util/math/RayTraceContext$FluidMode;"
	@ModifyArg(method = "Lnet/minecraft/item/GlassBottleItem;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/GlassBottleItem;getPlayerPOVHitResult(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/RayTraceContext$FluidMode;)Lnet/minecraft/util/math/BlockRayTraceResult;", args = "Lnet/minecraft/util/math/RayTraceContext$FluidMode;"))
	public FluidMode aaa(FluidMode fm) {
		return FluidMode.ANY;

	}

	@Inject(method = "Lnet/minecraft/item/GlassBottleItem;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"), cancellable = true)
	public void bbb(World w, PlayerEntity p, Hand hand, CallbackInfoReturnable<ActionResult<ItemStack>> ci) {
		FFluidStatic.onBottleUse(w, p, hand, ci, p.getItemInHand(hand));
	}

	// ================= SHADOW ================ //

}