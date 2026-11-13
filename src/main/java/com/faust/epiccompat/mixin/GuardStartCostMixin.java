package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=yesman.epicfight.skill.SkillContainer.class,remap=false)
public abstract class GuardStartCostMixin {
 @Redirect(method="requestHold",at=@At(value="INVOKE",target="Lyesman/epicfight/skill/SkillContainer;canUse(Lyesman/epicfight/world/capabilities/entitypatch/player/PlayerPatch;Lyesman/epicfight/api/event/types/player/SkillCastEvent;)Z")) private boolean essence$press(yesman.epicfight.skill.SkillContainer container,yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch<?> executor,yesman.epicfight.api.event.types.player.SkillCastEvent event){
  if(!container.canUse(executor,event))return false;if(!(container.getSkill() instanceof yesman.epicfight.skill.guard.GuardSkill))return true;if(!(executor.getOriginal() instanceof net.minecraft.server.level.ServerPlayer player)||!com.faust.core.runtime.CoreRuntime.active(player))return true;
  if(com.faust.core.progression.StaminaServices.present()&&com.faust.core.runtime.CoreRuntime.balance.stamina.get("enabled")>0){var data=com.faust.core.runtime.CoreRuntime.data(player);if(data.stamina<1||data.actionsBlocked(com.faust.core.runtime.CoreRuntime.balance))return false;return com.faust.core.runtime.CoreRuntime.payStaminaExact(player,1);}
  if(executor.getStamina()<1)return false;executor.setStamina(executor.getStamina()-1);return true;
 }
}
