package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
@Mixin(value=SkillEditScreen.class,remap=false)
public class SkillEditMixin {
    @Inject(method="maxPassiveSkillsRule",at=@At("HEAD"),cancellable=true)
    private void standard(CallbackInfoReturnable<Integer> cir){cir.setReturnValue(3);}
}
