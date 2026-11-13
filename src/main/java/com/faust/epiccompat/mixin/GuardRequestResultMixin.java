package com.faust.epiccompat.mixin;
@org.spongepowered.asm.mixin.Mixin(value=yesman.epicfight.skill.SkillContainer.class,remap=false)
public abstract class GuardRequestResultMixin {
 @org.spongepowered.asm.mixin.injection.Inject(method="requestHold",at=@org.spongepowered.asm.mixin.injection.At("RETURN"),cancellable=true) private void essence$result(yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch executor,net.minecraft.nbt.CompoundTag args,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci){var container=(yesman.epicfight.skill.SkillContainer)(Object)this;if(ci.getReturnValue()&&container.getSkill() instanceof yesman.epicfight.skill.guard.GuardSkill&&!container.isActivated())ci.setReturnValue(false);}
}
