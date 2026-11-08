package com.faust.epiccompat.mixin;
import com.faust.core.progression.Progress;
import com.faust.epiccompat.Milestones;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Progress.class,remap=false)
public class FatigueMilestoneMixin {
    @Inject(method="updateExhaustion",at=@At("HEAD"))
    private void observe(com.faust.core.progression.Balance balance,CallbackInfo ci){Milestones.fatigue((Progress)(Object)this);}
}
