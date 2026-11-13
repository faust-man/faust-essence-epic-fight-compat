package com.faust.epiccompat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=yesman.epicfight.api.animation.types.AttackAnimation.class,remap=false)
public abstract class WeaponSwingMixin {
 @Inject(method="begin",at=@At("TAIL")) private void essence$swing(yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch<?> patch,CallbackInfo ci){if(patch.getOriginal() instanceof net.minecraft.server.level.ServerPlayer player)com.faust.epiccompat.WeaponMastery.swing(player);}
}
