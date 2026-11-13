package com.faust.epiccompat.mixin;
@org.spongepowered.asm.mixin.Mixin(value=yesman.epicfight.skill.Skill.class,remap=false)
public abstract class EmergencyDodgeMixin {
 @org.spongepowered.asm.mixin.injection.Inject(method="resourcePredicate",at=@org.spongepowered.asm.mixin.injection.At("RETURN"),cancellable=true)
 private void essence$emergency(yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch<?> patch,yesman.epicfight.api.event.types.player.SkillCastEvent event,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci){
  if(!ci.getReturnValue()&&((yesman.epicfight.skill.Skill)(Object)this).getCategory()==yesman.epicfight.skill.SkillCategories.DODGE&&com.faust.epiccompat.EmergencyDodge.spend(patch,event.getArguments()))ci.setReturnValue(true);
 }
}
