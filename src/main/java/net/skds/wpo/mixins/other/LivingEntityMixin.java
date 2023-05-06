package net.skds.wpo.mixins.other;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = { LivingEntity.class })
public abstract class LivingEntityMixin extends Entity {
    @Shadow protected abstract void defineSynchedData();

    @Shadow protected abstract float getWaterSlowDown();

    @Shadow protected abstract float getFrictionInfluencedSpeed(float p_21331_);

    public LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        LOGGER.error("LivingEntityMixin constructor should never be called (IDE forced me to implement it");
    }

    // #### LivingEntity.travel(): WATER-MOTION ####
    // (since everything is in ticks the dt = 1 [tick] in the discrete integration is dropped in the code)
    // this.deltaMovement: entity velocity -> is added to entity position every tick: pos += velocity (* dt)
    // f6: input acceleration/force -> is added to entity velocity every tick: velocity += accel (* dt)
    // (1-f5): viscous drag coefficient -> is scaled by velocity subtracted from it every tick: velocity -= cDrag * velocity
    // full integrals of motion:
    // position = position + this.deltaMovement * dt
    // this.deltaMovement = this.deltaMovement + f6 * dt - (1-f5) * this.deltaMovement = f5 * this.deltaMovement + f6 * dt
    // equations of motion (in execution order):
    // this.deltaMovement += f6
    // position += this.deltaMovement
    // this.deltaMovement *= f5
    // (walkingSpeed=0.1, sprintingSpeed=0.13)
    // CONVERGENCE (forward hold): velocity -> f6 / (1 - f5)

    // these two variable fixes fix walking speed in water (aiStep calls travel internally, so we're good)
    @ModifyVariable(method = "travel", at = @At("STORE"), name = "f6")
    private float correctWaterSpeedForDepth(float oldWaterSpeed) {
        if (oldWaterSpeed == 0.02F) {  // this is the init value: this only checks if the call is right after init
            FluidState fluidstate = this.level.getFluidState(this.blockPosition());
            BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
            float f3 = this.level.getBlockState(blockpos).getFriction(level, blockpos, this);
            float landSpeed = this.getFrictionInfluencedSpeed(f3); // <-> f6 -- walking speed (on dry ground)
            // LERP: (water amount: 0 to AMOUNT_FULL) -> (speed: landSpeed to oldWaterSpeed)
            float waterSpeed = landSpeed - (landSpeed - oldWaterSpeed) * fluidstate.getAmount() / FluidState.AMOUNT_FULL;
            // SWIM_SPEED seems to be always 1.0... idk if this would go havoc if it changed (see next line)
//            float swimSpeed = (float)this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
//            LOGGER.info("LivingEntity.travel: water_level=" + fluidstate.getAmount() + ", oldSpeed=" + oldWaterSpeed +
//                    ", newSpeed=" + waterSpeed + ", blockFriction=" + f3 + ", landSpeed=" + landSpeed);
            return waterSpeed;
        } else {
            return oldWaterSpeed;
        }
    }

    @ModifyVariable(method = "travel", at = @At("STORE"), name = "f5")
    private float correctWaterDragForDepth(float oldWaterDrag) {
        // this is the init value: this only checks if the call is right after init
        if (this.isSprinting() && oldWaterDrag == 0.9F || !this.isSprinting() && oldWaterDrag == this.getWaterSlowDown()) {
            BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
            float f3 = this.level.getBlockState(blockpos).getFriction(level, blockpos, this);
            float landDrag = this.onGround ? f3 * 0.91F : 0.91F; // <-> f5 -- walking drag (on dry ground)
            FluidState fluidstate = this.level.getFluidState(this.blockPosition());
            // LERP: (water amount: 0 to AMOUNT_FULL) -> (speed: landDrag to oldWaterDrag)  => see lerp for Depth Strider
            float waterDrag = landDrag + (oldWaterDrag - landDrag) * fluidstate.getAmount() / FluidState.AMOUNT_FULL;
//            LOGGER.info("LivingEntity.travel: water_level=" + fluidstate.getAmount() + ", oldDrag=" + oldWaterDrag +
//                    ", newDrag=" + waterDrag + ", landDrag=" + landDrag + ", blockFriction=" + f3);
            return waterDrag;
        } else {
            return oldWaterDrag;
        }
    }

    // TODO fix lava and other liquids?

    // jumping is already correct, because LivingEntity.aiStep already does the correct jump depending on the water height
}
