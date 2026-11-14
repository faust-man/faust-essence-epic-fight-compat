package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch.class,remap=false)
public class WeaponChargeMixin {
 @Redirect(method="lambda$new$0",at=@At(value="INVOKE",target="Lyesman/epicfight/skill/Skill;setConsumptionSynchronize(Lyesman/epicfight/skill/SkillContainer;F)V"))
 private void essence$charge(yesman.epicfight.skill.Skill skill,yesman.epicfight.skill.SkillContainer container,float value){skill.setConsumptionSynchronize(container,com.faust.epiccompat.AcceleratedThinking.charge(container,value));}
}
