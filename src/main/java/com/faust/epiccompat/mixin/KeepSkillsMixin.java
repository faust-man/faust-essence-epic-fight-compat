package com.faust.epiccompat.mixin;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Essence progression and equipped Epic Fight skills survive player death. */
@Mixin(value=EpicFightGameRules.ConfigurableGameRule.class,remap=false)
public abstract class KeepSkillsMixin {
 @Inject(method="getRuleValue",at=@At("HEAD"),cancellable=true)
 private void essence$keep(net.minecraft.world.level.Level level,CallbackInfoReturnable<Object> ci){if((Object)this==EpicFightGameRules.KEEP_SKILLS)ci.setReturnValue(Boolean.TRUE);}
}
