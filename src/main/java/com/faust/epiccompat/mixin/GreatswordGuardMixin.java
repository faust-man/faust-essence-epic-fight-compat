package com.faust.epiccompat.mixin;
import com.faust.epiccompat.WeaponMastery;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(value={yesman.epicfight.skill.guard.GuardSkill.class,yesman.epicfight.skill.guard.ParryingSkill.class,yesman.epicfight.skill.guard.ImpactGuardSkill.class},remap=false)
public abstract class GreatswordGuardMixin {
 @WrapMethod(method="guard") private void essence$guard(yesman.epicfight.skill.SkillContainer container,yesman.epicfight.world.capabilities.item.CapabilityItem item,yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch patch,yesman.epicfight.api.event.types.entity.TakeDamageEvent.Income event,float knockback,float impact,boolean advanced,Operation<Void> original){
  if(!((Object)this instanceof yesman.epicfight.skill.guard.ImpactGuardSkill)&&WeaponMastery.pierces(event.getDamageSource()))return;
  int depth=WeaponMastery.GUARD_DEPTH.get();WeaponMastery.GUARD_DEPTH.set(depth+1);
  try{original.call(container,item,patch,event,knockback,depth==0&&WeaponMastery.mastered(event.getDamageSource())?impact*2:impact,advanced);}finally{WeaponMastery.GUARD_DEPTH.set(depth);}
 }
}
