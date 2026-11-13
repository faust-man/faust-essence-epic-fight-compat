package com.faust.epiccompat.mixin;
@org.spongepowered.asm.mixin.Mixin(targets="com.faust.stamina.StaminaModel",remap=false)
public abstract class SecondWindAttackMixin {
 @org.spongepowered.asm.mixin.injection.Inject(method="secondWind",at=@org.spongepowered.asm.mixin.injection.At("HEAD"),cancellable=true) private void essence$pause(com.faust.core.progression.Progress progress,double dt,com.faust.core.progression.Balance balance,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){var player=com.faust.epiccompat.Forbidden.TICK.get();if(player!=null&&com.faust.epiccompat.WeaponMastery.windBlocked(player))ci.cancel();}
}
