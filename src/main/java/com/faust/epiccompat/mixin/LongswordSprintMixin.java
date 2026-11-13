package com.faust.epiccompat.mixin;
import com.faust.epiccompat.WeaponMastery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=yesman.epicfight.skill.weaponinnate.LiechtenauerSkill.class,remap=false)
public abstract class LongswordSprintMixin {
 @Inject(method="lambda$onInitiate$3",at=@At("HEAD"),cancellable=true) private void essence$sprint(yesman.epicfight.skill.SkillContainer container,yesman.epicfight.api.client.event.types.control.MappedMovementInputUpdateEvent event,CallbackInfo ci){if(WeaponMastery.enabled(container.getExecutor().getOriginal(),WeaponMastery.LONG))ci.cancel();}
}
