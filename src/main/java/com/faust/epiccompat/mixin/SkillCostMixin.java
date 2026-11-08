package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.common.ComboAttacks;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
@Mixin(value=Skill.class,remap=false)
public class SkillCostMixin {
    @Inject(method="resourcePredicate",at=@At("HEAD"),cancellable=true)
    private void phaseCost(PlayerPatch<?> patch,SkillCastEvent event,CallbackInfoReturnable<Boolean> cir){if((Object)this instanceof ComboAttacks)cir.setReturnValue(true);}
}
