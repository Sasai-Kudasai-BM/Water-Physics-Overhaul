package net.skds.wpo.mixins.other;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.item.BoatEntity;

@Mixin(value = { BoatEntity.class })
public class BoatEntityMixin {

	@Inject(method = "Lnet/minecraft/entity/item/BoatEntity;getUnderwaterStatus()Lnet/minecraft/entity/item/BoatEntity$Status;", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isSource()Z")), cancellable = true)
	public void aaa(CallbackInfoReturnable<BoatEntity.Status> ci) {
		ci.setReturnValue(BoatEntity.Status.IN_WATER);
	}

	@ModifyVariable(method = "checkInWater", at = @At("STORE"), name = "l", print = false)
	public int bbb(int i) {
		return i + 1;
	}
}