package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.guard.*;
/** Core learned prerequisites replace Epic Fight's equipped-base-guard requirement. */
@Mixin(value={ImpactGuardSkill.class,ParryingSkill.class},remap=false)
public class GuardPriorMixin {
    @Inject(method="getPriorSkill",at=@At("HEAD"),cancellable=true)
    private void corePrerequisite(CallbackInfoReturnable<Skill> cir){cir.setReturnValue(null);}
}
