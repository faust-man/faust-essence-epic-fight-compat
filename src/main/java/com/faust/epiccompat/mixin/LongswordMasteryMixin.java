package com.faust.epiccompat.mixin;
import com.faust.epiccompat.WeaponMastery;
import yesman.epicfight.skill.SkillContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
@Mixin(value=yesman.epicfight.skill.weaponinnate.LiechtenauerSkill.class,remap=false)
public abstract class LongswordMasteryMixin {
 @ModifyExpressionValue(method="lambda$onInitiate$2",at=@At(value="INVOKE",target="Lyesman/epicfight/api/animation/types/EntityState;getLevel()I")) private int essence$window(int level,SkillContainer container,yesman.epicfight.api.event.types.entity.TakeDamageEvent.Income event){return level==3&&WeaponMastery.extendedParry(container)?2:level;}
 @Inject(method="lambda$onInitiate$2",at=@At(value="INVOKE",target="Lyesman/epicfight/world/capabilities/entitypatch/player/PlayerPatch;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"),cancellable=true)
 private void essence$blockCost(SkillContainer container,yesman.epicfight.api.event.types.entity.TakeDamageEvent.Income event,CallbackInfo ci){
  var patch=container.getExecutor();if(!(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer p)||!com.faust.essence.api.EssenceApi.active(p))return;
  double cost=com.faust.essence.api.EssenceApi.enabled(p,WeaponMastery.LONG)?0.5:1;
  boolean paid;
  if(com.faust.core.progression.StaminaServices.present())paid=com.faust.essence.api.Resources.payStamina(p,cost,true);
  else {paid=patch.getStamina()>=cost;if(paid)patch.setStamina(patch.getStamina()-(float)cost);}
  if(!paid)ci.cancel();
 }
 @Inject(method="isBlockableSource",at=@At("HEAD"),cancellable=true) private static void essence$pierce(net.minecraft.world.damagesource.DamageSource source,CallbackInfoReturnable<Boolean> ci){if(WeaponMastery.pierces(source))ci.setReturnValue(false);}
}
