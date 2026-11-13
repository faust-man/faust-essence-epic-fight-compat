package com.faust.epiccompat.mixin;
import com.faust.epiccompat.WeaponMastery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
@Mixin(value=EpicFightDamageSource.class,remap=false)
public abstract class GreatswordDamageMixin {
 @Inject(method="calculateArmorNegation",at=@At("RETURN"),cancellable=true) private void essence$armor(CallbackInfoReturnable<Float> ci){ci.setReturnValue(Math.min(100,ci.getReturnValue()*WeaponMastery.armorMultiplier((EpicFightDamageSource)(Object)this)));}
 @Inject(method="is(Lnet/minecraft/tags/TagKey;)Z",at=@At("RETURN"),cancellable=true) private void essence$shield(net.minecraft.tags.TagKey<net.minecraft.world.damagesource.DamageType> tag,CallbackInfoReturnable<Boolean> ci){if(tag==net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD&&WeaponMastery.pierces((EpicFightDamageSource)(Object)this))ci.setReturnValue(true);}
}
