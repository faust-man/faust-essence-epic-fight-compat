package com.faust.epiccompat.mixin;
import com.faust.epiccompat.WeaponMastery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
@Mixin(value=EpicFightDamageSource.class,remap=false)
public abstract class GreatswordDamageMixin {
 @Inject(method="calculateArmorNegation",at=@At("RETURN"),cancellable=true) private void essence$armor(CallbackInfoReturnable<Float> ci){ci.setReturnValue(WeaponMastery.armorNegation((EpicFightDamageSource)(Object)this,ci.getReturnValue()));}
 @Inject(method="calculateDamageAgainst",at=@At("RETURN"),cancellable=true) private void essence$shieldDamage(net.minecraft.world.entity.Entity owner,net.minecraft.world.entity.LivingEntity target,float base,CallbackInfoReturnable<Float> ci){if(WeaponMastery.shieldSpecial((EpicFightDamageSource)(Object)this))ci.setReturnValue(ci.getReturnValue()*3);}
 @Inject(method="calculateImpact",at=@At("RETURN"),cancellable=true) private void essence$shieldImpact(CallbackInfoReturnable<Float> ci){if(WeaponMastery.shieldSpecial((EpicFightDamageSource)(Object)this))ci.setReturnValue(ci.getReturnValue()*3);}
 @Inject(method="is(Lnet/minecraft/tags/TagKey;)Z",at=@At("RETURN"),cancellable=true) private void essence$shield(net.minecraft.tags.TagKey<net.minecraft.world.damagesource.DamageType> tag,CallbackInfoReturnable<Boolean> ci){if(tag==net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD&&WeaponMastery.pierces((EpicFightDamageSource)(Object)this))ci.setReturnValue(true);}
}
