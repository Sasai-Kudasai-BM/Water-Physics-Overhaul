package net.skds.wpo.mixins.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.skds.wpo.fluidphysics.FFluidStatic;

@Mixin(value = { BottleItem.class })
public class GlassBottleItemMixin {

	// , args = "Lnet/minecraft/util/math/RayTraceContext$FluidMode;"
	@ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BottleItem;getPlayerPOVHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/ClipContext$Fluid;)Lnet/minecraft/world/phys/BlockHitResult;", args = "Lnet/minecraft/world/level/ClipContext$Fluid;"))
	public Fluid aaa(Fluid fm) {
		return Fluid.ANY;

	}

	@Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"), cancellable = true)
	public void bbb(Level w, Player p, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci) {
		FFluidStatic.onBottleUse(w, p, hand, ci, p.getItemInHand(hand));
	}

	// ================= SHADOW ================ //

}