package net.skds.wpo.mixins.other;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.vehicle.Boat;

@Mixin(value = { Boat.class })
public class BoatEntityMixin {

	@Inject(method = "isUnderwater()Lnet/minecraft/world/entity/vehicle/Boat$Status;", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;isSource()Z")), cancellable = true)
	public void aaa(CallbackInfoReturnable<Boat.Status> ci) {
		ci.setReturnValue(Boat.Status.IN_WATER);
	}

	@ModifyVariable(method = "checkInWater", at = @At("STORE"), name = "l", print = false)
	public int bbb(int i) {
		return i + 1;
	}
}