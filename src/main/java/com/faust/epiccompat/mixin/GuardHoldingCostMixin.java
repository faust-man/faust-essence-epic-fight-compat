package com.faust.epiccompat.mixin;
import com.faust.epiccompat.GuardEntryCost;
@org.spongepowered.asm.mixin.Mixin(value=yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch.class,remap=false)
public abstract class GuardHoldingCostMixin {
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="startSkillHolding") private boolean essence$holding(yesman.epicfight.skill.modules.HoldableSkill skill,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original){
  var patch=(yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch<?>)(Object)this;
  if(!(skill instanceof yesman.epicfight.skill.guard.GuardSkill))return original.call(skill);var container=patch.getSkillContainerFor(skill.asSkill());if(container.isEmpty())return original.call(skill);if(container.get().isActivated())return original.call(skill);if(!GuardEntryCost.pay(patch))return false;
  boolean previous=GuardEntryCost.PAID.get();GuardEntryCost.PAID.set(true);try{return original.call(skill);}finally{GuardEntryCost.PAID.set(previous);}
 }
}
