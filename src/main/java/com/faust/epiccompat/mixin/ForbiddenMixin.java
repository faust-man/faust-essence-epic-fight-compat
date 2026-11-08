package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.passive.ForbiddenStrengthSkill;
import yesman.epicfight.skill.passive.PassiveSkill;
import yesman.epicfight.skill.*;
import yesman.epicfight.api.event.EntityEventListener;
@Mixin(value=ForbiddenStrengthSkill.class,remap=false)
public abstract class ForbiddenMixin extends PassiveSkill {
    protected ForbiddenMixin(SkillBuilder<?> builder){super(builder);}
    @Inject(method="onInitiate",at=@At("HEAD"),cancellable=true)
    private void replace(SkillContainer container,EntityEventListener listener,CallbackInfo ci){super.onInitiate(container,listener);ci.cancel();}
}
