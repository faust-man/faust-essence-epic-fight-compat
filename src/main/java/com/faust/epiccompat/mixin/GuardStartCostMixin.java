package com.faust.epiccompat.mixin;
@org.spongepowered.asm.mixin.Mixin(value=yesman.epicfight.skill.guard.GuardSkill.class,remap=false)
public abstract class GuardStartCostMixin {
 @org.spongepowered.asm.mixin.injection.Inject(method="startHolding",at=@org.spongepowered.asm.mixin.injection.At("HEAD"),cancellable=true) private void essence$start(yesman.epicfight.skill.SkillContainer container,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){if(!container.isActivated()&&!com.faust.epiccompat.GuardEntryCost.PAID.get()&&!com.faust.epiccompat.GuardEntryCost.pay(container.getExecutor()))ci.cancel();}
}
