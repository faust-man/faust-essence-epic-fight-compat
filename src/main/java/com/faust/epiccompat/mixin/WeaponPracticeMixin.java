package com.faust.epiccompat.mixin;
import com.faust.epiccompat.WeaponMastery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=yesman.epicfight.skill.SkillContainer.class,remap=false)
public abstract class WeaponPracticeMixin {
 @Inject(method="requestCasting",at=@At("RETURN")) private void essence$cast(yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch executor,net.minecraft.nbt.CompoundTag args,CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValue())WeaponMastery.used((yesman.epicfight.skill.SkillContainer)(Object)this);}
}
